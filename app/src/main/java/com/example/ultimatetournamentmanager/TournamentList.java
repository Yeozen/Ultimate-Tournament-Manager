package com.example.ultimatetournamentmanager;

import static com.example.ultimatetournamentmanager.R.*;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.INotificationSideChannel;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TournamentList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TournamentAdapter tournamentAdapter;
    private List<Tournament> tournaments;
    private FloatingActionButton fabCreateTournament;
    private Button logoutButton;
    private static final int REQUEST_CREATE_TOURNAMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_tournament_list);

        recyclerView = findViewById(id.tournamentsRecyclerView);
        fabCreateTournament = findViewById(id.fabAddTournament);
        logoutButton = findViewById(id.logout);
        String userRole = getIntent().getStringExtra("UserRole");

        if ("Participant".equals(userRole)) {
            // Hide the logout button and fabAddTournament button for participants
            logoutButton.setVisibility(View.GONE);
            fabCreateTournament.setVisibility(View.GONE);

        } else {
            // Show the logout button and fabAddTournament button for administrators
            logoutButton.setVisibility(View.VISIBLE);
            fabCreateTournament.setVisibility(View.VISIBLE);
        }

        // Fetch data from Firebase
        DatabaseReference tournamentsRef = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("tournaments");

        tournaments = new ArrayList<>();
        tournamentAdapter = new TournamentAdapter(new ArrayList<>(), tournamentsRef, this, userRole);
        recyclerView.setAdapter(tournamentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tournamentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Tournament> tournaments = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Tournament tournament = dataSnapshot.getValue(Tournament.class);
                    tournaments.add(tournament);
                    Log.println(Log.INFO,"cibai", tournaments.toString());
                }
                tournamentAdapter.setTournaments(tournaments);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read value.", error.toException());
            }
        });

        fabCreateTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTournamentDialog();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showAddTournamentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Tournament");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Tournament Name");

        // Set the input type to text
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add the team name to the teamsList
                String tournamentName = input.getText().toString().trim();
                saveTournamentToDatabase(tournamentName);
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

    private void saveTournamentToDatabase(String tournamentName) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference tournamentsRef = database.getReference("tournaments");

        String tournamentId = tournamentsRef.push().getKey();

        Tournament tournament = new Tournament(tournamentId, tournamentName);

        assert tournamentId != null;
        tournamentsRef.child("").child(tournamentId).setValue(tournament);

        Toast.makeText(this, "Tournament and teams saved", Toast.LENGTH_SHORT).show();
    }
}

