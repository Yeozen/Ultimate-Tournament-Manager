package com.example.ultimatetournamentmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.support.v4.app.INotificationSideChannel;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddTeamMembers extends AppCompatActivity {
    private EditText editTeamName;
    private Button btnUpdate;
    private FloatingActionButton addTeamMembers;
    private String teamName;
    private String teamId;
    private String tournamentId;
    private RecyclerView teamMembersRecyclerView;
    private TeamMemberAdapter teamMemberAdapter;
    private List<TeamMember> teamMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_add_team_members);

        editTeamName = findViewById(R.id.editTeamName);
        btnUpdate = findViewById(R.id.btnUpdate);
        addTeamMembers = findViewById(R.id.fabAddTeamMember);

        String userRole = getIntent().getStringExtra("userRole");

        if ("Participant".equals(userRole)) {
            editTeamName.setEnabled(false);
            // Hide delete icons, update button, and add teams button
            // Make sure to set visibility to GONE if you want to reclaim the space
            btnUpdate.setVisibility(View.GONE);
            addTeamMembers.setVisibility(View.GONE);
        }

        // Fetch the tournament data from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TeamName")) {
            teamName = intent.getStringExtra("TeamName");
            if (teamName != null) {
                // Set the existing tournament name in the EditText
                editTeamName.setText(teamName);
            }
        }

        if (intent != null && intent.hasExtra("TeamId")) {
            teamId = intent.getStringExtra("TeamId");
        }

        if (intent != null && intent.hasExtra("TournamentId")) {
            tournamentId = intent.getStringExtra("TournamentId");
        }

        addTeamMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTeamMemberDialog();
            }
        });

        teamMembersRecyclerView = findViewById(R.id.teamMembersRecyclerView);
        teamMemberAdapter = new TeamMemberAdapter(teamMembers, tournamentId, teamId, this, userRole);
        teamMembersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        teamMembersRecyclerView.setAdapter(teamMemberAdapter);

        DatabaseReference teamNamesRef = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("tournaments").child(tournamentId).child("teams").child(teamId).child("teamMembers");
        if (teamId != null) {
            teamNamesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<TeamMember> teamMembers = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        TeamMember teamMember = dataSnapshot.getValue(TeamMember.class);
                        teamMembers.add(teamMember);
                    }
                    teamMemberAdapter.setTeamMembers(teamMembers);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle Finalize button click, update tournament details and return to MainActivity
                String updatedName = editTeamName.getText().toString().trim();
                if (!updatedName.isEmpty()) {
                    updateTeamNameInDatabase(updatedName);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("UPDATED_TOURNAMENT", String.valueOf(teamName));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(AddTeamMembers.this, "Please enter a valid team name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateTeamNameInDatabase(String updatedName) {
        DatabaseReference tournamentsRef = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("tournaments");
        DatabaseReference tournamentToUpdateRef = tournamentsRef.child(tournamentId).child("teams").child(teamId).child("name");

        tournamentToUpdateRef.setValue(updatedName);
    }

    private void saveTeamMemberToDatabase(String tournamentId, String teamId, String teamMemberName) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference teamMembersRef = database.getReference("tournaments").child(tournamentId).child("teams").child(teamId).child("teamMembers");
        Log.println(Log.INFO, "team id", teamId);
        String teamMembersId = teamMembersRef.push().getKey();
        Log.println(Log.INFO, "teamMember id", teamMembersId);
        Log.println(Log.INFO, "teamMember name", teamMemberName);

        TeamMember teamMember = new TeamMember(teamMembersId, teamMemberName);

        assert teamMembersId != null;
        // Save the team under the corresponding tournament's "teams" node
        teamMembersRef.child(teamMembersId).setValue(teamMember);
    }

    private void showAddTeamMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Team Member");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Team Member Name");

        // Set the input type to text
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add the team name to the teamsList
                String teamMember = input.getText().toString().trim();
                saveTeamMemberToDatabase(tournamentId, teamId, teamMember);
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
}
