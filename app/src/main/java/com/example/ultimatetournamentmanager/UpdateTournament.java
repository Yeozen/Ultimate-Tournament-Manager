package com.example.ultimatetournamentmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UpdateTournament extends AppCompatActivity {

    private EditText editTournamentName;
    private Button btnUpdate;
    private FloatingActionButton addTeam;
    private String tournament;
    private String tournamentId;
    private RecyclerView teamsRecyclerView;
    private TeamAdapter teamAdapter;
    private List<Team> teams;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_tournament);

        editTournamentName = findViewById(R.id.editTournamentName);
        btnUpdate = findViewById(R.id.btnUpdate);
        addTeam = findViewById(R.id.fabAddTeam);

        // Fetch the tournament data from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TournamentName")) {
            tournament = intent.getStringExtra("TournamentName");
            if (tournament != null) {
                // Set the existing tournament name in the EditText
                editTournamentName.setText(tournament);
            }
        }

        if (intent != null && intent.hasExtra("TournamentId")) {
            tournamentId = intent.getStringExtra("TournamentId");
        }

        String userRole = getIntent().getStringExtra("userRole");

        if ("Participant".equals(userRole)) {
            editTournamentName.setEnabled(false);
            // Hide delete icons, update button, and add teams button
            // Make sure to set visibility to GONE if you want to reclaim the space
            btnUpdate.setVisibility(View.GONE);
            addTeam.setVisibility(View.GONE);
        }

        addTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTeamDialog();
            }
        });

        teamsRecyclerView = findViewById(R.id.teamsRecyclerView);
        teamAdapter = new TeamAdapter(teams, tournamentId, this, userRole);
        teamsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        teamsRecyclerView.setAdapter(teamAdapter);

        DatabaseReference teamsRef = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("tournaments").child(tournamentId).child("teams");
        if (tournamentId != null) {
            teamsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Team> teams = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Team team = dataSnapshot.getValue(Team.class);
                        teams.add(team);
                    }
                    teamAdapter.setTeams(teams);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Failed to read value.", error.toException());
                }
            });
        }
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle Finalize button click, update tournament details and return to MainActivity
                String updatedName = editTournamentName.getText().toString().trim();
                if (!updatedName.isEmpty()) {
                    updateTournamentInDatabase(updatedName);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("UPDATED_TOURNAMENT", String.valueOf(tournament));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(UpdateTournament.this, "Please enter a valid tournament name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateTournamentInDatabase(String updatedName) {
        DatabaseReference tournamentsRef = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("tournaments");
        DatabaseReference tournamentToUpdateRef = tournamentsRef.child(tournamentId).child("name");

        tournamentToUpdateRef.setValue(updatedName);
    }

    private void saveTeamToDatabase(String tournamentId, String teamName) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference teamsRef = database.getReference("tournaments").child(tournamentId).child("teams");

        String teamId = teamsRef.push().getKey();

        Team team = new Team(teamId, teamName);

        assert teamId != null;
        // Save the team under the corresponding tournament's "teams" node
        teamsRef.child(teamId).setValue(team);
    }

    private void showAddTeamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Team");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Team Name");

        // Set the input type to text
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add the team name to the teamsList
                String team = input.getText().toString().trim();
                saveTeamToDatabase(tournamentId, team);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    public void navigateToMatchScheduling(View view) {
        String userRole = getIntent().getStringExtra("userRole");
        // Handle navigation to Match Scheduling activity
        Intent intent = new Intent(this, MatchScheduling.class);
        intent.putExtra("userRole", userRole);
        intent.putExtra("tournamentId", tournamentId);
        startActivity(intent);
    }
}
