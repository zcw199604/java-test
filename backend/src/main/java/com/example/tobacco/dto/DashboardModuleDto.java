package com.example.tobacco.dto;

public class DashboardModuleDto {

    private String key;
    private String title;
    private String description;
    private String route;

    public DashboardModuleDto() {}
    public DashboardModuleDto(String key, String title, String description, String route) {
        this.key = key; this.title = title; this.description = description; this.route = route;
    }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
}
