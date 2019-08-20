package net.mossol.bot.model;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class LocationInfo {
    @Id
    private String id;
    private String title;
    private double latitude;
    private double longitude;
    private MenuType type;

    public LocationInfo() {

    }

    public LocationInfo(String title, double latitude, double longitude, MenuType type) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }
}
