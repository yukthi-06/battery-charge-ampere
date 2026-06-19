package com.vypeensoft.chargeampmeter.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "ChargeAmpMeterPrefs";
    
    // Keys
    private static final String KEY_SAMPLING_INTERVAL = "sampling_interval";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_DISPLAY_UNIT = "display_unit";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getSamplingInterval() {
        // Return sampling interval in seconds
        return sharedPreferences.getInt(KEY_SAMPLING_INTERVAL, 1);
    }

    public void setSamplingInterval(int intervalInSeconds) {
        sharedPreferences.edit().putInt(KEY_SAMPLING_INTERVAL, intervalInSeconds).apply();
    }

    public boolean isKeepScreenOn() {
        return sharedPreferences.getBoolean(KEY_KEEP_SCREEN_ON, false);
    }

    public void setKeepScreenOn(boolean keepScreenOn) {
        sharedPreferences.edit().putBoolean(KEY_KEEP_SCREEN_ON, keepScreenOn).apply();
    }

    public String getDisplayUnit() {
        return sharedPreferences.getString(KEY_DISPLAY_UNIT, "Both");
    }

    public void setDisplayUnit(String unit) {
        sharedPreferences.edit().putString(KEY_DISPLAY_UNIT, unit).apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean darkMode) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }
}
