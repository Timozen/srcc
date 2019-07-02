
package com.srcc.cameraapp.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.srcc.cameraapp.R;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SRCC_SETTIGNS";
    private SharedPreferences sharedPreferences;

    private String srdense_key;
    private String srgan_key;
    private String srresnet_key;

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

    private void loadSRDenseSettings() {

    }
    private void loadSRGANSettings(){

    }
    private void loadSRResNetSettings(){

    }
}