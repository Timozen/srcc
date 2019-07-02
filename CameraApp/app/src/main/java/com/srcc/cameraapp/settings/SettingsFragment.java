
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
import androidx.fragment.app.Fragment;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.srcc.cameraapp.R;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SRCC_SETTIGNS";
    private final int SRDENSE_TILE_SIZE = 44;

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

            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (position) {
                case 0:
                    Log.i(TAG, "Clicked segment button " + srdense_key);
                    editor.putBoolean(getString(R.string.srdense_key), true);
                    editor.putBoolean(getString(R.string.srgan_key), false);
                    editor.putBoolean(getString(R.string.srresnet_key), false);
                    loadSRDenseSettings();
                    break;
                case 1:
                    Log.i(TAG, "Clicked segment button " + srgan_key);
                    editor.putBoolean(getString(R.string.srdense_key), false);
                    editor.putBoolean(getString(R.string.srgan_key), true);
                    editor.putBoolean(getString(R.string.srresnet_key), false);
                    loadSRGANSettings();
                    break;
                case 2:
                    Log.i(TAG, "Clicked segment button " + srresnet_key);
                    editor.putBoolean(getString(R.string.srdense_key), false);
                    editor.putBoolean(getString(R.string.srgan_key), false);
                    editor.putBoolean(getString(R.string.srresnet_key), true);
                    loadSRResNetSettings();
                    break;
            }
            editor.apply();
        });

        //activate the button which is currently active, should only be one...
        if(sharedPreferences.getBoolean(srdense_key, true)){
            segmentedButtonGroup.setPosition(0, false);
        } else if(sharedPreferences.getBoolean(srgan_key, true)){
            segmentedButtonGroup.setPosition(1, false);
        } else if(sharedPreferences.getBoolean(srresnet_key, true)){
            segmentedButtonGroup.setPosition(2, false);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @SuppressLint("DefaultLocale")
    private void loadSRDenseSettings() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.settings_fragment_srdense, null, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        constraintLayout.removeAllViews();
        constraintLayout.addView(view);

        //set the switch to the settings value
        Switch use_tiling = view.findViewById(R.id.switch1);
        TextView seekbar_value = view.findViewById(R.id.textView_seekbar_value);
        SeekBar seekBar = view.findViewById(R.id.seekBar);


        use_tiling.setChecked(sharedPreferences.getBoolean("srdense_use_tiling", true));
        use_tiling.setOnClickListener(v -> {
            sharedPreferences.edit().putBoolean("srdense_use_tiling", use_tiling.isChecked()).apply();
            seekBar.setEnabled(use_tiling.isChecked());
        });

        seekBar.setEnabled(use_tiling.isChecked());

        //set the current saved tile size
        int seekbar_saved_value = sharedPreferences.getInt("srdense_tiling_size", 0);
        seekbar_value.setText(String.format("%d", (seekbar_saved_value + 1 )* SRDENSE_TILE_SIZE));
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
    }
    private void loadSRGANSettings(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.settings_fragment_srgan, null);
        constraintLayout.removeAllViews();
        constraintLayout.addView(view);
    }
    private void loadSRResNetSettings(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.settings_fragment_srresnet, null);
        constraintLayout.removeAllViews();
        constraintLayout.addView(view);
    }
}