package com.dv.apps.purpleplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AutoSwitchMode;
import com.afollestad.materialdialogs.color.ColorChooserDialog;


public class SettingsActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.action_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

    }


    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int i) {
        String dialogueTag = colorChooserDialog.tag();
        if (dialogueTag.equals("Secondary")){
            Aesthetic.get()
                    .colorAccent(i)
                    .apply();
        }else {
            Aesthetic.get()
                    .colorPrimary(i)
                    .colorStatusBarAuto()
                    .lightStatusBarMode(AutoSwitchMode.AUTO)
                    .colorNavigationBarAuto()
                    .textColorSecondaryInverseRes(android.R.color.white)
                    .isDark(false)
                    .apply();

            Toast.makeText(this, R.string.restartAppIfThemeChanged, Toast.LENGTH_LONG).show();
            preferences.edit().putInt("primary_color", i).apply();
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog colorChooserDialog) {

    }

    @Override
    protected void onPause() {
        Aesthetic.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Aesthetic.resume(this);
    }
}
