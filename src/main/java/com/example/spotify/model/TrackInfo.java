package com.example.spotify.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackInfo {
    private String title;
    private int popularity;
    private String duration;
    private String releaseDate;
    private String imageUrl;
}
