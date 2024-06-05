package com.example.ultimatetournamentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SavedPostMatchDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_post_match_details);

        Bundle extras = getIntent().getExtras();
        String tournamentId = extras.getString("TournamentId");
        String matchId = extras.getString("MatchId");
        String team1Name = extras.getString("Team 1 Name");
        String team2Name = extras.getString("Team 2 Name");

        // Get a reference to the match details node based on the match ID
        DatabaseReference matchDetailsRef = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app").getReference()
                .child("tournaments").child(tournamentId).child("matches").child(matchId);

        // Read data from the database
        matchDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Extract match details data
                    String team1Score = dataSnapshot.child("matchDetails").child(team1Name + " Score").getValue(String.class);
                    String team2Score = dataSnapshot.child("matchDetails").child(team1Name + " Score").getValue(String.class);
                    String scorekeeperName = dataSnapshot.child("matchDetails").child("scorekeeperName").getValue(String.class);
                    String pulledTeam = dataSnapshot.child("matchDetails").child("pulledTeam").getValue(String.class);

                    // Populate the score display section
                    TextView team1NameTextView = findViewById(R.id.team1Name);
                    TextView team2NameTextView = findViewById(R.id.team2Name);
                    TextView team1ScoreTextView = findViewById(R.id.team1Score);
                    TextView team2ScoreTextView = findViewById(R.id.team2Score);
                    TextView scorekeeperNameTextView = findViewById(R.id.scorekeeperNameTextView);
                    TextView pulledTeamTextView = findViewById(R.id.pulledTeamTextView);
                    TextView team1MaleMVP = findViewById(R.id.team1MaleMVP);
                    TextView team1FemaleMVP = findViewById(R.id.team1FemaleMVP);
                    TextView team1SpiritScore = findViewById(R.id.team1SpiritScore);
                    TextView team2MaleMVP = findViewById(R.id.team2MaleMVP);
                    TextView team2FemaleMVP = findViewById(R.id.team2FemaleMVP);
                    TextView team2SpiritScore = findViewById(R.id.team2SpiritScore);

                    team1NameTextView.setText(team1Name);
                    team2NameTextView.setText(team2Name);
                    team1ScoreTextView.setText(team1Score);
                    team2ScoreTextView.setText(team2Score);
                    scorekeeperNameTextView.setText("Scorekeeper Name: " + scorekeeperName);
                    pulledTeamTextView.setText("Team that Pulled First: " + pulledTeam);
                    team1MaleMVP.setText(team1Name + " Male MVP: " + dataSnapshot.child("matchDetails").child(team1Name + " Male MVP").getValue(String.class));
                    team1FemaleMVP.setText(team1Name + " Female MVP: " + dataSnapshot.child("matchDetails").child(team1Name + " Female MVP").getValue(String.class));
                    team1SpiritScore.setText(team1Name + " Spirit Score: " + dataSnapshot.child("matchDetails").child(team1Name + " Spirit Score").getValue(String.class));
                    team2MaleMVP.setText(team2Name + " Male MVP: " + dataSnapshot.child("matchDetails").child(team2Name + " Male MVP").getValue(String.class));
                    team2FemaleMVP.setText(team2Name + " Female MVP: " + dataSnapshot.child("matchDetails").child(team2Name + " Female MVP").getValue(String.class));
                    team2SpiritScore.setText(team2Name + " Spirit Score: " + dataSnapshot.child("matchDetails").child(team2Name + " Spirit Score").getValue(String.class));

                    // Populate additional details
                    populateAdditionalDetails(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void populateAdditionalDetails(DataSnapshot matchDetailsSnapshot) {


        // Retrieve and display the score rows data
        TableLayout tableLayout = findViewById(R.id.postMatchTable);

        DataSnapshot scoreRowsSnapshot = matchDetailsSnapshot.child("scoreRows");

        for (DataSnapshot scoreSnapshot : scoreRowsSnapshot.getChildren()) {
            // Extract the data from the score row
            String points = scoreSnapshot.child("points").getValue(String.class);
            String score = scoreSnapshot.child("score").getValue(String.class);
            String assist = scoreSnapshot.child("assist").getValue(String.class);
            String team = scoreSnapshot.child("team").getValue(String.class);

            Log.d("row", points + score + assist + team);

            // Now you have the individual data, you can use it as needed
            addRow(points, score, assist, team);
        }
    }

    private void addRow(String points, String score, String assist, String team) {
        // Create a new table row
        TableRow row = new TableRow(this);

        // Add TextViews to the row
        addTextViewToRow(row, points);
        addTextViewToRow(row, score);
        addTextViewToRow(row, assist);
        addTextViewToRow(row, team);

        // Add the row to the table
        TableLayout tableLayout = findViewById(R.id.postMatchTable);
        tableLayout.addView(row);
    }

    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(8, 8, 8, 8);
        row.addView(textView);
    }

}
