package com.example.ultimatetournamentmanager;

public class TeamMember {
    private String name;
    private String teamId;
    private String id;

    public TeamMember(){

    }

    public TeamMember(String id, String name) {
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
