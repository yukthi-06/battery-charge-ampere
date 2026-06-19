package com.vypeensoft.chargeampmeter.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class ChargingMonitor {
    private final Context context;
    private final BatteryManager batteryManager;
    private final List<Double> samples = new ArrayList<>();

    public ChargingMonitor(Context context) {
        this.context = context;
        this.batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    /**
     * Queries the real-time battery current using BatteryManager.
     * Normalizes the value to milliamperes (mA).
     * Returns Double.NaN if information is not available or unsupported.
     */
    public double getCurrentNow() {
        if (batteryManager == null) {
            return Double.NaN;
        }

        long rawCurrent = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        
        // Android returns Long.MIN_VALUE or Integer.MIN_VALUE when not supported
        if (rawCurrent == Long.MIN_VALUE || rawCurrent == Integer.MIN_VALUE) {
            return Double.NaN;
        }

        return normalizeCurrent(rawCurrent);
    }

    /**
     * Queries the average battery current from the hardware (if available).
     * Normalizes the value to milliamperes (mA).
     */
    public double getHardwareAverageCurrent() {
        if (batteryManager == null) {
            return Double.NaN;
        }

        long rawCurrent = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
        
        if (rawCurrent == Long.MIN_VALUE || rawCurrent == Integer.MIN_VALUE) {
            return Double.NaN;
        }

        return normalizeCurrent(rawCurrent);
    }

    /**
     * Normalizes the raw current value.
     * 1. Detects uA (microamps) vs mA (milliamps).
     *    If the absolute value is > 20000, it is highly likely reported in microamperes.
     * 2. Converts negative value to positive value for charging representation.
     */
    private double normalizeCurrent(long rawCurrent) {
        double current = (double) rawCurrent;

        // Determine if value is in microamperes (uA) and convert to milliamperes (mA)
        // Usually current in mA is less than 10,000. If it's larger, it's uA.
        if (Math.abs(current) > 10000) {
            current = current / 1000.0;
        }

        // Check if charger is connected
        ChargerInfo chargerInfo = getChargerInfo();
        if (chargerInfo.isConnected) {
            // Charging: force positive
            return Math.abs(current);
        } else {
            // Discharging: force negative (representing energy leaving the battery)
            return -Math.abs(current);
        }
    }

    /**
     * Records a new sample and returns it.
     */
    public double recordSample() {
        double currentNow = getCurrentNow();
        if (!Double.isNaN(currentNow)) {
            samples.add(currentNow);
        }
        return currentNow;
    }

    /**
     * Calculates the average charging current from all samples in memory since the last reset.
     */
    public double getSessionAverageCurrent() {
        if (samples.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (double sample : samples) {
            sum += sample;
        }
        return sum / samples.size();
    }

    /**
     * Resets the collected samples.
     */
    public void clearSamples() {
        samples.clear();
    }

    public int getSampleCount() {
        return samples.size();
    }

    /**
     * Helper method to format current into mA string or A string.
     */
    public static String formatCurrentMa(double currentMa) {
        if (Double.isNaN(currentMa)) {
            return "N/A";
        }
        return String.format("%.0f mA", currentMa);
    }

    public static String formatCurrentAmpere(double currentMa) {
        if (Double.isNaN(currentMa)) {
            return "N/A";
        }
        double currentA = currentMa / 1000.0;
        return String.format("%.3f A", currentA);
    }

    /**
     * Checks if the device is currently charging or connected to a power source.
     */
    public ChargerInfo getChargerInfo() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus == null) {
            return new ChargerInfo(false, "Disconnected", "");
        }

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String source = "";
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) {
            source = "AC Charger";
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) {
            source = "USB Port";
        } else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
            source = "Wireless Charger";
        }

        String statusStr;
        if (isCharging) {
            statusStr = "Charging";
        } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            statusStr = "Not Charging";
        } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            statusStr = "Disconnected";
        } else {
            statusStr = "Unknown";
        }

        return new ChargerInfo(isCharging || chargePlug > 0, statusStr, source);
    }

    public static class ChargerInfo {
        public final boolean isConnected;
        public final String status;
        public final String source;

        public ChargerInfo(boolean isConnected, String status, String source) {
            this.isConnected = isConnected;
            this.status = status;
            this.source = source;
        }
    }
}
