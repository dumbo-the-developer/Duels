package com.meteordevelopments.duels.rank;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class Rank {
    
    @Getter
    private final String id;
    
    @Getter
    @Setter
    private String name;
    
    @Getter
    @Setter
    private String description;
    
    @Getter
    @Setter
    private int minElo;
    
    @Getter
    @Setter
    private int maxElo;
    
    @Getter
    @Setter
    private String color;
    
    @Getter
    @Setter
    private List<String> gradientColors;
    
    @Getter
    @Setter
    private boolean gradientEnabled;
    
    @Getter
    @Setter
    private List<String> promotionCommands;
    
    @Getter
    @Setter
    private List<String> oneTimeCommands;
    
    @Getter
    @Setter
    private List<String> demotionCommands;
    
    public Rank(String id) {
        this.id = id;
        this.gradientColors = new ArrayList<>();
        this.promotionCommands = new ArrayList<>();
        this.oneTimeCommands = new ArrayList<>();
        this.demotionCommands = new ArrayList<>();
    }
    
    public void loadFromConfig(ConfigurationSection section) {
        this.name = section.getString("name", "Unknown Rank");
        this.description = section.getString("description", "No description");
        this.minElo = section.getInt("min-elo", 0);
        this.maxElo = section.getInt("max-elo", 999999);
        this.color = section.getString("color", "&7");
        this.gradientEnabled = section.getBoolean("gradient-enabled", false);
        
        // Load gradient colors
        if (section.contains("gradient-colors")) {
            this.gradientColors = section.getStringList("gradient-colors");
        }
        
        // Load commands
        this.promotionCommands = section.getStringList("promotion-commands");
        this.oneTimeCommands = section.getStringList("one-time-commands");
        this.demotionCommands = section.getStringList("demotion-commands");
    }
    
    public boolean isInRange(int elo) {
        return elo >= minElo && elo <= maxElo;
    }
    
    public double getProgress(int elo) {
        if (maxElo == Integer.MAX_VALUE) {
            return 100.0; // Max rank, no progress needed
        }
        
        int range = maxElo - minElo;
        if (range <= 0) {
            return 100.0;
        }
        
        int progress = elo - minElo;
        return Math.min(100.0, Math.max(0.0, (double) progress / range * 100.0));
    }

    public String getColoredName() {
        // TODO: Implement gradient animation
        return color + name;
    }
    
    @Override
    public String toString() {
        return "Rank{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", minElo=" + minElo +
                ", maxElo=" + maxElo +
                ", color='" + color + '\'' +
                '}';
    }
}
