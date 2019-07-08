package com.srcc.cameraapp.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.google.android.material.snackbar.Snackbar;
import com.srcc.cameraapp.R;
import com.srcc.cameraapp.api.ApiService;

import java.util.Objects;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsFragment extends Fragment {

    public static final int SRDENSE_TILE_SIZE = 42;
    public static final int SRGAN_TILE_SIZE = 42;
    public static final int SRRESNET_TILE_SIZE = 42;

    private SharedPreferences sharedPreferences;
    private ConstraintLayout constraintLayoutSettingsBackend;

    private Retrofit client;
    private ApiService apiService;

    private SettingsFragment(Retrofit client, ApiService apiService) {
        this.client = client;
        this.apiService = apiService;
    }

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
                    loadStitchingSettings("srdense");
                    break;
                case 1:
                    loadStitchingSettings("srresnet");
                    break;
                case 2:
                    loadStitchingSettings("srgan");
                    break;
            }
        });
        segmentedButtonGroup.setPosition(sharedPreferences.getInt("backend", 0), false);


        Switch switchInterpolation = view.findViewById(R.id.switch_interpolation);
        switchInterpolation.setChecked(sharedPreferences.getBoolean("interpolation", false));
        switchInterpolation.setOnClickListener(v -> sharedPreferences.edit().putBoolean("interpolation", switchInterpolation.isChecked()).apply());

        Switch switchDebug = view.findViewById(R.id.switch_debug);
        switchDebug.setChecked(sharedPreferences.getBoolean("debug", false));
        switchDebug.setOnClickListener(v -> sharedPreferences.edit().putBoolean("debug", switchDebug.isChecked()).apply());

        EditText editTextServerUrl = view.findViewById(R.id.editText_server_url);
        editTextServerUrl.setText(sharedPreferences.getString("server_url", "192.168.178.44"));
        editTextServerUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = editTextServerUrl.getText().toString();

                if(text.equals("")){
                   return;
                }

                sharedPreferences.edit().putString("server_url", text).apply();
                client = new Retrofit.Builder()
                        .baseUrl("http://" + text + ":5000/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();
                apiService = client.create(ApiService.class);
            }
        });

    }

    @SuppressLint("DefaultLocale")
    private void loadStitchingSettings(String backend_name) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view;

        final TextView textViewTilingSize;
        final TextView textViewSeekBarValue;
        final TextView textViewStitchingStyle;

        final Switch switchUseTiling ;
        final Switch switchUseOverlap;
        final Switch switchAdjustBrightness;
        final Switch switchUseHSV;
        final SeekBar seekBarTilingSize;
        final SegmentedButtonGroup segmentedButtonGroupStitchingStyle;

        switch (backend_name){
            case "srdense":
                view = inflater.inflate(R.layout.settings_fragment_srdense, null, false);
                break;
            case "srresnet":
                view = inflater.inflate(R.layout.settings_fragment_srresnet, null, false);
                break;
            case "srgan":
                view = inflater.inflate(R.layout.settings_fragment_srgan, null, false);
                break;
            default:
                view = null;
                break;
        }
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        constraintLayoutSettingsBackend.removeAllViews();
        constraintLayoutSettingsBackend.addView(view);

        //load all the references
        switch (backend_name){
            case "srdense":
                textViewTilingSize = view.findViewById(R.id.textView_srdense_tiling_size);
                textViewSeekBarValue = view.findViewById(R.id.textView_srdense_seekbar_value);
                textViewStitchingStyle = view.findViewById(R.id.textView_srdense_stitching_style);

                switchUseTiling = view.findViewById(R.id.switch_srdense_use_tiling);
                switchUseOverlap = view.findViewById(R.id.switch_srdense_use_overlap);
                switchAdjustBrightness = view.findViewById(R.id.switch_srdense_adjust_brightness);
                switchUseHSV = view.findViewById(R.id.switch_srdense_use_hsv);

                seekBarTilingSize = view.findViewById(R.id.seekBar_srdense_tiling_size);
                segmentedButtonGroupStitchingStyle = view.findViewById(R.id.segmentButtonGroup_srdense_stitching_style);
                break;
            case "srresnet":
                textViewTilingSize = view.findViewById(R.id.textView_srresnet_tiling_size);
                textViewSeekBarValue = view.findViewById(R.id.textView_srresnet_seekbar_value);
                textViewStitchingStyle = view.findViewById(R.id.textView_srresnet_stitching_style);

                switchUseTiling = view.findViewById(R.id.switch_srresnet_use_tiling);
                switchUseOverlap = view.findViewById(R.id.switch_srresnet_use_overlap);
                switchAdjustBrightness = view.findViewById(R.id.switch_srresnet_adjust_brightness);
                switchUseHSV = view.findViewById(R.id.switch_srresnet_use_hsv);

                seekBarTilingSize = view.findViewById(R.id.seekBar_srresnet_tiling_size);
                segmentedButtonGroupStitchingStyle = view.findViewById(R.id.segmentButtonGroup_srresnet_stitching_style);
                break;
            case "srgan":
                textViewTilingSize = view.findViewById(R.id.textView_srgan_tiling_size);
                textViewSeekBarValue = view.findViewById(R.id.textView_srgan_seekbar_value);
                textViewStitchingStyle = view.findViewById(R.id.textView_srgan_stitching_style);

                switchUseTiling = view.findViewById(R.id.switch_srgan_use_tiling);
                switchUseOverlap = view.findViewById(R.id.switch_srgan_use_overlap);
                switchAdjustBrightness = view.findViewById(R.id.switch_srgan_adjust_brightness);
                switchUseHSV = view.findViewById(R.id.switch_srgan_use_hsv);

                seekBarTilingSize = view.findViewById(R.id.seekBar_srgan_tiling_size);
                segmentedButtonGroupStitchingStyle = view.findViewById(R.id.segmentButtonGroup_srgan_stitching_style);

                Switch switchUseInit = view.findViewById(R.id.switch_srgan_init);
                switchUseInit.setChecked(sharedPreferences.getBoolean("srgan_use_init", true));
                switchUseInit.setOnClickListener(v -> sharedPreferences.edit().putBoolean("srgan_use_init", switchUseInit.isChecked()).apply());
                break;
            default:
                textViewTilingSize = null;
                textViewSeekBarValue = null;
                textViewStitchingStyle = null;

                switchUseOverlap = null;
                switchAdjustBrightness = null;
                switchUseHSV = null;
                switchUseTiling = null;
                seekBarTilingSize = null;
                segmentedButtonGroupStitchingStyle = null;
                break;
        }

        //load the current settings

        int seekBarCurrentProgress = sharedPreferences.getInt(backend_name + "_tiling_size", 0);
        textViewSeekBarValue.setText(String.format("%d", (seekBarCurrentProgress + 1) * SRDENSE_TILE_SIZE));
        seekBarTilingSize.setProgress(seekBarCurrentProgress);

        Objects.requireNonNull(switchUseTiling).setChecked(sharedPreferences.getBoolean(backend_name + "_use_tiling", true));
        switchUseOverlap.setChecked(sharedPreferences.getBoolean(backend_name + "_use_overlap", true));
        segmentedButtonGroupStitchingStyle.setPosition(sharedPreferences.getInt(backend_name + "_stitch_style", 0), false);
        switchAdjustBrightness.setChecked(sharedPreferences.getBoolean(backend_name + "_adjust_brightness", true));
        switchUseHSV.setChecked(sharedPreferences.getBoolean(backend_name + "_use_hsv", true));


        //set the action listeners
        switchUseTiling.setOnClickListener(view1 -> {
            sharedPreferences.edit().putBoolean(backend_name + "_use_tiling", switchUseTiling.isChecked()).apply();
            boolean isChecked = switchUseTiling.isChecked();

            if(backend_name == "srdense" && !isChecked){
                Snackbar.make(view, "CAVEAT: This requires a strong backend!", Snackbar.LENGTH_SHORT).show();
            }

            textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), isChecked ? R.color.black : R.color.grey_500));
            seekBarTilingSize.setEnabled(isChecked);
            textViewSeekBarValue.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));

            textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));
            switchUseOverlap.setEnabled(isChecked);
            switchUseOverlap.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));

            segmentedButtonGroupStitchingStyle.setEnabled(isChecked);
            segmentedButtonGroupStitchingStyle.setBorder(
                    segmentedButtonGroupStitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), isChecked ? R.color.orange : R.color.grey_500),
                    segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                    segmentedButtonGroupStitchingStyle.getBorderDashGap()
            );
            segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), isChecked ? R.color.orange : R.color.grey_500));

            switchAdjustBrightness.setEnabled(isChecked);
            switchAdjustBrightness.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));
            switchUseHSV.setEnabled(switchAdjustBrightness.isChecked() && switchUseTiling.isChecked());
            switchUseHSV.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));
        });

        //have to check the same rules on loading
        boolean isChecked = switchUseTiling.isChecked();
        textViewTilingSize.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), isChecked ? R.color.black : R.color.grey_500));
        seekBarTilingSize.setEnabled(isChecked);
        textViewSeekBarValue.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));

        textViewStitchingStyle.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));
        switchUseOverlap.setEnabled(isChecked);
        switchUseOverlap.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));

        segmentedButtonGroupStitchingStyle.setEnabled(isChecked);
        segmentedButtonGroupStitchingStyle.setBorder(
                segmentedButtonGroupStitchingStyle.getBorderWidth(),
                ContextCompat.getColor(getContext(), isChecked ? R.color.orange : R.color.grey_500),
                segmentedButtonGroupStitchingStyle.getBorderDashWidth(),
                segmentedButtonGroupStitchingStyle.getBorderDashGap()
        );
        segmentedButtonGroupStitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), isChecked ? R.color.orange : R.color.grey_500));

        switchAdjustBrightness.setEnabled(isChecked);
        switchAdjustBrightness.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));
        switchUseHSV.setEnabled(switchAdjustBrightness.isChecked() && switchUseTiling.isChecked());
        switchUseHSV.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));

        seekBarTilingSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPreferences.edit().putInt(backend_name + "_tiling_size", progress).apply();
                textViewSeekBarValue.setText(String.format("%d", (progress + 1) * SRDENSE_TILE_SIZE));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        segmentedButtonGroupStitchingStyle.setOnPositionChangedListener(position -> {
            sharedPreferences.edit().putInt(backend_name + "_stitch_style", position).apply();
            boolean simple = position == 0;
            switchAdjustBrightness.setEnabled(!simple);
            switchAdjustBrightness.setTextColor(ContextCompat.getColor(getContext(), !simple ? R.color.black : R.color.grey_500));
            switchUseHSV.setEnabled(!simple && switchAdjustBrightness.isChecked() );
            switchUseHSV.setTextColor(ContextCompat.getColor(getContext(), !simple && switchAdjustBrightness.isChecked() ? R.color.black : R.color.grey_500));
        });


        switchUseOverlap.setOnClickListener(v -> sharedPreferences.edit().putBoolean(backend_name + "_use_overlap", switchUseOverlap.isChecked()).apply());
        switchAdjustBrightness.setOnClickListener(v -> {
            sharedPreferences.edit().putBoolean(backend_name + "_adjust_brightness", switchAdjustBrightness.isChecked()).apply();
            switchUseHSV.setEnabled(switchAdjustBrightness.isChecked() && switchUseTiling.isChecked());
            switchUseHSV.setTextColor(ContextCompat.getColor(getContext(), switchAdjustBrightness.isChecked() && switchUseTiling.isChecked()  ? R.color.black : R.color.grey_500));
        });

        isChecked = segmentedButtonGroupStitchingStyle.getPosition() != 0;
        switchAdjustBrightness.setEnabled(isChecked);
        switchAdjustBrightness.setTextColor(ContextCompat.getColor(getContext(), isChecked ? R.color.black : R.color.grey_500));


        switchUseHSV.setEnabled(switchAdjustBrightness.isChecked() && switchUseTiling.isChecked() && segmentedButtonGroupStitchingStyle.getPosition() != 0);
        switchUseHSV.setOnClickListener(v-> sharedPreferences.edit().putBoolean(backend_name + "_use_hsv", switchUseHSV.isChecked()).apply());
        switchUseHSV.setTextColor(ContextCompat.getColor(getContext(), switchAdjustBrightness.isChecked() && switchUseTiling.isChecked() && isChecked ? R.color.black : R.color.grey_500));
    }

    public static class Builder {
        private Retrofit client;
        private ApiService apiService;

        public SettingsFragment createSettingsFragment() {
            return new SettingsFragment(client, apiService);
        }

        public Builder setClient(Retrofit client) {
            this.client = client;
            return this;
        }

        public Builder setApiService(ApiService apiService) {
            this.apiService = apiService;
            return this;
        }
    }
}