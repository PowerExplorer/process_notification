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

public class ProgressApplication extends Application {
    private static final String TAG = "ProgressApplication";
    private List<OnActivityResultListener> onActivityResultListeners;
    private List<OnPreferenceChangedListener> onPreferenceChangedListeners;

    public static void showDebug(Context context, String message, int length) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, message, length).show();
            Log.d(TAG, "showDebug message = " + message);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        onActivityResultListeners = new ArrayList<>();
        onPreferenceChangedListeners = new ArrayList<>();
    }

    public void addListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    public void removeListener(OnActivityResultListener listener) {
        onActivityResultListeners.remove(listener);
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
