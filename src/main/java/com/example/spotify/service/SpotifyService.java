package com.example.spotify.service;

import com.example.spotify.model.TrackInfo;
import jakarta.annotation.PostConstruct;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);

    private final String clientId;
    private final String clientSecret;

    private String accessToken = "";
    private long tokenExpirationTime = 0;

    // Constructeur avec injection des propriétés
    public SpotifyService(@Value("${spotify.client.id}") String clientId,
                          @Value("${spotify.client.secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // Appel post-initialisation qui garantit que les injections sont faites
    @PostConstruct
    public void init() {
        authenticate();
    }

    private void authenticate() {
        try {
            logger.info("Authentification auprès de Spotify...");
            HttpResponse<JsonNode> response = Unirest.post("https://accounts.spotify.com/api/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .basicAuth(clientId, clientSecret)
                    .body("grant_type=client_credentials")
                    .asJson();

            int status = response.getStatus();
            if (status != 200) {
                logger.error("Échec de l'authentification (HTTP " + status + ") : " + response.getBody().toString());
                throw new RuntimeException("Erreur d'authentification Spotify: " + response.getBody().toString());
            }

            JSONObject body = response.getBody().getObject();
            if (!body.has("access_token")) {
                logger.error("Réponse invalide d'authentification: " + body.toString());
                throw new RuntimeException("Erreur d'authentification Spotify: " + body.toString());
            }
            accessToken = body.getString("access_token");
            int expiresIn = body.getInt("expires_in"); // durée en secondes
            tokenExpirationTime = System.currentTimeMillis() + expiresIn * 1000;
            logger.info("Authentification réussie. Token obtenu, expirant dans " + expiresIn + " secondes.");
        } catch (UnirestException e) {
            logger.error("Erreur lors de la requête d'authentification à Spotify", e);
            throw new RuntimeException("Erreur lors de l'authentification à Spotify", e);
        }
    }

    private void ensureValidToken() {
        if (System.currentTimeMillis() >= tokenExpirationTime) {
            logger.info("Token expiré ou invalide, ré-authentification...");
            authenticate();
        }
    }

    @Cacheable(value = "topTracks", key = "#artistId")
    public List<TrackInfo> getTopTracks(String artistId) {
        ensureValidToken();
        try {
            HttpResponse<JsonNode> response = Unirest.get("https://api.spotify.com/v1/artists/" + artistId + "/top-tracks?market=FR")
                    .header("Authorization", "Bearer " + accessToken)
                    .asJson();

            int status = response.getStatus();
            if (status != 200) {
                logger.error("Erreur lors de la récupération des top tracks (HTTP " + status + ") : " + response.getBody().toString());
                throw new RuntimeException("Erreur lors de la récupération des top tracks");
            }

            List<TrackInfo> tracks = new ArrayList<>();
            JSONArray items = response.getBody().getObject().getJSONArray("tracks");
            for (int i = 0; i < items.length(); i++) {
                JSONObject track = items.getJSONObject(i);
                String title = track.getString("name");
                int popularity = track.getInt("popularity");

                int durationMs = track.getInt("duration_ms");
                String duration = (durationMs / 60000) + "m " + (durationMs % 60000) / 1000 + "s";

                String releaseDate = "N/A";
                if (track.has("album")) {
                    releaseDate = track.getJSONObject("album").optString("release_date", "N/A");
                }

                String imageUrl = "";
                if (track.has("album")) {
                    JSONArray images = track.getJSONObject("album").optJSONArray("images");
                    if (images != null && images.length() > 0) {
                        imageUrl = images.getJSONObject(0).optString("url", "");
                    }
                }
                tracks.add(new TrackInfo(title, popularity, duration, releaseDate, imageUrl));
            }
            return tracks;
        } catch (UnirestException e) {
            logger.error("Erreur lors de la récupération des top tracks pour l'artiste " + artistId, e);
            throw new RuntimeException("Erreur lors de la récupération des top tracks", e);
        }
    }

    public List<String> autocompleteArtistNames(String query) {
        try {
            HttpResponse<JsonNode> response = Unirest.get("https://api.spotify.com/v1/search")
                    .header("Authorization", "Bearer " + accessToken)
                    .queryString("q", query)
                    .queryString("type", "artist")
                    .queryString("limit", 5)
                    .asJson();

            int status = response.getStatus();
            if (status != 200) {
                logger.error("Erreur lors de l'autocomplétion (HTTP " + status + "): " + response.getBody().toString());
                throw new RuntimeException("Erreur lors de l'autocomplétion");
            }

            List<String> suggestions = new ArrayList<>();
            JSONArray items = response.getBody()
                    .getObject()
                    .getJSONObject("artists")
                    .getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                String name = items.getJSONObject(i).getString("name");
                suggestions.add(name);
            }
            return suggestions;
        } catch (UnirestException e) {
            logger.error("Erreur lors de l'autocomplétion pour la requête: " + query, e);
            throw new RuntimeException("Erreur lors de l'autocomplétion", e);
        }
    }

    public String searchArtistIdByName(String artistName) {
        try {
            HttpResponse<JsonNode> response = Unirest.get("https://api.spotify.com/v1/search")
                    .header("Authorization", "Bearer " + accessToken)
                    .queryString("q", artistName)
                    .queryString("type", "artist")
                    .queryString("limit", 1)
                    .asJson();

            int status = response.getStatus();
            if (status != 200) {
                logger.error("Erreur lors de la recherche de l'artiste (HTTP " + status + "): " + response.getBody().toString());
                throw new RuntimeException("Erreur lors de la recherche de l'artiste");
            }

            JSONArray items = response.getBody().getObject()
                    .getJSONObject("artists")
                    .getJSONArray("items");

            if (items.length() > 0) {
                return items.getJSONObject(0).getString("id");
            }
            return "1vCWHaC5f2uS3yhpwWbIA6";
        } catch (UnirestException e) {
            logger.error("Erreur lors de la recherche de l'artiste: " + artistName, e);
            throw new RuntimeException("Erreur lors de la recherche de l'artiste", e);
        }
    }
}
