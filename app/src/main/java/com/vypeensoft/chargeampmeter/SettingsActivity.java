package com.vypeensoft.chargeampmeter;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vypeensoft.chargeampmeter.helpers.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private Spinner spinnerSamplingInterval;
    private Spinner spinnerDisplayUnit;
    private SwitchMaterial switchKeepScreenOn;
    private SwitchMaterial switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize PreferenceManager before super.onCreate to set dark mode if configured
        preferenceManager = new PreferenceManager(this);
        if (preferenceManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize UI Elements
        spinnerSamplingInterval = findViewById(R.id.spinner_sampling_interval);
        spinnerDisplayUnit = findViewById(R.id.spinner_display_unit);
        switchKeepScreenOn = findViewById(R.id.switch_keep_screen_on);
        switchDarkMode = findViewById(R.id.switch_dark_mode);

        setupSamplingIntervalSpinner();
        setupDisplayUnitSpinner();
        setupSwitches();
    }

    private void setupSamplingIntervalSpinner() {
        String[] intervals = {"1 second", "2 seconds", "5 seconds"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSamplingInterval.setAdapter(adapter);

        int savedInterval = preferenceManager.getSamplingInterval();
        int selectionIndex = 0;
        if (savedInterval == 2) selectionIndex = 1;
        else if (savedInterval == 5) selectionIndex = 2;
        spinnerSamplingInterval.setSelection(selectionIndex);

        spinnerSamplingInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int seconds = 1;
                if (position == 1) seconds = 2;
                else if (position == 2) seconds = 5;
                preferenceManager.setSamplingInterval(seconds);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDisplayUnitSpinner() {
        String[] units = {"mA", "A", "Both"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisplayUnit.setAdapter(adapter);

        String savedUnit = preferenceManager.getDisplayUnit();
        int selectionIndex = 2; // Default "Both"
        if ("mA".equals(savedUnit)) selectionIndex = 0;
        else if ("A".equals(savedUnit)) selectionIndex = 1;
        spinnerDisplayUnit.setSelection(selectionIndex);

        spinnerDisplayUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String unit = "Both";
                if (position == 0) unit = "mA";
                else if (position == 1) unit = "A";
                preferenceManager.setDisplayUnit(unit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSwitches() {
        switchKeepScreenOn.setChecked(preferenceManager.isKeepScreenOn());
        switchKeepScreenOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setKeepScreenOn(isChecked);
        });

        switchDarkMode.setChecked(preferenceManager.isDarkMode());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            recreate(); // Recreate activity to apply the new theme style
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to Home
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Return to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        super.onBackPressed();
        finish();
    }
}
