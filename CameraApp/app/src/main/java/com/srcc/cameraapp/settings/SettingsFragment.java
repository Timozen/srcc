
package com.srcc.cameraapp.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
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

import org.w3c.dom.Text;

import static androidx.core.content.res.ResourcesCompat.getColor;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SRCC_SETTIGNS";
    private final int SRDENSE_TILE_SIZE = 42;
    private final int SRGAN_TILE_SIZE = 42;

    private SharedPreferences sharedPreferences;

    private String srdense_key;
    private String srgan_key;
    private String srresnet_key;
    private ConstraintLayout constraintLayout;

    public static class Builder{
        public SettingsFragment createSettingsFragment() {
            return new SettingsFragment();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        constraintLayout = view.findViewById(R.id.backend_settings);

        srdense_key = getString(R.string.srdense_key);
        srgan_key = getString(R.string.srgan_key);
        srresnet_key = getString(R.string.srresnet_key);

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
    }

    @Override
    public void onClick(View v) {

    }

    @SuppressLint("DefaultLocale")
    private void loadSRDenseSettings() {
        //load the layout as a view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.settings_fragment_srdense, null, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        constraintLayout.removeAllViews();
        constraintLayout.addView(view);

        //set the switch to the settings value
        //Switch use_tiling = view.findViewById(R.id.switch1);
        TextView seekbar_value = view.findViewById(R.id.textView_seekbar_value);
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        SegmentedButtonGroup stitchingStyle = view.findViewById(R.id.srdense_button_group_stitching);

        //set the current saved tile size
        int seekbar_saved_value = sharedPreferences.getInt("srdense_tiling_size", 0);
        seekbar_value.setText(String.format("%d", (seekbar_saved_value + 1 ) * SRDENSE_TILE_SIZE));
        seekBar.setProgress(seekbar_saved_value);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPreferences.edit().putInt("srdense_tiling_size", progress).apply();
                seekbar_value.setText(String.format("%d", (progress + 1)* SRDENSE_TILE_SIZE));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        stitchingStyle.setOnPositionChangedListener(position -> {
            sharedPreferences.edit().putInt("srdense_stitch_style", position).apply();
        });

        stitchingStyle.setPosition(sharedPreferences.getInt("srdense_stitch_style", 0), false);
    }
    @SuppressLint("DefaultLocale")
    private void loadSRGANSettings(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.settings_fragment_srgan, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        constraintLayout.removeAllViews();
        constraintLayout.addView(view);


        Switch tilingSwitch = view.findViewById(R.id.srgan_tiling_switch);
        SeekBar tilingSize = view.findViewById(R.id.srgan_seekBar);
        TextView tilingSizeValue = view.findViewById(R.id.textView_srgan_seekbar_value);
        SegmentedButtonGroup stitchingStyle = view.findViewById(R.id.srgan_button_group_stitching);
        Switch initSwitch = view.findViewById(R.id.srgan_init_switch);
        TextView textViewTilingSize = view.findViewById(R.id.textView_srgan_tiling_size);
        TextView textViewSwitchingStyle = view.findViewById(R.id.textView_srgan_switching_style);


        initSwitch.setChecked(sharedPreferences.getBoolean("srgan_use_init", true));
        initSwitch.setOnClickListener(v -> {
            sharedPreferences.edit().putBoolean("srgan_use_init", initSwitch.isChecked()).apply();
        });


        tilingSwitch.setChecked(sharedPreferences.getBoolean("srgan_use_tiling", true));
        tilingSwitch.setOnClickListener(view1 -> {
            sharedPreferences.edit().putBoolean("srgan_use_tiling", tilingSwitch.isChecked()).apply();
            tilingSize.setEnabled(tilingSwitch.isChecked());
            stitchingStyle.setEnabled(tilingSwitch.isChecked());

            if(tilingSwitch.isChecked()){
                textViewTilingSize.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                tilingSizeValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

                stitchingStyle.setBorder(stitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.orange),
                        stitchingStyle.getBorderDashWidth(),
                        stitchingStyle.getBorderDashGap());


                stitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
            } else {
                textViewTilingSize.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
                textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
                tilingSizeValue.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

                stitchingStyle.setBorder(stitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.grey_500),
                        stitchingStyle.getBorderDashWidth(),
                        stitchingStyle.getBorderDashGap());
                stitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));


            }

        });

        sharedPreferences.edit().putBoolean("srgan_use_tiling", tilingSwitch.isChecked()).apply();
        tilingSize.setEnabled(tilingSwitch.isChecked());
        stitchingStyle.setEnabled(tilingSwitch.isChecked());

        if(tilingSwitch.isChecked()){
            textViewTilingSize.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            tilingSizeValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

            stitchingStyle.setBorder(stitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.orange),
                    stitchingStyle.getBorderDashWidth(),
                    stitchingStyle.getBorderDashGap());


            stitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
        } else {
            textViewTilingSize.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
            textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
            tilingSizeValue.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

            stitchingStyle.setBorder(stitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.grey_500),
                    stitchingStyle.getBorderDashWidth(),
                    stitchingStyle.getBorderDashGap());
            stitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));


        }



        int seekbar_saved_value = sharedPreferences.getInt("srgan_tiling_size", 0);
        tilingSizeValue.setText(String.format("%d", (seekbar_saved_value + 1 ) * SRGAN_TILE_SIZE));
        tilingSize.setProgress(seekbar_saved_value);


        tilingSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPreferences.edit().putInt("srgan_tiling_size", progress).apply();
                tilingSizeValue.setText(String.format("%d", (progress + 1)* SRGAN_TILE_SIZE));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        stitchingStyle.setOnPositionChangedListener(position -> {
            sharedPreferences.edit().putInt("srgan_stitch_style", position).apply();
        });

        stitchingStyle.setPosition(sharedPreferences.getInt("srgan_stitch_style", 0), false);


    }
    private void loadSRResNetSettings(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.settings_fragment_srresnet, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        constraintLayout.removeAllViews();
        constraintLayout.addView(view);


        Switch tilingSwitch = view.findViewById(R.id.srresnet_tiling_switch);
        SeekBar tilingSize = view.findViewById(R.id.srresnet_seekBar);
        TextView tilingSizeValue = view.findViewById(R.id.textView_srresnet_seekbar_value);
        SegmentedButtonGroup stitchingStyle = view.findViewById(R.id.srresnet_button_group_stitching);

        TextView textViewTilingSize = view.findViewById(R.id.textView_srresnet_tiling_size);
        TextView textViewSwitchingStyle = view.findViewById(R.id.textView_srresnet_switching_style);


        tilingSwitch.setChecked(sharedPreferences.getBoolean("srresnet_use_tiling", true));
        tilingSwitch.setOnClickListener(view1 -> {
            sharedPreferences.edit().putBoolean("srresnet_use_tiling", tilingSwitch.isChecked()).apply();
            tilingSize.setEnabled(tilingSwitch.isChecked());
            stitchingStyle.setEnabled(tilingSwitch.isChecked());

            if(tilingSwitch.isChecked()){
                textViewTilingSize.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                tilingSizeValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

                stitchingStyle.setBorder(stitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.orange),
                        stitchingStyle.getBorderDashWidth(),
                        stitchingStyle.getBorderDashGap());


                stitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
            } else {
                textViewTilingSize.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
                textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
                tilingSizeValue.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

                stitchingStyle.setBorder(stitchingStyle.getBorderWidth(),
                        ContextCompat.getColor(getContext(), R.color.grey_500),
                        stitchingStyle.getBorderDashWidth(),
                        stitchingStyle.getBorderDashGap());
                stitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));


            }

        });

        sharedPreferences.edit().putBoolean("srresnet_use_tiling", tilingSwitch.isChecked()).apply();
        tilingSize.setEnabled(tilingSwitch.isChecked());
        stitchingStyle.setEnabled(tilingSwitch.isChecked());

        if(tilingSwitch.isChecked()){
            textViewTilingSize.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            tilingSizeValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

            stitchingStyle.setBorder(stitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.orange),
                    stitchingStyle.getBorderDashWidth(),
                    stitchingStyle.getBorderDashGap());


            stitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.orange));
        } else {
            textViewTilingSize.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
            textViewSwitchingStyle.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
            tilingSizeValue.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));

            stitchingStyle.setBorder(stitchingStyle.getBorderWidth(),
                    ContextCompat.getColor(getContext(), R.color.grey_500),
                    stitchingStyle.getBorderDashWidth(),
                    stitchingStyle.getBorderDashGap());
            stitchingStyle.setSelectedBackground(ContextCompat.getColor(getContext(), R.color.grey_500));


        }



        int seekbar_saved_value = sharedPreferences.getInt("srresnet_tiling_size", 0);
        tilingSizeValue.setText(String.format("%d", (seekbar_saved_value + 1 ) * SRGAN_TILE_SIZE));
        tilingSize.setProgress(seekbar_saved_value);


        tilingSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharedPreferences.edit().putInt("srresnet_tiling_size", progress).apply();
                tilingSizeValue.setText(String.format("%d", (progress + 1)* SRGAN_TILE_SIZE));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        stitchingStyle.setOnPositionChangedListener(position -> {
            sharedPreferences.edit().putInt("srresnet_stitch_style", position).apply();
        });

        stitchingStyle.setPosition(sharedPreferences.getInt("srresnet_stitch_style", 0), false);
    }
}