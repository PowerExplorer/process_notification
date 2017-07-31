package com.duy.notifi.statusbar.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.duy.notifi.R;
import com.duy.notifi.statusbar.StatusApplication;
import com.duy.notifi.statusbar.data.AppData;
import com.duy.notifi.statusbar.utils.ColorUtils;
import com.duy.notifi.statusbar.utils.PreferenceUtils;

import java.lang.ref.SoftReference;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    public static final String
            ACTION_GET_COLOR = "com.duy.notifi.ACTION_GET_COLOR",
            EXTRA_COMPONENT = "com.duy.notifi.EXTRA_COMPONENT";

    private PackageManager packageManager;

    private AppData.ActivityData activityData;
    private VolumeReceiver volumeReceiver;

    private int color = Color.BLACK;

    public static boolean shouldHideOnVolume(Context context) {
        Boolean isVolumeHidden = PreferenceUtils.getBooleanPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_HIDE_ON_VOLUME);
        return (isVolumeHidden == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) || (isVolumeHidden != null && isVolumeHidden);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case ACTION_GET_COLOR:
                        Intent i = new Intent(StatusService.ACTION_UPDATE);
                        i.setClass(this, StatusService.class);
                        i.putExtra(StatusService.EXTRA_COLOR, color);
                        startService(i);
                        break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        packageManager = getPackageManager();

        volumeReceiver = new VolumeReceiver(this);
        registerReceiver(volumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));

        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        }

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled != null && enabled) {
            switch (event.getEventType()) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    final CharSequence packageName = event.getPackageName();
                    final CharSequence className = event.getClassName();
                    StatusApplication.showDebug(this, event.toString(), Toast.LENGTH_LONG);

                    if (packageManager != null && packageName != null && packageName.length() > 0 && className != null && className.length() > 0) {
                        try {
                            activityData = new AppData.ActivityData(packageManager, packageManager.getActivityInfo(new ComponentName(packageName.toString(), className.toString()), PackageManager.GET_META_DATA));
                        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                            if (activityData != null) {
                                if (packageName.toString().equals("com.android.systemui")) {
                                    if (event.getText().toString().toLowerCase().contains("volume")) {
                                        if (event.getText().toString().toLowerCase().contains("hidden")) {
                                            volumeReceiver.cancel();
                                            setStatusBar(null, false, null, false, null, null);
                                        } else if (!VolumeReceiver.canReceive())
                                            volumeReceiver.onVolumeChanged();
                                    } else setStatusBar(null, false, null, true, null, null);
                                } else setStatusBar(null, false, null, false, null, null);
                            }
                            return;
                        }

                        Boolean isFullscreen = activityData.getBooleanPreference(this, AppData.PreferenceIdentifier.FULLSCREEN);
                        Boolean isTransparentEnabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_HOME_TRANSPARENT);
                        boolean isHome = false;

                        if (packageManager != null) {
                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                            homeIntent.addCategory(Intent.CATEGORY_HOME);
                            ResolveInfo homeInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);

                            isHome = homeInfo != null && packageName.toString().matches(homeInfo.activityInfo.packageName);
                        }

                        Integer color = activityData.getIntegerPreference(this, AppData.PreferenceIdentifier.COLOR);
                        if (color != null && (!isHome || (isTransparentEnabled != null && !isTransparentEnabled))) {
                            setStatusBar(color, null, isFullscreen, false, packageName.toString(), activityData);
                            return;
                        } else if (isHome) {
                            setStatusBar(null, true, isFullscreen, false, packageName.toString(), activityData);
                            return;
                        }

                        Boolean isColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
                        if (isColorAuto != null && !isColorAuto) {
                            setStatusBar(getDefaultColor(), null, isFullscreen, false, packageName.toString(), activityData);
                            return;
                        }

                        if (packageName.toString().equals("com.android.systemui")) {
                            //prevents the creation of some pretty nasty looking color schemes below Lollipop
                            setStatusBar(getDefaultColor(), null, false, false, packageName.toString(), activityData);
                            return;
                        }

                        if (packageName.toString().matches("com.duy.notifi")) {
                            //prevents recursive heads up notifications
                            setStatusBar(ContextCompat.getColor(this, R.color.colorPrimaryDark), null, isFullscreen, false, packageName.toString(), activityData);
                            return;
                        }

                        Integer cacheVersion = activityData.getIntegerPreference(this, AppData.PreferenceIdentifier.CACHE_VERSION);
                        if (cacheVersion != null && cacheVersion == activityData.version) {
                            color = activityData.getIntegerPreference(this, AppData.PreferenceIdentifier.CACHE_COLOR);
                        }

                        if (color == null) {
                            color = ColorUtils.getPrimaryColor(AccessibilityService.this, new ComponentName(packageName.toString(), className.toString()));

                            if (color != null) {
                                activityData.putPreference(this, AppData.PreferenceIdentifier.CACHE_COLOR, color);
                                activityData.putPreference(this, AppData.PreferenceIdentifier.CACHE_VERSION, activityData.version);
                            }
                        }

                        setStatusBar(color != null ? color : getDefaultColor(), null, isFullscreen, false, packageName.toString(), activityData);
                    }
            }
        }
    }

    @ColorInt
    private int getDefaultColor() {
        Integer color = PreferenceUtils.getIntegerPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
        if (color == null) color = Color.BLACK;
        return color;
    }

    private void setStatusBar(@Nullable @ColorInt Integer color, @Nullable Boolean isTransparent, @Nullable Boolean isFullscreen, @Nullable Boolean isSystemFullscreen, @Nullable String packageName, @Nullable AppData.ActivityData activityData) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);

        if (color != null) intent.putExtra(StatusService.EXTRA_COLOR, color);

        if (isTransparent != null)
            intent.putExtra(StatusService.EXTRA_IS_TRANSPARENT, isTransparent);

        if (isFullscreen != null) intent.putExtra(StatusService.EXTRA_IS_FULLSCREEN, isFullscreen);

        if (isSystemFullscreen != null)
            intent.putExtra(StatusService.EXTRA_IS_SYSTEM_FULLSCREEN, isSystemFullscreen);

        if (packageName != null)
            intent.putExtra(StatusService.EXTRA_PACKAGE, packageName);

        if (activityData != null)
            intent.putExtra(StatusService.EXTRA_ACTIVITY, activityData);

        startService(intent);

        if (color != null) this.color = color;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        if (volumeReceiver != null) unregisterReceiver(volumeReceiver);
        super.onDestroy();
    }

    private static class VolumeReceiver extends BroadcastReceiver {

        private SoftReference<AccessibilityService> reference;
        private Handler handler;
        private Runnable runnable;

        private VolumeReceiver(AccessibilityService service) {
            reference = new SoftReference<>(service);
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    AccessibilityService service = reference.get();
                    if (service != null) {
                        StatusApplication.showDebug(service, "Volume callback called", Toast.LENGTH_SHORT);
                        service.setStatusBar(null, false, null, false, null, null);
                    }
                }
            };
        }

        private static boolean canReceive() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            StatusApplication.showDebug(context, intent.getExtras().toString(), Toast.LENGTH_SHORT);
            onVolumeChanged();
        }

        private void onVolumeChanged() {
            AccessibilityService service = reference.get();
            if (service != null && shouldHideOnVolume(service)) {
                StatusApplication.showDebug(service, "Volume callback added", Toast.LENGTH_SHORT);
                service.setStatusBar(null, false, null, true, null, null);
                handler.removeCallbacks(runnable);

                handler.postDelayed(runnable, 3000);
            }
        }

        private void cancel() {
            AccessibilityService service = reference.get();
            if (service != null)
                StatusApplication.showDebug(service, "Volume callback removed", Toast.LENGTH_SHORT);
            handler.removeCallbacks(runnable);
        }
    }
}
