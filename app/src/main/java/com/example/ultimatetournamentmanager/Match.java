package com.example.ultimatetournamentmanager;

public class Match {

    private String team1;
    private String team2;
    private String date;
    private String time;
    private String location;
    private String id;
    private String status;


    public Match(){

    }

    public Match(String id, String team1, String team2, String date, String time, String location, String status) {
        this.id = id;
        this.team1 = team1;
        this.team2 = team2;
        this.date = date;
        this.time = time;
        this.location = location;
        this.status = status;
    }


    public String getTeam1() {
        return team1;
    }
    public String getTeam2() {
        return team2;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getLocation() {
        return location;
    }
    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }
}
