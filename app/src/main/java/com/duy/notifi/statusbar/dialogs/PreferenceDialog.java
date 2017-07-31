package com.duy.notifi.statusbar.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatDialog;

import com.duy.notifi.R;

public class PreferenceDialog<T> extends AppCompatDialog {

    private T preference, defaultPreference;
    private Object tag;
    private OnPreferenceListener<T> listener;

    public PreferenceDialog(Context context) {
        super(context, R.style.AppTheme_Dialog);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                cancel();
            }
        });
    }

    public PreferenceDialog(Context context, int theme) {
        super(context, theme);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                cancel();
            }
        });
    }

    public void confirm() {
        if (hasListener()) getListener().onPreference(this, getPreference());
        if (isShowing()) dismiss();
    }

    public void cancel() {
        if (hasListener()) getListener().onCancel(this);
        if (isShowing()) dismiss();
    }

    public T getPreference() {
        return preference != null ? preference : getDefaultPreference();
    }

    public PreferenceDialog setPreference(T preference) {
        this.preference = preference;
        return this;
    }

    public T getDefaultPreference() {
        return defaultPreference;
    }

    public PreferenceDialog setDefaultPreference(T preference) {
        defaultPreference = preference;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public PreferenceDialog setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public boolean hasListener() {
        return listener != null;
    }

    public OnPreferenceListener<T> getListener() {
        return listener;
    }

    public PreferenceDialog setListener(OnPreferenceListener<T> listener) {
        this.listener = listener;
        return this;
    }

    public interface OnPreferenceListener<T> {
        void onPreference(PreferenceDialog dialog, T preference);

        void onCancel(PreferenceDialog dialog);
    }
}
