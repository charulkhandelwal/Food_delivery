package com.example.food_delivery.Activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_delivery.R;
import com.example.food_delivery.databinding.ActivityAskForLeaveBinding;

import java.util.Calendar;
public class AskForLeave extends AppCompatActivity {

    private ActivityAskForLeaveBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAskForLeaveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSpinners();
        setupDatePickers();
        setupTabs();
        setupListeners();
    }

    private void setupSpinners() {
        String[] daysOptions = {"Select", "1 Day", "2 Days", "3 Days", "1 Week"};
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, daysOptions);
        binding.spinnerDays.setAdapter(daysAdapter);

        String[] reasons = {"Select", "Sick Leave", "Casual Leave", "Emergency", "Other"};
        ArrayAdapter<String> reasonAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, reasons);
        binding.spinnerReason.setAdapter(reasonAdapter);
    }

    private void setupDatePickers() {
        binding.etFromDate.setOnClickListener(v -> showDatePicker(binding.etFromDate));
        binding.etToDate.setOnClickListener(v -> showDatePicker(binding.etToDate));
    }

    private void showDatePicker(final android.widget.EditText target) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String date = String.format("%02d/%02d/%04d",
                            selectedDay, (selectedMonth + 1), selectedYear);
                    target.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void setupTabs() {
        binding.tabNewApplication.setOnClickListener(v -> {
            binding.tabNewApplication.setTextColor(getResources().getColor(R.color.red));
            binding.tabMyApplication.setTextColor(getResources().getColor(R.color.light_grey));
        });

        binding.tabMyApplication.setOnClickListener(v -> {
            binding.tabMyApplication.setTextColor(getResources().getColor(R.color.red));
            binding.tabNewApplication.setTextColor(getResources().getColor(R.color.light_grey));
        });
    }

    private void setupListeners() {
        binding.ivBack.setOnClickListener(v -> onBackPressed());

        binding.btnSubmit.setOnClickListener(v -> {
            String days = binding.spinnerDays.getSelectedItem().toString();
            String fromDate = binding.etFromDate.getText().toString();
            String toDate = binding.etToDate.getText().toString();
            String reason = binding.spinnerReason.getSelectedItem().toString();
            String comments = binding.etComments.getText().toString();

            if (days.equals("Select") || fromDate.isEmpty() || toDate.isEmpty() || reason.equals("Select")) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this,
                    "Leave Submitted:\nDays: " + days +
                            "\nFrom: " + fromDate +
                            "\nTo: " + toDate +
                            "\nReason: " + reason +
                            "\nComments: " + comments,
                    Toast.LENGTH_LONG).show();
        });
    }
}
