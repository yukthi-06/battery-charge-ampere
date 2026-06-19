package com.vypeensoft.chargeampmeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.vypeensoft.chargeampmeter.helpers.ChargingMonitor;
import com.vypeensoft.chargeampmeter.helpers.PreferenceManager;
import com.vypeensoft.chargeampmeter.helpers.TimerManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private PreferenceManager preferenceManager;
    private ChargingMonitor chargingMonitor;
    private TimerManager timerManager;

    // UI Widgets
    private TextView tvUnsupportedError;
    private TextView tvCurrentValue;
    private TextView tvCurrentSubValue;
    private TextView tvAverageValue;
    private TextView tvAverageSubValue;
    private TextView tvDuration;
    private TextView tvChargerStatus;
    private TextView tvChargerSource;
    private ImageView ivStatusIcon;
    private MaterialButton btnReset;

    private final Handler samplingHandler = new Handler(Looper.getMainLooper());
    private boolean isSamplingActive = false;

    // Periodic sampling runnable
    private final Runnable samplingRunnable = new Runnable() {
        @Override
        public void run() {
            if (isSamplingActive) {
                performSampleUpdate();
                // Schedule next sample based on PreferenceManager interval
                int intervalMs = preferenceManager.getSamplingInterval() * 1000;
                samplingHandler.postDelayed(this, intervalMs);
            }
        }
    };

    // Broadcast receiver to detect charger plug/unplug events instantly
    private final BroadcastReceiver powerConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateChargerStatusUI();
            // Start or reset sampling if charger status changed
            if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
                resetMeasurementSession();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize PreferenceManager first and set dark/light theme style
        preferenceManager = new PreferenceManager(this);
        if (preferenceManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep screen on if setting is checked
        updateKeepScreenOnFlag();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize helper objects
        chargingMonitor = new ChargingMonitor(this);
        timerManager = new TimerManager((totalSeconds, formattedTime) -> {
            tvDuration.setText(formattedTime);
        });

        // Bind Views
        tvUnsupportedError = findViewById(R.id.tv_unsupported_error);
        tvCurrentValue = findViewById(R.id.tv_current_value);
        tvCurrentSubValue = findViewById(R.id.tv_current_sub_value);
        tvAverageValue = findViewById(R.id.tv_average_value);
        tvAverageSubValue = findViewById(R.id.tv_average_sub_value);
        tvDuration = findViewById(R.id.tv_duration);
        tvChargerStatus = findViewById(R.id.tv_charger_status);
        tvChargerSource = findViewById(R.id.tv_charger_source);
        ivStatusIcon = findViewById(R.id.iv_status_icon);
        btnReset = findViewById(R.id.btn_reset_measurement);

        // Setup Reset Button
        btnReset.setOnClickListener(v -> resetMeasurementSession());

        // Initial setup
        resetMeasurementSession();
    }

    private void updateKeepScreenOnFlag() {
        if (preferenceManager.isKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void startSamplingLoop() {
        if (!isSamplingActive) {
            isSamplingActive = true;
            samplingHandler.post(samplingRunnable);
        }
    }

    private void stopSamplingLoop() {
        isSamplingActive = false;
        samplingHandler.removeCallbacks(samplingRunnable);
    }

    private void performSampleUpdate() {
        // Record current sample
        double currentNow = chargingMonitor.recordSample();
        double averageCurrent = chargingMonitor.getSessionAverageCurrent();

        // Handle unsupported device error (returns NaN on current now check)
        if (Double.isNaN(currentNow) && chargingMonitor.getSampleCount() <= 1) {
            // Check if device supports battery measurements
            double testAverage = chargingMonitor.getHardwareAverageCurrent();
            if (Double.isNaN(testAverage)) {
                tvUnsupportedError.setVisibility(View.VISIBLE);
            }
        } else {
            tvUnsupportedError.setVisibility(View.GONE);
        }

        // Format and display real-time current values
        updateValueViews(currentNow, tvCurrentValue, tvCurrentSubValue);

        // Format and display average current values
        updateValueViews(averageCurrent, tvAverageValue, tvAverageSubValue);

        // Update charger status and icons
        updateChargerStatusUI();
    }

    private void updateValueViews(double currentMa, TextView mainView, TextView subView) {
        String displayUnit = preferenceManager.getDisplayUnit();

        if (Double.isNaN(currentMa)) {
            mainView.setText(R.string.unit_not_available);
            subView.setVisibility(View.GONE);
            return;
        }

        if ("mA".equals(displayUnit)) {
            mainView.setText(ChargingMonitor.formatCurrentMa(currentMa));
            subView.setVisibility(View.GONE);
        } else if ("A".equals(displayUnit)) {
            mainView.setText(ChargingMonitor.formatCurrentAmpere(currentMa));
            subView.setVisibility(View.GONE);
        } else {
            // Both
            mainView.setText(ChargingMonitor.formatCurrentMa(currentMa));
            subView.setText(ChargingMonitor.formatCurrentAmpere(currentMa));
            subView.setVisibility(View.VISIBLE);
        }
    }

    private void updateChargerStatusUI() {
        ChargingMonitor.ChargerInfo info = chargingMonitor.getChargerInfo();

        tvChargerStatus.setText(info.status);
        if (info.isConnected && info.source != null && !info.source.isEmpty()) {
            tvChargerSource.setText(info.source);
            tvChargerSource.setVisibility(View.VISIBLE);
        } else {
            tvChargerSource.setVisibility(View.GONE);
        }

        // Icon tint and status text color
        int statusColor;
        if ("Charging".equalsIgnoreCase(info.status)) {
            statusColor = ContextCompat.getColor(this, R.color.colorCharging);
        } else if ("Not Charging".equalsIgnoreCase(info.status)) {
            statusColor = ContextCompat.getColor(this, R.color.colorNotCharging);
        } else {
            statusColor = ContextCompat.getColor(this, R.color.colorDisconnected);
        }

        ivStatusIcon.setImageTintList(ColorStateList.valueOf(statusColor));
    }

    private void resetMeasurementSession() {
        chargingMonitor.clearSamples();
        timerManager.reset();
        timerManager.start();

        // Perform immediate update for visual feedback
        performSampleUpdate();

        // Restart loop
        stopSamplingLoop();
        startSamplingLoop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update user settings preferences in case they were modified
        updateKeepScreenOnFlag();
        updateChargerStatusUI();
        startSamplingLoop();
        timerManager.start();

        // Register power connection broadcast filters
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerConnectionReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSamplingLoop();
        timerManager.stop();
        try {
            unregisterReceiver(powerConnectionReceiver);
        } catch (IllegalArgumentException e) {
            // Ignore receiver not registered
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
