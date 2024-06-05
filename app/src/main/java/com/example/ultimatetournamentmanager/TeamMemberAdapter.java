package com.example.ultimatetournamentmanager;

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

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.ViewHolder> {


    private List<TeamMember> teamMembers;
    private DatabaseReference databaseReference;
    Context context;
    private String teamId, tournamentId, userRole;

    public TeamMemberAdapter(List<TeamMember> teamMembers, String tournamentId, String teamId, Context context, String userRole) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = database.getReference("tournaments").child(tournamentId).child("teams").child(teamId).child("teamMembers");
        this.teamMembers = teamMembers;
        this.context = context;
        this.teamId = teamId;
        this.tournamentId = tournamentId;
        this.userRole = userRole;
    }

    @androidx.annotation.NonNull
    @Override
    public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
        TeamMember teamMember = teamMembers.get(position);
        holder.bind(teamMember);

        if ("Participant".equals(userRole)) {
            holder.imgDeleteTeamMember.setVisibility(View.GONE);
        } else {
            holder.imgDeleteTeamMember.setVisibility(View.VISIBLE);
            // Set click listener for the delete icon
            holder.imgDeleteTeamMember.setOnClickListener(view -> {
                // Call a method to delete the tournament from the database
                deleteTeamMember(teamMember.getId());
            });
        }
    }

    @Override
    public int getItemCount() {
        return teamMembers.size();
    }

    public void setTeamMembers(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
        notifyDataSetChanged();
    }

    private void deleteTeamMember(String tournamentId) {
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
        public TextView TeamMemberNameTextView;
        public ImageView imgDeleteTeamMember;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            TeamMemberNameTextView = itemView.findViewById(R.id.textTeamMemberName);
            imgDeleteTeamMember = itemView.findViewById(R.id.imgDeleteTeamMember);
        }

        public void bind(TeamMember teamMember) {
            TeamMemberNameTextView.setText(teamMember.getName());
        }
    }
}
