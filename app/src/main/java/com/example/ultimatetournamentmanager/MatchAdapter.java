package com.example.ultimatetournamentmanager;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {

    private List<Match> matches;
    private Context context;
    private List<String> availableTeams;
    private String tournamentId, userRole;
    private FragmentManager fragmentManager;

    public MatchAdapter(List<Match> matches, Context context, List<String> availableTeams, String tournamentId, FragmentManager fragmentManager, String userRole) {
        this.matches = matches;
        this.context = context;
        this.availableTeams = availableTeams;
        this.tournamentId = tournamentId;
        this.fragmentManager = fragmentManager;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Match match = matches.get(position);

        // Set team names
        holder.textTeam1.setText(match.getTeam1());
        holder.textTeam2.setText(match.getTeam2());
        holder.textMatchLocation.setText(match.getLocation());
        holder.textMatchDate.setText(match.getDate());
        holder.textMatchTime.setText(match.getTime());
        holder.textMatchStatus.setText(match.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SavedPostMatchDetails.class);

                intent.putExtra("TournamentId", tournamentId);
                intent.putExtra("MatchId", match.getId());
                intent.putExtra("Team 1 Name", match.getTeam1());
                intent.putExtra("Team 2 Name", match.getTeam2());
                intent.putExtra("userRole", userRole);
                context.startActivity(intent);
            }
        });

        if ("Participant".equals(userRole)) {
            holder.moreOptionsBtn.setVisibility(View.GONE);
        } else {
            holder.moreOptionsBtn.setVisibility(View.VISIBLE);
            // Set click listener for the delete icon
            holder.moreOptionsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show a popup menu
                    PopupMenu popupMenu = new PopupMenu(context, holder.moreOptionsBtn);
                    popupMenu.getMenuInflater().inflate(R.menu.match_options_menu, popupMenu.getMenu());

                    // Set item click listener for the popup menu
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();

                            if (itemId == R.id.menuUpdate) {
                                // Handle update option
                                showEditMatchDialog(match);
                                return true;
                            } else if (itemId == R.id.menuDelete) {
                                // Handle delete option
                                deleteMatch(match);
                                return true;
                            } else if (itemId == R.id.menuEditMatchDetails) {
                                // Handle edit match details option
                                startEditMatchDetailsActivity(match);
                                return true;
                            } else {
                                return false;
                            }
                        }

                    });

                    // Show the popup menu
                    popupMenu.show();
                }
            });
        }
    }

    private void startEditMatchDetailsActivity(Match match) {
        Intent intent = new Intent(context, MatchDetails.class);

        intent.putExtra("TournamentId", tournamentId);
        intent.putExtra("MatchId", match.getId());
        intent.putExtra("Team 1 Name", match.getTeam1());
        intent.putExtra("Team 2 Name", match.getTeam2());

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    private void deleteMatch(Match match) {
        // Implement the logic to delete the match from the database
        // Get a reference to the tournament
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference tournamentRef = database.getReference().child("tournaments").child(tournamentId).child("matches").child(match.getId());

        // Remove the match from the database
        tournamentRef.removeValue();
    }

    private void showEditMatchDialog(Match match) {
        // Inflate the custom layout
        View popupView = LayoutInflater.from(context).inflate(R.layout.add_match_dialog, null);

        // Find views in the custom layout
        Spinner spinnerTeam1 = popupView.findViewById(R.id.spinnerTeam1);
        Spinner spinnerTeam2 = popupView.findViewById(R.id.spinnerTeam2);
        EditText editMatchDate = popupView.findViewById(R.id.editMatchDate);
        TextView editMatchTime = popupView.findViewById(R.id.editMatchTime);
        EditText editMatchLocation = popupView.findViewById(R.id.editMatchLocation);
        Spinner spinnerMatchStatus = popupView.findViewById(R.id.editMatchStatus);

        editMatchTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(editMatchTime);
            }
        });

        // Populate Spinners with available teams
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, availableTeams);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTeam1.setAdapter(teamAdapter);
        spinnerTeam2.setAdapter(teamAdapter);

        String[] statusValues = {"Not started", "Ongoing", "Ended"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statusValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerMatchStatus.setAdapter(adapter);

        // Set the previous information
        spinnerTeam1.setSelection(teamAdapter.getPosition(match.getTeam1()));
        spinnerTeam2.setSelection(teamAdapter.getPosition(match.getTeam2()));
        editMatchDate.setText(match.getDate());
        editMatchTime.setText(match.getTime());
        editMatchLocation.setText(match.getLocation());
        spinnerMatchStatus.setSelection(adapter.getPosition(match.getStatus()));

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popupView)
                .setTitle("Edit Match")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the OK button click
                        String selectedTeam1 = spinnerTeam1.getSelectedItem().toString();
                        String selectedTeam2 = spinnerTeam2.getSelectedItem().toString();
                        String updatedMatchDate = editMatchDate.getText().toString();
                        String updatedMatchTime = editMatchTime.getText().toString();
                        String updatedMatchLocation = editMatchLocation.getText().toString();
                        String updatedMatchStatus = spinnerMatchStatus.getSelectedItem().toString();

                        // Ensure all fields are filled
                        if (!selectedTeam1.isEmpty() && !selectedTeam2.isEmpty() && !updatedMatchDate.isEmpty() && !updatedMatchTime.isEmpty() && !updatedMatchLocation.isEmpty()) {
                            // Get a reference to the tournament
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://mobile-device-programming-app-default-rtdb.asia-southeast1.firebasedatabase.app");
                            DatabaseReference tournamentRef = database.getReference().child("tournaments").child(tournamentId).child("matches").child(match.getId());

                            // Update the match in the database
                            tournamentRef.child("team1").setValue(selectedTeam1);
                            tournamentRef.child("team2").setValue(selectedTeam2);
                            tournamentRef.child("date").setValue(updatedMatchDate);
                            tournamentRef.child("time").setValue(updatedMatchTime);
                            tournamentRef.child("location").setValue(updatedMatchLocation);
                            tournamentRef.child("status").setValue(updatedMatchStatus);

                        } else {
                            // Handle empty fields
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTeam1, textTeam2, textMatchLocation, textMatchDate, textMatchTime, textMatchStatus;
        ImageButton moreOptionsBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textTeam1 = itemView.findViewById(R.id.textTeam1);
            textTeam2 = itemView.findViewById(R.id.textTeam2);
            textMatchLocation = itemView.findViewById(R.id.textMatchLocation);
            textMatchDate = itemView.findViewById(R.id.textMatchDate);
            textMatchTime = itemView.findViewById(R.id.textMatchTime);
            moreOptionsBtn = itemView.findViewById(R.id.moreOptionsBtn);
            textMatchStatus = itemView.findViewById(R.id.textMatchStatus);
        }
    }

    private void showTimePickerDialog(TextView editMatchTime) {
        MatchAdapter.TimePickerFragment timePickerFragment = new MatchAdapter.TimePickerFragment();
        timePickerFragment.setListener(new MatchAdapter.TimePickerFragment.TimePickerListener() {
            @Override
            public void onTimeSet(int hourOfDay, int minute) {
                // Handle the selected time in the activity
                handleTimeSet(editMatchTime, hourOfDay, minute);
            }
        });
        timePickerFragment.show(fragmentManager, "timePicker");
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

        private MatchAdapter.TimePickerFragment.TimePickerListener listener;

        interface TimePickerListener {
            void onTimeSet(int hourOfDay, int minute);
        }

        @com.example.ultimatetournamentmanager.NonNull
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

        void setListener(MatchAdapter.TimePickerFragment.TimePickerListener listener) {
            this.listener = listener;
        }

        MatchAdapter.TimePickerFragment.TimePickerListener getListener() {
            return listener;
        }
    }
}

