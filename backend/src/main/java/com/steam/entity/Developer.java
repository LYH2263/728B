package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Developer {
    private Long id;
    private String name;
    private String avatar;
    private String description;
    private String country;
    private Integer foundedYear;
    private String website;
    private Integer followerCount;
    private Integer gameCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
