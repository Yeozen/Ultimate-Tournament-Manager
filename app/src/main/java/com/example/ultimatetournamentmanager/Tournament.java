package com.example.ultimatetournamentmanager;
public class Tournament {
    private String id;
    private String name;

    public Tournament() {
        // Required default constructor for Firebase
    }

    public Tournament(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}


