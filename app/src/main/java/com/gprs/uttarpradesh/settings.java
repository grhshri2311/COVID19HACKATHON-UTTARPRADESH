package com.gprs.uttarpradesh;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import static android.content.Context.FINGERPRINT_SERVICE;


public class settings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        final SwitchPreferenceCompat biometric = findPreference("finger");
        biometric.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {


                if ((Boolean) newValue) {
                    FingerprintManager fingerprintManager = (FingerprintManager) getActivity().getSystemService(FINGERPRINT_SERVICE);

                    //Check whether the device has a fingerprint sensor//
                    if (!fingerprintManager.isHardwareDetected()) {
                        // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
                        Toast.makeText(getContext(), "Your device doesn't support fingerprint authentication", Toast.LENGTH_SHORT).show();
                        biometric.setSwitchTextOff("Biometric is turned Off");
                    }
                }
                return true;
            }
        });
    }
}