package com.lyrashop.brand.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "brand_settings")
public class BrandSettings {
    @Id
    private Short id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String tagline;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String story;

    @Column(nullable = false)
    private int founded;

    @Column(nullable = false, length = 50)
    private String hotline;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 500)
    private String address;

    protected BrandSettings() {}

    public String getName() { return name; }
    public String getTagline() { return tagline; }
    public String getStory() { return story; }
    public int getFounded() { return founded; }
    public String getHotline() { return hotline; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
}
