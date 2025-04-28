package com.alim.greennote.data.model;

import android.graphics.Color;

public class ModelTask {

    private int id;
    private String title;
    private String description;
    private long dueDateMillis;
    private String priority;
    private String category;
    private int color;
    private int autoArchiveDays;

    public ModelTask() {
        id = 0;
        title = "";
        description = "";
        dueDateMillis = 0;
        priority = "Low";
        category = "General";
        color = Color.BLACK;
        autoArchiveDays = 0;
    }

    public ModelTask(
            int id, String title, String description, long dueDateMillis,
            String priority, String category, int color, int autoArchiveDays
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDateMillis = dueDateMillis;
        this.priority = priority;
        this.category = category;
        this.color = color;
        this.autoArchiveDays = autoArchiveDays;
    }

    // --- Getters and Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDueDateMillis() {
        return dueDateMillis;
    }

    public void setDueDateMillis(long dueDateMillis) {
        this.dueDateMillis = dueDateMillis;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getAutoArchiveDays() {
        return autoArchiveDays;
    }

    public void setAutoArchiveDays(int autoArchiveDays) {
        this.autoArchiveDays = autoArchiveDays;
    }
}