package com.example.mad_asgn;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class ScheduleBookingActivity extends AppCompatActivity {

    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday;
    private Button btnSelectTime, btnSaveSchedule;
    private EditText etFromLocation, etToLocation;
    private int selectedHour = 8;
    private int selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule_booking);
        
        // Apply window insets to prevent overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize views
        cbMonday = findViewById(R.id.cbMonday);
        cbTuesday = findViewById(R.id.cbTuesday);
        cbWednesday = findViewById(R.id.cbWednesday);
        cbThursday = findViewById(R.id.cbThursday);
        cbFriday = findViewById(R.id.cbFriday);
        
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        
        etFromLocation = findViewById(R.id.etFromLocation);
        etToLocation = findViewById(R.id.etToLocation);
        
        // Set up time picker dialog
        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    ScheduleBookingActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            selectedHour = hourOfDay;
                            selectedMinute = minute;
                            
                            // Update button text to show selected time
                            String timeText = String.format("%d:%02d %s", 
                                    hourOfDay > 12 ? hourOfDay - 12 : hourOfDay,
                                    minute,
                                    hourOfDay >= 12 ? "PM" : "AM");
                            btnSelectTime.setText(timeText);
                        }
                    },
                    selectedHour,
                    selectedMinute,
                    false
                );
                timePickerDialog.show();
            }
        });
        
        // Set up save button
        btnSaveSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate inputs
                if (!cbMonday.isChecked() && !cbTuesday.isChecked() && !cbWednesday.isChecked() 
                        && !cbThursday.isChecked() && !cbFriday.isChecked()) {
                    Toast.makeText(ScheduleBookingActivity.this, "Please select at least one day", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (etFromLocation.getText().toString().trim().isEmpty()) {
                    Toast.makeText(ScheduleBookingActivity.this, "Please enter starting location", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (etToLocation.getText().toString().trim().isEmpty()) {
                    Toast.makeText(ScheduleBookingActivity.this, "Please enter destination", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // In a real app, save the schedule to database
                // For demo, just show success message and finish activity
                Toast.makeText(ScheduleBookingActivity.this, "Schedule saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}