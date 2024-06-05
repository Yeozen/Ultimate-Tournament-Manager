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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.ViewHolder> {

    private List<Tournament> tournaments;
    private DatabaseReference databaseReference;
    Context context;
    private String userRole;

    public TournamentAdapter(List<Tournament> tournaments, DatabaseReference databaseReference, Context context, String userRole) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        this.tournaments = tournaments;
        this.databaseReference = database.getReference("tournaments");
        this.context = context;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tournament, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tournament tournament = tournaments.get(position);
        holder.bind(tournament);

        String userRole = ((AppCompatActivity)context).getIntent().getStringExtra("UserRole");

        // Set up click listener for the entire item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, UpdateTournament.class);

                intent.putExtra("TournamentId", tournament.getId());
                intent.putExtra("TournamentName", tournament.getName());
                intent.putExtra("userRole", userRole);
                context.startActivity(intent);
            }
        });

        if ("Participant".equals(userRole)) {
            holder.imgDeleteTournament.setVisibility(View.GONE);
        } else {
            holder.imgDeleteTournament.setVisibility(View.VISIBLE);
            // Set click listener for the delete icon
            holder.imgDeleteTournament.setOnClickListener(view -> {
                // Call a method to delete the tournament from the database
                deleteTournament(tournament.getId());
            });
        }
    }

    @Override
    public int getItemCount() {
        return tournaments.size();
    }

    public void setTournaments(List<Tournament> tournaments) {
        this.tournaments = tournaments;
        notifyDataSetChanged();
    }

    private void deleteTournament(String tournamentId) {
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
        public TextView textTournamentName;
        public ImageView imgDeleteTournament;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTournamentName = itemView.findViewById(R.id.textTournamentName);
            imgDeleteTournament = itemView.findViewById(R.id.imgDeleteTournament);
        }
        public void bind(Tournament tournament) {
            textTournamentName.setText(tournament.getName());
        }

    }
}
