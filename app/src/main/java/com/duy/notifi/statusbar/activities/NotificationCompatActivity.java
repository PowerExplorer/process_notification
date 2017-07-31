package com.duy.notifi.statusbar.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.duy.notifi.statusbar.dialogs.CompatibilityNotificationDialog;
import com.duy.notifi.statusbar.utils.StaticUtils;

public class NotificationCompatActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FrameLayout(this));

        CompatibilityNotificationDialog dialog = new CompatibilityNotificationDialog(this);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (StaticUtils.shouldUseCompatNotifications(NotificationCompatActivity.this)) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
        dialog.show();
    }
}
