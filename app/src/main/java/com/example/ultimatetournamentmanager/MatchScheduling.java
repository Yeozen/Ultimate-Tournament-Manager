package com.example.ultimatetournamentmanager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MatchScheduling extends AppCompatActivity {
    private FloatingActionButton addMatch;
    private String tournamentId;

    List<String> availableTeams = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_scheduling);

        addMatch = findViewById(R.id.fabAddMatch);

        String userRole = getIntent().getStringExtra("userRole");
        if ("Participant".equals(userRole)) {
            addMatch.setVisibility(View.GONE);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("tournamentId")) {
            tournamentId = intent.getStringExtra("tournamentId");
        }

        addMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddMatchDialog();
            }
        });

        // Reference to the "matches" node under the specific tournament
        DatabaseReference matchesRef = database.getReference().child("tournaments").child(tournamentId).child("matches");

        RecyclerView recyclerView = findViewById(R.id.matchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // List to hold matches
        List<Match> matchesList = new ArrayList<>();

        // Adapter for the RecyclerView
        MatchAdapter matchAdapter = new MatchAdapter(matchesList, this, availableTeams, tournamentId, getSupportFragmentManager(), userRole);
        recyclerView.setAdapter(matchAdapter);

        // Event listener for the matches
        matchesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                matchesList.clear();

                for (DataSnapshot matchSnapshot : dataSnapshot.getChildren()) {
                    Match match = matchSnapshot.getValue(Match.class);
                    if (match != null) {
                        matchesList.add(match);
                    }
                }

                // Notify the adapter that the data has changed
                matchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });


        // Reference to the "teams" node under the specific tournament

        DatabaseReference teamsRef = database.getReference().child("tournaments").child(tournamentId).child("teams");

        teamsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through team nodes
                for (DataSnapshot teamSnapshot : dataSnapshot.getChildren()) {
                    // Assuming each team node has a child "name"
                    String teamName = teamSnapshot.child("name").getValue(String.class);

                    if (teamName != null) {
                        availableTeams.add(teamName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });


    }


    public void navigateToUpdateTournament(View view) {
        finish(); // Close the current activity
    }

    private void showAddMatchDialog() {
        // Inflate the custom layout
        View popupView = getLayoutInflater().inflate(R.layout.add_match_dialog, null);

        // Find views in the custom layout
        Spinner spinnerTeam1 = popupView.findViewById(R.id.spinnerTeam1);
        Spinner spinnerTeam2 = popupView.findViewById(R.id.spinnerTeam2);
        EditText editMatchDate = popupView.findViewById(R.id.editMatchDate);
        TextView editMatchTime = popupView.findViewById(R.id.editMatchTime);
        EditText editMatchLocation = popupView.findViewById(R.id.editMatchLocation);
        Spinner setMatchStatus = popupView.findViewById(R.id.editMatchStatus);

        editMatchTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(editMatchTime);
            }
        });


        // Populate Spinners with available teams
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableTeams);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTeam1.setAdapter(teamAdapter);
        spinnerTeam2.setAdapter(teamAdapter);

        // Define the static values
        String[] statusValues = {"Not started", "Ongoing", "Ended"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setMatchStatus.setAdapter(adapter);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView)
                .setTitle("Add Match")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the OK button click
                        String selectedTeam1 = spinnerTeam1.getSelectedItem().toString();
                        String selectedTeam2 = spinnerTeam2.getSelectedItem().toString();
                        String matchDate = editMatchDate.getText().toString();
                        String matchTime = editMatchTime.getText().toString();
                        String matchLocation = editMatchLocation.getText().toString();
                        String matchStatus = setMatchStatus.getSelectedItem().toString();

                        // Ensure all fields are filled
                        if (!selectedTeam1.isEmpty() && !selectedTeam2.isEmpty() && !matchDate.isEmpty() && !matchTime.isEmpty() && !matchLocation.isEmpty()) {
                            // Get a reference to the tournament
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
                            DatabaseReference tournamentRef = database.getReference().child("tournaments").child(tournamentId);

                            // Generate a unique key for the new match
                            String matchId = tournamentRef.child("matches").push().getKey();

                            // Create a new Match object
                            Match newMatch = new Match(matchId, selectedTeam1, selectedTeam2, matchDate, matchTime, matchLocation, matchStatus);

                            // Save the new match to the database
                            tournamentRef.child("matches").child(matchId).setValue(newMatch);
                        } else {
                            // Handle empty fields
                            Toast.makeText(MatchScheduling.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the Cancel button click
                        dialog.dismiss();
                    }
                });

        // Show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showTimePickerDialog(TextView editMatchTime) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setListener(new TimePickerFragment.TimePickerListener() {
            @Override
            public void onTimeSet(int hourOfDay, int minute) {
                // Handle the selected time in the activity
                handleTimeSet(editMatchTime, hourOfDay, minute);
            }
        });
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void handleTimeSet(TextView editMatchTime, int hourOfDay, int minute) {
        // Handle the selected time
        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);

        // Set the selected time in the editMatchTime TextView
        if (editMatchTime != null) {
            editMatchTime.setText(selectedTime);
        }
    }


    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        private TimePickerListener listener;

        interface TimePickerListener {
            void onTimeSet(int hourOfDay, int minute);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(requireActivity(), this, hour, minute, true);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (listener != null) {
                listener.onTimeSet(hourOfDay, minute);
            }
        }

        void setListener(TimePickerListener listener) {
            this.listener = listener;
        }

        TimePickerListener getListener() {
            return listener;
        }
    }
}