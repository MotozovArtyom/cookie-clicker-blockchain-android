package ru.rienel.clicker.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import ru.rienel.clicker.R;

import static ru.rienel.clicker.common.Configuration.SharedPreferencesKeys.PREFERENCES_NAME;
import static ru.rienel.clicker.common.Configuration.SharedPreferencesKeys.PREFERENCES_VOLUME_EFFECT;
import static ru.rienel.clicker.common.Configuration.SharedPreferencesKeys.PREFERENCES_VOLUME_MUSIC;


public class SettingsDialogFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    private SharedPreferences cookieSettings;
    private SeekBar volumeE, volumeM;
    private Button clear;
    private View view;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_settings, null);

        volumeM = (SeekBar)view.findViewById(R.id.seekBarM);
        volumeE = (SeekBar)view.findViewById(R.id.seekBarE);

        volumeM.setOnSeekBarChangeListener(this);
        volumeE.setOnSeekBarChangeListener(this);

        clear = view.findViewById(R.id.btnClearGameSaves);

        cookieSettings = view.getContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        volumeM.setProgress((int)(cookieSettings.getFloat(PREFERENCES_VOLUME_MUSIC,0)*volumeM.getMax()));
        volumeE.setProgress((int)(cookieSettings.getFloat(PREFERENCES_VOLUME_EFFECT,0)*volumeM.getMax()));

        clear.setOnClickListener(newOnClearGamesSavesClickListener());

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SharedPreferences.Editor editor = cookieSettings.edit();

        if (seekBar.getId() == R.id.seekBarM) {
            float musicVolumeLevel = (float) seekBar.getProgress() / (float) volumeM.getMax();
            editor.putFloat(PREFERENCES_VOLUME_MUSIC, musicVolumeLevel);
            editor.apply();
        } else if(seekBar.getId() == R.id.seekBarE) {
            float effectVolumeLevel = (float) seekBar.getProgress() / (float) volumeE.getMax();
            editor.putFloat(PREFERENCES_VOLUME_EFFECT, effectVolumeLevel);
            editor.apply();
        }


    }

    private void clearGameSaves() {
        view.getContext().getSharedPreferences(getString(R.string.gameSaves), Context.MODE_PRIVATE).edit().clear().apply();
    }

    public View.OnClickListener newOnClearGamesSavesClickListener() {
        return view -> {
            clearGameSaves();
            checkFirstLoadGameSaves();
        };
    }

    private boolean checkFirstLoadGameSaves() {
        SharedPreferences saves =  view.getContext().getSharedPreferences(getString(R.string.gameSaves), Context.MODE_PRIVATE);
        boolean hasVisited = saves.getBoolean("hasVisited", false);
        if (!hasVisited) {
            SharedPreferences.Editor editor = saves.edit();
            editor.putBoolean("hasVisited", true);
            editor.putInt("tempClicks", 1);               // Counter of temporary increment clicks  (purchased for comman clicks)
            editor.putInt("tempAutoClicks", 1);           // Counter of temporary Auto clicks  (purchased for comman clicks)
            editor.putInt("donutPerClick", 1);           // Increase only for multiplayer clicks; defoult value is "1";
            editor.putInt("mAutoClicks", 0);                // Counter of Auto clicks (purchased for multiplayer clicks)
            editor.putInt("clicks", 0);            // Common clicks
            editor.putInt("mClicks", 0);            // Multiplayer Clicks
            editor.putInt("currentLevel", 0);            // Player Level
            editor.putInt("commonCoins", 0);            // Player common coins
            editor.putInt("multiplayerCoins", 10);            // Player multiplayer coins
            editor.apply();
            return true;
        }
        return false;
    }

}