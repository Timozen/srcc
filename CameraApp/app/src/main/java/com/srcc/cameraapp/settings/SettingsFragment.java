package com.srcc.cameraapp.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.srcc.cameraapp.R;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    public static final int SRDENSE_TILE_SIZE = 42;
    public static final int SRGAN_TILE_SIZE = 42;
    public static final int SRRESNET_TILE_SIZE = 42;

    private SharedPreferences sharedPreferences;
    private ConstraintLayout constraintLayoutSettingsBackend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        constraintLayoutSettingsBackend = view.findViewById(R.id.constraintLayout_settings_backend);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        SegmentedButtonGroup segmentedButtonGroup = view.findViewById(R.id.segmentedButtonGroup);
        segmentedButtonGroup.setOnPositionChangedListener(position -> {

            sharedPreferences.edit().putInt("backend", position).apply();
            switch (position) {
                case 0:
                    loadSRDenseSettings();
                    break;
                case 1:
                    loadSRResNetSettings();
                    break;
                case 2:
                    loadSRGANSettings();
                    break;
            }
        });
        segmentedButtonGroup.setPosition(sharedPreferences.getInt("backend", 0), false);

        Switch switchDebug = view.findViewById(R.id.switch_debug);
        switchDebug.setChecked(sharedPreferences.getBoolean("debug", true));
        switchDebug.setOnClickListener(v -> sharedPreferences.edit().putBoolean("debug", switchDebug.isChecked()).apply());

    }

    @SuppressLint("DefaultLocale")
    private void loadSRDenseSettings() {
        //load the layout as a view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.settings_fragment_srdense, null, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        constraintLayoutSettingsBackend.removeAllViews();
        constraintLayoutSettingsBackend.addView(view);

        TextView textViewTilingSize = view.findViewById(R.id.textView_srdense_tiling_size);
        TextView textViewSeekBarValue = view.findViewById(R.id.textView_seekbar_value);
        TextView textViewStitchingStyle = view.findViewById(R.id.textView_srdense_stitching_style);

        Switch switchUseTiling = view.findViewById(R.id.switch_srdense_use_tiling);
        SeekBar seekBarTilingSize = view.findViewById(R.id.seekBar_srdense_tiling_size);
        SegmentedButtonGroup segmentedButtonGroupStitchingStyle = view.findViewById(R.id.segmentButtonGroup_srdense_stitching_style);


        switchUseTiling.setChecked(sharedPreferences.getBoolean("srdense_use_tiling", true));
        switchUseTiling.setOnClickListener(view1 -> {
            sharedPreferences.edit().putBoolean("srdense_use_tiling", switchUseTiling.isChecked()).apply();
            textViewTilingSize.setEnabled(switchUseTiling.isChecked());
            segmentedButtonGroupStitchingStyle.setEnabled(switchUseTiling.isChecked());

            if (switchUseTiling.isChecked()) {
                textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.black));
                textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                textViewSeekBarValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

                segmentedButtonGroupStitchingStyle.setBorder(
                        segmentedButtonGroupStitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.orange),
                        segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                        segmentedButtonGroupStitchingStyle.getBorderDashGap()
                );


                segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
            } else {
                textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.grey_500));
                textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
                textViewSeekBarValue.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

                segmentedButtonGroupStitchingStyle.setBorder(
                        segmentedButtonGroupStitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.grey_500),
                        segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                        segmentedButtonGroupStitchingStyle.getBorderDashGap()
                );
                segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));
            }

        });

        sharedPreferences.edit().putBoolean("srdense_use_tiling", switchUseTiling.isChecked()).apply();
        textViewTilingSize.setEnabled(switchUseTiling.isChecked());
        segmentedButtonGroupStitchingStyle.setEnabled(switchUseTiling.isChecked());

        if (switchUseTiling.isChecked()) {
            textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.black));
            textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            textViewSeekBarValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

            segmentedButtonGroupStitchingStyle.setBorder(
                    segmentedButtonGroupStitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.orange),
                    segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                    segmentedButtonGroupStitchingStyle.getBorderDashGap()
            );
            segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
        } else {
            textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.grey_500));
            textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
            textViewSeekBarValue.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

            segmentedButtonGroupStitchingStyle.setBorder(
                    segmentedButtonGroupStitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.grey_500),
                    segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                    segmentedButtonGroupStitchingStyle.getBorderDashGap()
            );
            segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));
        }

        //set the current saved tile size
        int seekBarCurrentProgress = sharedPreferences.getInt("srdense_tiling_size", 0);
        textViewSeekBarValue.setText(String.format("%d", (seekBarCurrentProgress + 1) * SRDENSE_TILE_SIZE));
        seekBarTilingSize.setProgress(seekBarCurrentProgress);

        seekBarTilingSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPreferences.edit().putInt("srdense_tiling_size", progress).apply();
                textViewSeekBarValue.setText(String.format("%d", (progress + 1) * SRDENSE_TILE_SIZE));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        segmentedButtonGroupStitchingStyle.setOnPositionChangedListener(position -> sharedPreferences.edit().putInt("srdense_stitch_style", position).apply());

        segmentedButtonGroupStitchingStyle.setPosition(sharedPreferences.getInt("srdense_stitch_style", 0), false);
    }

    @SuppressLint("DefaultLocale")
    private void loadSRGANSettings() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.settings_fragment_srgan, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        constraintLayoutSettingsBackend.removeAllViews();
        constraintLayoutSettingsBackend.addView(view);

        TextView textViewSeekBarProgress = view.findViewById(R.id.textView_srgan_seekbar_progress);
        TextView textViewTilingSize = view.findViewById(R.id.textView_srgan_tiling_size);
        TextView textViewSwitchingStyle = view.findViewById(R.id.textView_srgan_switching_style);

        Switch switchInit = view.findViewById(R.id.switch_srgan_init);
        Switch switchUseTiling = view.findViewById(R.id.switch_srgan_use_tiling);

        SeekBar seekBarTilingSize = view.findViewById(R.id.seekBar_srgan);
        SegmentedButtonGroup segmentedButtonGroupStitchingStyle = view.findViewById(R.id.segmentedButtonGroup_srgan_stitching_style);

        switchInit.setChecked(sharedPreferences.getBoolean("srgan_use_init", true));
        switchInit.setOnClickListener(v -> sharedPreferences.edit().putBoolean("srgan_use_init", switchInit.isChecked()).apply());


        switchUseTiling.setChecked(sharedPreferences.getBoolean("srgan_use_tiling", true));
        switchUseTiling.setOnClickListener(view1 -> {
            sharedPreferences.edit().putBoolean("srgan_use_tiling", switchUseTiling.isChecked()).apply();
            seekBarTilingSize.setEnabled(switchUseTiling.isChecked());
            segmentedButtonGroupStitchingStyle.setEnabled(switchUseTiling.isChecked());

            if (switchUseTiling.isChecked()) {
                textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.black));
                textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                textViewSeekBarProgress.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

                segmentedButtonGroupStitchingStyle.setBorder(segmentedButtonGroupStitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.orange),
                        segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                        segmentedButtonGroupStitchingStyle.getBorderDashGap());


                segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
            } else {
                textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.grey_500));
                textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
                textViewSeekBarProgress.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

                segmentedButtonGroupStitchingStyle.setBorder(
                        segmentedButtonGroupStitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.grey_500),
                        segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                        segmentedButtonGroupStitchingStyle.getBorderDashGap()
                );
                segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));
            }

        });

        sharedPreferences.edit().putBoolean("srgan_use_tiling", switchUseTiling.isChecked()).apply();
        seekBarTilingSize.setEnabled(switchUseTiling.isChecked());
        segmentedButtonGroupStitchingStyle.setEnabled(switchUseTiling.isChecked());

        if (switchUseTiling.isChecked()) {
            textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.black));
            textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            textViewSeekBarProgress.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

            segmentedButtonGroupStitchingStyle.setBorder(
                    segmentedButtonGroupStitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.orange),
                    segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                    segmentedButtonGroupStitchingStyle.getBorderDashGap()
            );
            segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
        } else {
            textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.grey_500));
            textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
            textViewSeekBarProgress.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

            segmentedButtonGroupStitchingStyle.setBorder(
                    segmentedButtonGroupStitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.grey_500),
                    segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                    segmentedButtonGroupStitchingStyle.getBorderDashGap()
            );
            segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));
        }
        int seekBarCurrentProgress = sharedPreferences.getInt("srgan_tiling_size", 0);
        textViewSeekBarProgress.setText(String.format("%d", (seekBarCurrentProgress + 1) * SRGAN_TILE_SIZE));
        seekBarTilingSize.setProgress(seekBarCurrentProgress);

        seekBarTilingSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPreferences.edit().putInt("srgan_tiling_size", progress).apply();
                textViewSeekBarProgress.setText(String.format("%d", (progress + 1) * SRGAN_TILE_SIZE));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        segmentedButtonGroupStitchingStyle.setOnPositionChangedListener(position -> sharedPreferences.edit().putInt("srgan_stitch_style", position).apply());

        segmentedButtonGroupStitchingStyle.setPosition(sharedPreferences.getInt("srgan_stitch_style", 0), false);
    }

    @SuppressLint("DefaultLocale")
    private void loadSRResNetSettings() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.settings_fragment_srresnet, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        constraintLayoutSettingsBackend.removeAllViews();
        constraintLayoutSettingsBackend.addView(view);

        TextView textViewSeekBarProgress = view.findViewById(R.id.textView_srresnet_seekbar_progress);
        TextView textViewTilingSize = view.findViewById(R.id.textView_srresnet_tiling_size);
        TextView textViewStitchingStyle = view.findViewById(R.id.textView_srresnet_stitching_style);

        Switch switchUseTiling = view.findViewById(R.id.srresnet_tiling_switch);
        SeekBar seekBarTilingSize = view.findViewById(R.id.srresnet_seekBar);
        SegmentedButtonGroup segmentedButtonGroupStitchingStyle = view.findViewById(R.id.srresnet_button_group_stitching);

        switchUseTiling.setChecked(sharedPreferences.getBoolean("srresnet_use_tiling", true));
        switchUseTiling.setOnClickListener(view1 -> {
            sharedPreferences.edit().putBoolean("srresnet_use_tiling", switchUseTiling.isChecked()).apply();
            seekBarTilingSize.setEnabled(switchUseTiling.isChecked());
            segmentedButtonGroupStitchingStyle.setEnabled(switchUseTiling.isChecked());

            if (switchUseTiling.isChecked()) {
                textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.black));
                textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                textViewSeekBarProgress.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

                segmentedButtonGroupStitchingStyle.setBorder(
                        segmentedButtonGroupStitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.orange),
                        segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                        segmentedButtonGroupStitchingStyle.getBorderDashGap()
                );
                segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
            } else {
                textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.grey_500));
                textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
                textViewSeekBarProgress.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

                segmentedButtonGroupStitchingStyle.setBorder(
                        segmentedButtonGroupStitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.grey_500),
                        segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                        segmentedButtonGroupStitchingStyle.getBorderDashGap()
                );
                segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));
            }
        });

        sharedPreferences.edit().putBoolean("srresnet_use_tiling", switchUseTiling.isChecked()).apply();
        seekBarTilingSize.setEnabled(switchUseTiling.isChecked());
        segmentedButtonGroupStitchingStyle.setEnabled(switchUseTiling.isChecked());

        if (switchUseTiling.isChecked()) {
            textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.black));
            textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            textViewSeekBarProgress.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

            segmentedButtonGroupStitchingStyle.setBorder(
                    segmentedButtonGroupStitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.orange),
                    segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                    segmentedButtonGroupStitchingStyle.getBorderDashGap()
            );
            segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
        } else {
            textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.grey_500));
            textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
            textViewSeekBarProgress.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

            segmentedButtonGroupStitchingStyle.setBorder(
                    segmentedButtonGroupStitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.grey_500),
                    segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                    segmentedButtonGroupStitchingStyle.getBorderDashGap());
            segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));
        }
        int seekBarCurrentProgress = sharedPreferences.getInt("srresnet_tiling_size", 0);
        textViewSeekBarProgress.setText(String.format("%d", (seekBarCurrentProgress + 1) * SRRESNET_TILE_SIZE));
        seekBarTilingSize.setProgress(seekBarCurrentProgress);


        seekBarTilingSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPreferences.edit().putInt("srresnet_tiling_size", progress).apply();
                textViewSeekBarProgress.setText(String.format("%d", (progress + 1) * SRRESNET_TILE_SIZE));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        segmentedButtonGroupStitchingStyle.setOnPositionChangedListener(position -> sharedPreferences.edit().putInt("srresnet_stitch_style", position).apply());

        segmentedButtonGroupStitchingStyle.setPosition(sharedPreferences.getInt("srresnet_stitch_style", 0), false);
    }

    public static class Builder {
        public SettingsFragment createSettingsFragment() {
            return new SettingsFragment();
        }
    }
}