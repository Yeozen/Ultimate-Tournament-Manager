package com.example.ultimatetournamentmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MatchDetails extends AppCompatActivity {

    private EditText editScorekeeperName, editPulledTeam;
    private Spinner spinnerGenderRatio;
    private TextView textTimer;
    private Button btnStartTimer, btnStopTimer, btnResetTimer;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeRemainingMillis = 0;
    private TextView team1Score, team2Score, timeoutTimeText;
    private Button btnTeam1Plus, btnTeam2Plus, btnFinishMatch;
    private TableLayout scoreTable;
    private int team1Timeouts = 0;
    private int team2Timeouts = 0;
    private ImageView team1Circle1, team1Circle2, team2Circle1, team2Circle2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_details);

        // Initialize UI elements
        editScorekeeperName = findViewById(R.id.editScorekeeperName);
        editPulledTeam = findViewById(R.id.editPulledTeam);
        spinnerGenderRatio = findViewById(R.id.spinnerGenderRatio);
        textTimer = findViewById(R.id.textTimer);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnStopTimer = findViewById(R.id.btnStopTimer);
        btnResetTimer = findViewById(R.id.btnResetTimer);
        team1Score = findViewById(R.id.team1Score);
        team2Score = findViewById(R.id.team2Score);
        btnTeam1Plus = findViewById(R.id.btnTeam1Plus);
        btnTeam2Plus = findViewById(R.id.btnTeam2Plus);
        scoreTable = findViewById(R.id.scoreTable);
        btnFinishMatch = findViewById(R.id.btnFinishMatch);
        ImageButton team1TimeoutBtn = findViewById(R.id.team1TimeoutBtn);
        ImageButton team2TimeoutBtn = findViewById(R.id.team2TimeoutBtn);
        team1Circle1 = findViewById(R.id.team1Circle1);
        team1Circle2 = findViewById(R.id.team1Circle2);
        team2Circle1 = findViewById(R.id.team2Circle1);
        team2Circle2 = findViewById(R.id.team2Circle2);
        timeoutTimeText = findViewById(R.id.timeoutTimeText);

        team1TimeoutBtn.setOnClickListener(v -> onTimeoutButtonClicked(1));
        team2TimeoutBtn.setOnClickListener(v -> onTimeoutButtonClicked(2));

        // Find TextViews in the layout
        TextView team1NameTextView = findViewById(R.id.team1Name);
        TextView team2NameTextView = findViewById(R.id.team2Name);

        // Get team names from intent data
        String team1Name = getIntent().getStringExtra("Team 1 Name");
        String team2Name = getIntent().getStringExtra("Team 2 Name");
        String tournamentId = getIntent().getStringExtra("TournamentId");


        // Set team names to TextViews
        team1NameTextView.setText(team1Name);
        team2NameTextView.setText(team2Name);


        btnTeam1Plus.setOnClickListener(v -> showScoreDialog(team1Name, 1));;
        btnTeam2Plus.setOnClickListener(v -> showScoreDialog(team2Name, 1));

        // Set up Gender Ratio Spinner
        String[] ratioValues = {"A", "B"};

        ArrayAdapter<String> ratioAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ratioValues);
        ratioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerGenderRatio.setAdapter(ratioAdapter);
        // Set up timer buttons
        btnStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        btnStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });

        btnResetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        // Set up click listener for the timer TextView
        textTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        updateTimerText();

        retrieveStoredData();

        btnFinishMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinishMatchClick(v);
            }
        });
    }

    private void onTimeoutButtonClicked(int team) {
        String team1Name = getIntent().getStringExtra("Team 1 Name");
        String team2Name = getIntent().getStringExtra("Team 2 Name");
        switch (team) {
            case 1:
                if (team1Timeouts == 0) {
                    stopTimer();
                    timeoutTimeText.setText(getTimerText());
                    showToast("First Timeout for " + team1Name);
                    team1Timeouts++;
                    team1Circle1.setImageResource(R.drawable.outline_radio_button_checked_24);
                } else if (team1Timeouts == 1) {
                    stopTimer();
                    timeoutTimeText.setText(getTimerText());
                    showToast("Second Timeout for " + team1Name);
                    team1Timeouts++;
                    team1Circle2.setImageResource(R.drawable.outline_radio_button_checked_24);
                } else {
                    showToast("No More Timeouts for " + team1Name);
                }
                break;
            case 2:
                if (team2Timeouts == 0) {
                    stopTimer();
                    timeoutTimeText.setText(getTimerText());
                    showToast("First Timeout for " + team2Name);
                    team2Timeouts++;
                    team2Circle1.setImageResource(R.drawable.outline_radio_button_checked_24);
                } else if (team2Timeouts == 1) {
                    stopTimer();
                    timeoutTimeText.setText(getTimerText());
                    showToast("Second Timeout for " + team2Name);
                    team2Timeouts++;
                    team2Circle2.setImageResource(R.drawable.outline_radio_button_checked_24);
                } else {
                    showToast("No More Timeouts for " + team2Name);
                }
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getMatchKey() {
        // Use the match ID as the key
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("MatchId")) {
            return intent.getStringExtra("MatchId");
        }
        return "";
    }

    // Method to store data in SharedPreferences
    private void storeData() {
        String matchKey = getMatchKey();
        if (!matchKey.isEmpty()) {
            SharedPreferences preferences = getSharedPreferences(matchKey, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            // Store the data
            editor.putString("scorekeeperName", editScorekeeperName.getText().toString());
            editor.putString("pulledTeam", editPulledTeam.getText().toString());
            editor.putString("genderRatio", spinnerGenderRatio.getSelectedItem().toString());
            editor.putString("timer", textTimer.getText().toString());
            editor.putString("team1Score", team1Score.getText().toString());
            editor.putString("team2Score", team2Score.getText().toString());
            editor.putInt("team1Timeout", team1Timeouts);
            editor.putInt("team2Timeout", team2Timeouts);

            // Add more fields as needed

            editor.apply();
        }
    }
    private void storeRowData(String team, String score, String assist) {
        String matchKey = getMatchKey();
        if (!matchKey.isEmpty()) {
            SharedPreferences preferences = getSharedPreferences(matchKey, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            // Increment the row count
            int rowCount = preferences.getInt("rowCount", 0);

            // Clear existing row data
            editor.remove("points_" + rowCount);
            editor.remove("score_" + rowCount);
            editor.remove("assist_" + rowCount);
            editor.remove("team_" + rowCount);

            // Store the new row data
            editor.putString("points_" + rowCount, String.format("%s - %s", team1Score.getText().toString(), team2Score.getText().toString()));
            editor.putString("score_" + rowCount, score);
            editor.putString("assist_" + rowCount, assist);
            editor.putString("team_" + rowCount, team);

            // Increment the row count
            rowCount++;
            editor.putInt("rowCount", rowCount);

            editor.apply();
        }
    }



    // Method to retrieve stored data from SharedPreferences
    private void retrieveStoredData() {
        String matchKey = getMatchKey();
        if (!matchKey.isEmpty()) {
            SharedPreferences preferences = getSharedPreferences(matchKey, MODE_PRIVATE);


            // Retrieve the data
            String scorekeeperName = preferences.getString("scorekeeperName", "");
            String pulledTeam = preferences.getString("pulledTeam", "");
            String genderRatio = preferences.getString("genderRatio", "");
            String timer = preferences.getString("timer", "");
            String team1ScoreText = preferences.getString("team1Score", "0");
            String team2ScoreText = preferences.getString("team2Score", "0");
            team1Timeouts = preferences.getInt("team1Timeout", 0);
            team2Timeouts = preferences.getInt("team2Timeout", 0);

            Log.d("team 1 timeout", String.valueOf(team1Timeouts));

            if (team1Timeouts == 1) {
                team1Circle1.setImageResource(R.drawable.outline_radio_button_checked_24);
            } else if (team1Timeouts == 2) {
                team1Circle1.setImageResource(R.drawable.outline_radio_button_checked_24);
                team1Circle2.setImageResource(R.drawable.outline_radio_button_checked_24);
            }

            if (team2Timeouts == 1) {
                team2Circle1.setImageResource(R.drawable.outline_radio_button_checked_24);
            } else if (team2Timeouts == 2) {
                team2Circle1.setImageResource(R.drawable.outline_radio_button_checked_24);
                team2Circle2.setImageResource(R.drawable.outline_radio_button_checked_24);
            }


            int rowCount = preferences.getInt("rowCount", 0);

            // Set the retrieved data to the corresponding UI elements
            editScorekeeperName.setText(scorekeeperName);
            editPulledTeam.setText(pulledTeam);
            // Set the genderRatio spinner selection
            int spinnerPosition = ((ArrayAdapter<String>) spinnerGenderRatio.getAdapter()).getPosition(genderRatio);
            spinnerGenderRatio.setSelection(spinnerPosition);
            // Handle the timer
            if (!timer.isEmpty()) {
                try {
                    String[] timeComponents = timer.split(":");
                    int minutes = Integer.parseInt(timeComponents[0]);
                    int seconds = Integer.parseInt(timeComponents[1]);
                    timeRemainingMillis = (minutes * 60 + seconds) * 1000;
                    updateTimerText();
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Handle exceptions, e.g., invalid timer format
                    e.printStackTrace();
                }
            }
            team1Score.setText(team1ScoreText);
            team2Score.setText(team2ScoreText);

            // Remove all rows from the table, except the header row at index 0
            while (scoreTable.getChildCount() > 1) {
                scoreTable.removeViewAt(1);
            }

            // Retrieve and display the stored row data
            for (int i = 0; i < rowCount; i++) {
                String points = preferences.getString("points_" + i, "");
                String score = preferences.getString("score_" + i, "");
                String assist = preferences.getString("assist_" + i, "");
                String storedTeam = preferences.getString("team_" + i, "");

                // Display the row data (you might want to adjust how you display this data)
                addSavedScoreRow(points, storedTeam, score, assist);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(timerUpdateReceiver);

        // Store data when the activity is paused
        storeData();
    }

    private void updateScore(String team, int delta) {
        int currentScore;
        String team1Name = getIntent().getStringExtra("Team 1 Name");
        String team2Name = getIntent().getStringExtra("Team 2 Name");

        if (team.equals(team1Name)) {
            currentScore = Integer.parseInt(team1Score.getText().toString());
            team1Score.setText(String.valueOf(currentScore + delta));
        } else if (team.equals(team2Name)) {
            currentScore = Integer.parseInt(team2Score.getText().toString());
            team2Score.setText(String.valueOf(currentScore + delta));

        }

        // Add logic to update the data rows based on the score changes
    }

    private void showScoreDialog(String team, int delta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.score_dialog, null);
        builder.setView(dialogView);

        EditText editScore = dialogView.findViewById(R.id.editScore);
        EditText editAssist = dialogView.findViewById(R.id.editAssist);
        String team1Name = getIntent().getStringExtra("Team 1 Name");

        builder.setPositiveButton("OK", (dialog, id) -> {
            String score = editScore.getText().toString();
            String assist = editAssist.getText().toString();
            if (team.equals(team1Name)){
                updateScore(team, delta);
            } else{
                updateScore(team, delta);
            }
            addScoreRow(team, score, assist);
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User cancelled the dialog
        });

        builder.show();
    }

    // New method to show options dialog
    // Modify the existing method to pass the TableRow to the options dialog
    private void showOptionsDialog(String team, TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options");

        // Add options to the dialog
        String[] options = {"Edit", "Delete"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Edit option
                    editScoreRow(team, row);
                    break;
                case 1:
                    // Delete option
                    deleteScoreRow(row);
                    break;
            }
        });

        builder.show();
    }

    // New method to delete the last score row for a team
    private void deleteScoreRow(TableRow row) {
        String team = ((TextView) row.getChildAt(3)).getText().toString();
        String scoreText;
        String team1Name = getIntent().getStringExtra("Team 1 Name");

        // Get the score from the row and convert it to an integer
        if (team.equals(team1Name)){
            scoreText = team1Score.getText().toString();
        }else{
            scoreText = team2Score.getText().toString();
        }

        int score = Integer.parseInt(scoreText);

        // Check if the score is greater than 0 before subtracting
        if (score > 0) {
            updateScore(team, -1);
        }

        // Remove the specific row
        scoreTable.removeView(row);

        deleteRowFromSharedPreferences(row);
    }

    private void editScoreRow(String team, TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.score_dialog, null);
        builder.setView(dialogView);

        EditText editScore = dialogView.findViewById(R.id.editScore);
        EditText editAssist = dialogView.findViewById(R.id.editAssist);

        builder.setPositiveButton("OK", (dialog, id) -> {
            String score = editScore.getText().toString();
            String assist = editAssist.getText().toString();

            // Update the values of the selected row
            updateScore(team, 0); // Add the new score (delta is 0 for editing)
            updateScoreRow(row, score, assist);
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User cancelled the dialog
        });

        builder.show();
    }

    // New method to delete the row from SharedPreferences
    private void deleteRowFromSharedPreferences(TableRow row) {
        String matchKey = getMatchKey();
        if (!matchKey.isEmpty()) {
            SharedPreferences preferences = getSharedPreferences(matchKey, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            // Decrement the row count
            int rowCount = preferences.getInt("rowCount", 0) - 1;

            // Remove the row data
            editor.remove("points_" + rowCount);
            editor.remove("score_" + rowCount);
            editor.remove("assist_" + rowCount);
            editor.remove("team_" + rowCount);

            // Decrement the row count
            editor.putInt("rowCount", rowCount);

            editor.apply();
        }
    }

    // New method to update the values of an existing score row
    private void updateScoreRow(TableRow row, String score, String assist) {
        // Update the values of the existing row
        TextView scoreTextView = (TextView) row.getChildAt(1);
        TextView assistTextView = (TextView) row.getChildAt(2);


        scoreTextView.setText(score);
        assistTextView.setText(assist);
    }

    private void addRow(String team, String score, String assist) {
        // Create a new table row
        TableRow row = new TableRow(this);

        row.setOnLongClickListener(v -> {
            // Get the team from the long-pressed row
            String longPressedTeam = ((TextView) row.getChildAt(3)).getText().toString();
            showOptionsDialog(longPressedTeam, row);
            return true; // Consume the long press event
        });

        // Add TextViews to the row
        TextView pointsTextView = new TextView(this);
        pointsTextView.setText(String.format("%s - %s", team1Score.getText().toString(), team2Score.getText().toString()));
        pointsTextView.setGravity(Gravity.CENTER);
        pointsTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        TextView scoreTextView = new TextView(this);
        scoreTextView.setText(score);
        scoreTextView.setGravity(Gravity.CENTER);
        scoreTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        TextView assistTextView = new TextView(this);
        assistTextView.setText(assist);
        assistTextView.setGravity(Gravity.CENTER);
        assistTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        TextView teamTextView = new TextView(this);
        teamTextView.setText(team);
        teamTextView.setGravity(Gravity.CENTER);
        teamTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        // Add TextViews to the row
        row.addView(pointsTextView);
        row.addView(scoreTextView);
        row.addView(assistTextView);
        row.addView(teamTextView);

        // Add the row to the table
        scoreTable.addView(row);
    }

    private void addSavedRow(String points, String team, String score, String assist) {
        // Create a new table row
        TableRow row = new TableRow(this);

        row.setOnLongClickListener(v -> {
            // Get the team from the long-pressed row
            String longPressedTeam = ((TextView) row.getChildAt(3)).getText().toString();
            showOptionsDialog(longPressedTeam, row);
            return true; // Consume the long press event
        });

        // Add TextViews to the row
        TextView pointsTextView = new TextView(this);
        pointsTextView.setText(points);
        pointsTextView.setGravity(Gravity.CENTER);
        pointsTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        TextView scoreTextView = new TextView(this);
        scoreTextView.setText(score);
        scoreTextView.setGravity(Gravity.CENTER);
        scoreTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        TextView assistTextView = new TextView(this);
        assistTextView.setText(assist);
        assistTextView.setGravity(Gravity.CENTER);
        assistTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        TextView teamTextView = new TextView(this);
        teamTextView.setText(team);
        teamTextView.setGravity(Gravity.CENTER);
        teamTextView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        // Add TextViews to the row
        row.addView(pointsTextView);
        row.addView(scoreTextView);
        row.addView(assistTextView);
        row.addView(teamTextView);

        // Add the row to the table
        scoreTable.addView(row);
    }

    private void addScoreRow(String team, String score, String assist) {
        addRow(team, score, assist);
        // Save the data to the database
        storeRowData(team, score, assist);
    }

    private void addSavedScoreRow(String points, String team, String score, String assist) {
        addSavedRow(points, team, score, assist);
    }

    private void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.timer_dialog, null);
            builder.setView(dialogView);

            NumberPicker minutePicker = dialogView.findViewById(R.id.minutePicker);
            NumberPicker secondPicker = dialogView.findViewById(R.id.secondPicker);

            // Set up the NumberPickers
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(60);
            secondPicker.setMinValue(0);
            secondPicker.setMaxValue(59);

            // Set up the dialog buttons
            builder.setPositiveButton("OK", (dialog, id) -> {
                // Handle the OK button click
                int selectedMinutes = minutePicker.getValue();
                int selectedSeconds = secondPicker.getValue();
                ((MatchDetails) getActivity()).setTimerFromDialog(selectedMinutes, selectedSeconds);
            });

            builder.setNegativeButton("Cancel", (dialog, id) -> {
                // User cancelled the dialog
            });

            // Create the AlertDialog object and return it
            return builder.create();
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Handle the time set event here
            // Update the timer with the selected time
            ((MatchDetails) getActivity()).setTimerFromDialog(hourOfDay, minute);
        }
    }

    // Method to set the timer from the dialog
    private void setTimerFromDialog(int minutes, int seconds) {
        stopTimer();
        timeRemainingMillis = (minutes * 60 + seconds) * 1000;
        updateTimerText();

        Intent serviceIntent = new Intent(this, CountdownTimerService.class);
        serviceIntent.putExtra("minutes", minutes);
        serviceIntent.putExtra("seconds", seconds);
        startService(serviceIntent);
    }

    private BroadcastReceiver timerUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long timeRemaining = intent.getLongExtra("timeRemaining", 0);
            boolean isRunning = intent.getBooleanExtra("isTimerRunning", false);

            timeRemainingMillis = timeRemaining;
            isTimerRunning = isRunning;
            updateTimerText();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(timerUpdateReceiver, new IntentFilter("timerUpdate"));
    }


    private void startTimer() {
        if (timeRemainingMillis > 0) {
            Intent serviceIntent = new Intent(this, CountdownTimerService.class);
            serviceIntent.setAction("start");
            serviceIntent.putExtra("minutes", (int) (timeRemainingMillis / 60000));
            serviceIntent.putExtra("seconds", (int) (timeRemainingMillis % 60000) / 1000);
            startService(serviceIntent);
        }
    }

    private void stopTimer() {
        Intent serviceIntent = new Intent(this, CountdownTimerService.class);
        serviceIntent.setAction("stop");
        startService(serviceIntent);
    }

    private void resetTimer() {
        Intent serviceIntent = new Intent(this, CountdownTimerService.class);
        serviceIntent.setAction("reset");
        startService(serviceIntent);
    }


    private void updateTimerText() {
        long minutes = timeRemainingMillis / 60000;
        long seconds = (timeRemainingMillis % 60000) / 1000;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        textTimer.setText(timeFormatted);
    }

    private String getTimerText() {
        long minutes = timeRemainingMillis / 60000;
        long seconds = (timeRemainingMillis % 60000) / 1000;
        String timeLeft = String.format("%02d:%02d", minutes, seconds);
        return timeLeft;
    }

    private void onFinishMatchClick(View view) {
        String tournamentId = getIntent().getStringExtra("TournamentId");
        String matchId = getIntent().getStringExtra("MatchId");

        Log.d("tournamentID", tournamentId);
        Log.d("matchId", matchId);


        // Gather data from UI elements
        String scorekeeperName = editScorekeeperName.getText().toString();
        String pulledTeam = editPulledTeam.getText().toString();
        String team1ScoreText = team1Score.getText().toString();
        String team2ScoreText = team2Score.getText().toString();
        String team1Name = getIntent().getStringExtra("Team 1 Name");
        String team2Name = getIntent().getStringExtra("Team 2 Name");

        // Pass data to ViewMatchDetails activity
        Intent intent = new Intent(MatchDetails.this, PostMatchDetails.class);
        intent.putExtra("scorekeeperName", scorekeeperName);
        intent.putExtra("pulledTeam", pulledTeam);
        intent.putExtra("team1Score", team1ScoreText);
        intent.putExtra("team2Score", team2ScoreText);
        intent.putExtra("team1Name", team1Name);
        intent.putExtra("team2Name", team2Name);
        intent.putExtra("tournamentId", tournamentId);
        intent.putExtra("matchId", matchId);



        int rowCount = scoreTable.getChildCount();
        intent.putExtra("rowCount", rowCount);
        for (int i = 1; i < rowCount; i++) {
            TableRow row = (TableRow) scoreTable.getChildAt(i);
            String team = ((TextView) row.getChildAt(3)).getText().toString();
            String score = ((TextView) row.getChildAt(1)).getText().toString();
            String assist = ((TextView) row.getChildAt(2)).getText().toString();
            String points = ((TextView) row.getChildAt(0)).getText().toString();
            Log.d("MatchDetails", "Row " + i + ": Team=" + team + ", Score=" + score + ", Assist=" + assist + ", Points=" + points);
            intent.putExtra("team" + i, team);
            intent.putExtra("score" + i, score);
            intent.putExtra("assist" + i, assist);
            intent.putExtra("points" + i, points);
        }

        startActivity(intent);

    }


}