package com.example.spotify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpotifyDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotifyDashboardApplication.class, args);
    }
}
