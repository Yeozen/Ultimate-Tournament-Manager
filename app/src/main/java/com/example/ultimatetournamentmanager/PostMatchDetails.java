package com.example.ultimatetournamentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostMatchDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_match_details);

        EditText team1MaleMVP = findViewById(R.id.team1MaleMVP);
        EditText team1FemaleMVP = findViewById(R.id.team1FemaleMVP);
        EditText team1SpiritScore = findViewById(R.id.team1SpiritScore);

        EditText team2MaleMVP = findViewById(R.id.team2MaleMVP);
        EditText team2FemaleMVP = findViewById(R.id.team2FemaleMVP);
        EditText team2SpiritScore = findViewById(R.id.team2SpiritScore);




        // Retrieve data from Intent
        Bundle extras = getIntent().getExtras();
        Button btnSubmitMatchDetails = null;
        if (extras != null) {
            // Retrieve data from Intent
            String scorekeeperName = extras.getString("scorekeeperName");
            String pulledTeam = extras.getString("pulledTeam");
            String team1Name = extras.getString("team1Name");
            String team2Name = extras.getString("team2Name");
            String team1Score = extras.getString("team1Score");
            String team2Score = extras.getString("team2Score");

            // Display the data in TextViews
            TextView scorekeeperNameTextView = findViewById(R.id.scorekeeperNameTextView);
            TextView pulledTeamTextView = findViewById(R.id.pulledTeamTextView);
            TextView team1NameTextView = findViewById(R.id.team1Name);
            TextView team2NameTextView = findViewById(R.id.team2Name);
            TextView team1ScoreTextView = findViewById(R.id.team1Score);
            TextView team2ScoreTextView = findViewById(R.id.team2Score);
            btnSubmitMatchDetails = findViewById(R.id.btnSubmitMatchDetails);

            scorekeeperNameTextView.setText("Scorekeeper Name: " + scorekeeperName);
            pulledTeamTextView.setText("Team that Pulled First: " + pulledTeam);
            team1NameTextView.setText(team1Name);
            team2NameTextView.setText(team2Name);
            team1ScoreTextView.setText(team1Score);
            team2ScoreTextView.setText(team2Score);
            team1MaleMVP.setHint(team1Name + " Male MVP");
            team1FemaleMVP.setHint(team1Name + " Female MVP");
            team1SpiritScore.setHint(team1Name + " Spirit Score");

            team2MaleMVP.setHint(team2Name + " Male MVP");
            team2FemaleMVP.setHint(team2Name + " Female MVP");
            team2SpiritScore.setHint(team2Name + " Spirit Score");

            // Retrieve and display the score rows data
            TableLayout tableLayout = findViewById(R.id.postMatchTable);
            int rowCount = extras.getInt("rowCount");

            // Retrieve and display the score rows data
            for (int i = 1; i <= rowCount; i++) {
                String team = extras.getString("team" + i);
                String score = extras.getString("score" + i);
                String assist = extras.getString("assist" + i);
                String points = extras.getString("points" + i);

                // Now you have the individual data, you can use it as needed
                addRow(points, score, assist, team);
            }
        }
        btnSubmitMatchDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSubmitMatchDetails(v);
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("PostMatchDetails", "Back key pressed");
            return super.onKeyDown(keyCode, event);
        }
        return false;
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

    private void btnSubmitMatchDetails(View view) {
        Bundle extras = getIntent().getExtras();

        String scorekeeperName = extras.getString("scorekeeperName");
        String pulledTeam = extras.getString("pulledTeam");
        TextView team1NameTextView = findViewById(R.id.team1Name);
        TextView team2NameTextView = findViewById(R.id.team2Name);
        TextView team1ScoreTextView = findViewById(R.id.team1Score);
        TextView team2ScoreTextView = findViewById(R.id.team2Score);
        TableLayout tableLayout = findViewById(R.id.postMatchTable);
        EditText team1MaleMVP = findViewById(R.id.team1MaleMVP);
        EditText team1FemaleMVP = findViewById(R.id.team1FemaleMVP);
        EditText team1SpiritScore = findViewById(R.id.team1SpiritScore);
        EditText team2MaleMVP = findViewById(R.id.team2MaleMVP);
        EditText team2FemaleMVP = findViewById(R.id.team2FemaleMVP);
        EditText team2SpiritScore = findViewById(R.id.team2SpiritScore);
        String tournamentId = getIntent().getStringExtra("tournamentId");
        String matchId = getIntent().getStringExtra("matchId");

        // Gather data from UI elements
        String team1ScoreText = team1ScoreTextView.getText().toString();
        String team2ScoreText = team2ScoreTextView.getText().toString();
        String team1Name = team1NameTextView.getText().toString();
        String team2Name = team2NameTextView.getText().toString();
        String team1MaleMVPText = team1MaleMVP.getText().toString();
        String team1FemaleMVPText = team1FemaleMVP.getText().toString();
        String team1SpiritScoreText = team1SpiritScore.getText().toString();
        String team2MaleMVPText = team2MaleMVP.getText().toString();
        String team2FemaleMVPText = team2FemaleMVP.getText().toString();
        String team2SpiritScoreText = team2SpiritScore.getText().toString();

        // Create a Firebase Database instance
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");

        // Get a reference to the match details node based on the match ID
        DatabaseReference matchDetailsRef = database.getReference().child("tournaments").child(tournamentId).child("matches").child(matchId);

        // Create a new HashMap to store the match details data
        HashMap<String, Object> matchDetailsData = new HashMap<>();
        matchDetailsData.put("scorekeeperName", scorekeeperName);
        matchDetailsData.put("pulledTeam", pulledTeam);
        matchDetailsData.put(team1Name + " Score", team1ScoreText);
        matchDetailsData.put(team2Name + " Score", team2ScoreText);
        // Add Team 1 MVPs and Spirit Score to the match details data
        matchDetailsData.put(team1Name + " Male MVP", team1MaleMVPText);
        matchDetailsData.put(team1Name + " Female MVP", team1FemaleMVPText);
        matchDetailsData.put(team1Name + " Spirit Score", team1SpiritScoreText);

        // Add Team 2 MVPs and Spirit Score to the match details data
        matchDetailsData.put(team2Name + " Male MVP", team2MaleMVPText);
        matchDetailsData.put(team2Name + " Female MVP", team2FemaleMVPText);
        matchDetailsData.put(team2Name + " Spirit Score", team2SpiritScoreText);

        // Score rows data
        List<HashMap<String, Object>> scoreRowsData = new ArrayList<>();

        // Iterate through the score rows in the table and extract the data
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            // Get the current row
            TableRow row = (TableRow) tableLayout.getChildAt(i);

            // Extract the data from the row's TextViews
            String team = ((TextView) row.getChildAt(3)).getText().toString();
            String score = ((TextView) row.getChildAt(1)).getText().toString();
            String assist = ((TextView) row.getChildAt(2)).getText().toString();
            String points = ((TextView) row.getChildAt(0)).getText().toString();

            // Create a HashMap to store the score row data
            HashMap<String, Object> scoreRowData = new HashMap<>();
            scoreRowData.put("points", points);
            scoreRowData.put("team", team);
            scoreRowData.put("score", score);
            scoreRowData.put("assist", assist);

            // Add the score row data to the ArrayList
            scoreRowsData.add(scoreRowData);
        }

        // Save the match details and score rows data to the database
        matchDetailsRef.child("matchDetails").setValue(matchDetailsData);
        matchDetailsRef.child("scoreRows").setValue(scoreRowsData);

        // Show a confirmation message
        Toast.makeText(this, "Match details saved successfully", Toast.LENGTH_SHORT).show();
    }
}

