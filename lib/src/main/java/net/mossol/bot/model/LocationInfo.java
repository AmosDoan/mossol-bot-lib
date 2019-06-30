package net.mossol.bot.model;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class LocationInfo {
    @Id
    private String id;
    private final String title;
    private final double latitude;
    private final double longitude;
}
