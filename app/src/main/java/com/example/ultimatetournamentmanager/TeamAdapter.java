package com.example.ultimatetournamentmanager;

// TeamAdapter.java

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.ViewHolder> {


    private List<Team> teams;
    private DatabaseReference databaseReference;
    Context context;
    private String tournamentId, userRole;


    public TeamAdapter(List<Team> teams, String tournamentId, Context context, String userRole) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = database.getReference("tournaments").child(tournamentId).child("teams");
        this.teams = teams;
        this.context = context;
        this.tournamentId = tournamentId;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.bind(team);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start CreateTournament activity when an item is clicked
                Intent intent = new Intent(context, AddTeamMembers.class);
                // Pass relevant data to the CreateTournament activity if needed
                intent.putExtra("TeamId", team.getId());
                intent.putExtra("TeamName", team.getName());
                intent.putExtra("TournamentId", tournamentId);
                intent.putExtra("userRole", userRole);
                context.startActivity(intent);
            }
        });

        if ("Participant".equals(userRole)) {
            holder.imgDeleteTeam.setVisibility(View.GONE);
        } else {
            holder.imgDeleteTeam.setVisibility(View.VISIBLE);
            // Set click listener for the delete icon
            holder.imgDeleteTeam.setOnClickListener(view -> {
                // Call a method to delete the tournament from the database
                deleteTeam(team.getId());
            });
        }
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
        notifyDataSetChanged();
    }

    private void deleteTeam(String tournamentId) {
        databaseReference.child(tournamentId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Tournament deleted successfully
                        Toast.makeText(context, "Tournament deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle the error
                        Toast.makeText(context, "Failed to delete tournament", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView teamNameTextView;
        public ImageView imgDeleteTeam;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            teamNameTextView = itemView.findViewById(R.id.textTeamName);
            imgDeleteTeam = itemView.findViewById(R.id.imgDeleteTeam);
        }

        public void bind(Team team) {
            teamNameTextView.setText(team.getName());
        }
    }
}


