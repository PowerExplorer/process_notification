package com.duy.notifi.statusbar.dialogs;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duy.notifi.R;
import com.duy.notifi.statusbar.ProgressApplication;
import com.duy.notifi.statusbar.utils.ColorUtils;
import com.duy.notifi.statusbar.views.ColorImageView;
import com.duy.notifi.statusbar.views.CustomImageView;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerDialog extends PreferenceDialog<Integer> implements ProgressApplication.OnActivityResultListener {

    private ProgressApplication statusApplication;
    private TextWatcher textWatcher;
    private List<Integer> presetColors;

    private CustomImageView colorImage;
    private AppCompatEditText colorHex;
    private TextView redInt, greenInt, blueInt;
    private AppCompatSeekBar red, green, blue;
    private View reset;

    private boolean isTrackingTouch;

    public ColorPickerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_color_picker);

        statusApplication = (ProgressApplication) getContext().getApplicationContext();
        statusApplication.addListener(this);

        colorImage = (CustomImageView) findViewById(R.id.color);
        colorHex = (AppCompatEditText) findViewById(R.id.colorHex);
        red = (AppCompatSeekBar) findViewById(R.id.red);
        redInt = (TextView) findViewById(R.id.redInt);
        green = (AppCompatSeekBar) findViewById(R.id.green);
        greenInt = (TextView) findViewById(R.id.greenInt);
        blue = (AppCompatSeekBar) findViewById(R.id.blue);
        blueInt = (TextView) findViewById(R.id.blueInt);
        reset = findViewById(R.id.reset);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int color = Color.parseColor(colorHex.getText().toString());
                    setColor(color, true);
                    setPreference(color);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        colorHex.addTextChangedListener(textWatcher);

        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, i, Color.green(color), Color.blue(color));
                setColor(color, false);
                setPreference(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
            }
        });

        green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, Color.red(color), i, Color.blue(color));
                setColor(color, false);
                setPreference(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
            }
        });

        blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int color = getPreference();
                color = Color.argb(255, Color.red(color), Color.green(color), i);
                setColor(color, false);
                setPreference(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
            }
        });

        setColor(getPreference(), false);

        LinearLayout presetLayout = (LinearLayout) findViewById(R.id.colors);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (presetColors == null) presetColors = new ArrayList<>();
        for (int color : getContext().getResources().getIntArray(R.array.defaultColors)) {
            presetColors.add(color);
        }

        List<Integer> colors = new ArrayList<>();
        for (Integer color : presetColors) {
            if (!colors.contains(color)) colors.add(color);
        }

        for (int preset : colors) {
            View v = inflater.inflate(R.layout.item_color, presetLayout, false);

            ColorImageView colorView = (ColorImageView) v.findViewById(R.id.color);
            colorView.setColor(preset);
            colorView.setTag(preset);
            colorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();
                    if (tag != null && tag instanceof Integer) {
                        setColor((int) tag, true);
                        setPreference((int) tag);
                    }
                }
            });

            presetLayout.addView(v);
        }


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = getDefaultPreference();
                setColor(color, true);
                setPreference(color);
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    private void setColor(@ColorInt int color, boolean animate) {
        if (!isTrackingTouch && animate) {
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), getPreference(), color);
            animator.setDuration(250);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int color = (int) animation.getAnimatedValue();
                    red.setProgress(Color.red(color));
                    green.setProgress(Color.green(color));
                    blue.setProgress(Color.blue(color));
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isTrackingTouch = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isTrackingTouch = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        } else {
            colorImage.setImageDrawable(new ColorDrawable(color));
            colorHex.removeTextChangedListener(textWatcher);
            colorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
            colorHex.setTextColor(ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
            colorHex.addTextChangedListener(textWatcher);
            redInt.setText(String.valueOf(Color.red(color)));
            greenInt.setText(String.valueOf(Color.green(color)));
            blueInt.setText(String.valueOf(Color.blue(color)));

            if (red.getProgress() != Color.red(color)) red.setProgress(Color.red(color));
            if (green.getProgress() != Color.green(color)) green.setProgress(Color.green(color));
            if (blue.getProgress() != Color.blue(color)) blue.setProgress(Color.blue(color));
        }
    }

    public ColorPickerDialog setPresetColors(List<Integer> presetColors) {
        this.presetColors = presetColors;
        return this;
    }

    @Override
    public ColorPickerDialog setPreference(@ColorInt Integer preference) {
        Integer defaultPreference = getDefaultPreference();
        if (preference != null && defaultPreference != null && reset != null) {
            if (preference.equals(defaultPreference))
                reset.setVisibility(View.GONE);
            else reset.setVisibility(View.VISIBLE);
        }

        return (ColorPickerDialog) super.setPreference(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = null;



        if (bitmap != null) {
            new ImageColorPickerDialog(getContext(), bitmap).setDefaultPreference(Color.BLACK).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                @Override
                public void onPreference(PreferenceDialog dialog, Integer preference) {
                    setColor(preference, false);
                    setPreference(preference);
                }

                @Override
                public void onCancel(PreferenceDialog dialog) {
                }
            }).show();
        }
    }

    @Override
    public void dismiss() {
        statusApplication.removeListener(this);
        super.dismiss();
    }
}
