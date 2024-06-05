package com.example.ultimatetournamentmanager;

public class Team {
    private String name;

    private String id;

    public Team(){

    }

    public Team(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public String getName() {
        return name;
    }


    public String getId() {
        return id;
    }
}
