package com.duy.notifi.statusbar;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.compat.BuildConfig;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class StatusApplication extends Application {

    private List<OnActivityResultListener> onActivityResultListeners;
    private List<OnColorPickedListener> onColorPickedListeners;
    private List<OnPreferenceChangedListener> onPreferenceChangedListeners;

    public static void showDebug(Context context, String message, int length) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, message, length).show();
            Log.d("Status", message);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        onActivityResultListeners = new ArrayList<>();
        onColorPickedListeners = new ArrayList<>();
        onPreferenceChangedListeners = new ArrayList<>();
    }

    public void addListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    public void removeListener(OnActivityResultListener listener) {
        onActivityResultListeners.remove(listener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnActivityResultListener listener : onActivityResultListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onColor(@Nullable Integer color) {
        for (OnColorPickedListener listener : onColorPickedListeners) {
            listener.onColorPicked(color);
        }
    }

    public void onPreferenceChanged() {
        for (OnPreferenceChangedListener listener : onPreferenceChangedListeners) {
            listener.onPreferenceChanged();
        }
    }

    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public interface OnColorPickedListener {
        void onColorPicked(@Nullable Integer color);
    }

    public interface OnPreferenceChangedListener {
        void onPreferenceChanged();
    }
}
