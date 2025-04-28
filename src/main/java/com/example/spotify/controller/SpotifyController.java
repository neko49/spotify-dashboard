package com.example.spotify.controller;

import com.example.spotify.model.TrackInfo;
import com.example.spotify.service.SpotifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class SpotifyController {
    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "artistName", required = false) String artistName, Model model) {
        // Valeur par défaut
        if (artistName == null || artistName.isEmpty()) {
            artistName = "Avicii";
        }

        // Récupère l'ID à partir du nom
        String artistId = spotifyService.searchArtistIdByName(artistName);
        model.addAttribute("tracks", spotifyService.getTopTracks(artistId));
        return "index";
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public List<String> autocomplete(@RequestParam String query) {
        return spotifyService.autocompleteArtistNames(query);
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam String artistName, HttpServletResponse response) throws IOException {
        String artistId = spotifyService.searchArtistIdByName(artistName);
        List<TrackInfo> tracks = spotifyService.getTopTracks(artistId);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=tracks.csv");

        PrintWriter writer = response.getWriter();
        writer.println("Titre,Popularité,Durée,Date de sortie");

        for (TrackInfo track : tracks) {
            writer.printf("%s,%d,%s,%s%n",
                    track.getTitle().replace(",", " "),
                    track.getPopularity(),
                    track.getDuration(),
                    track.getReleaseDate());
        }
        writer.flush();
    }

    @GetMapping("/export/json")
    public void exportJson(@RequestParam String artistName, HttpServletResponse response) throws IOException {
        String artistId = spotifyService.searchArtistIdByName(artistName);
        List<TrackInfo> tracks = spotifyService.getTopTracks(artistId);

        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=tracks.json");

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(tracks));
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "artistName", required = false) String artistName, Model model) {
        if (artistName == null || artistName.isEmpty()) {
            artistName = "Avicii";
        }

        String artistId = spotifyService.searchArtistIdByName(artistName);
        model.addAttribute("tracks", spotifyService.getTopTracks(artistId));
        model.addAttribute("artistName", artistName);
        return "dashboard";
    }


}

