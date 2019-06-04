/**
 * BabelFish
 * Copyright (C) 2019  Julia Gleichweit
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tuwien.babelfish.speech;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Set;

import tuwien.babelfish.CheckConnection;
import tuwien.babelfish.LanguageDialogFragment;
import tuwien.babelfish.R;
import tuwien.babelfish.bluetooth.BluetoothConnectionService;

/**
 * Fragment used to implement the translation pipeline (speech-to-text -> translation -> text-to-speech)
 */
public class SpeechService extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener {

    private static final int REQUEST_RECORD_AUDIO = 5;
    private static final int REQUEST_ENABLE_BLUETOOTH = 10;
    private boolean allowRecording = false;

    private EditText et_speech_input;
    private EditText et_translation;
    private ImageView iv_bot;

    private boolean startListening = false;
    private boolean initialised = false;
    private int lastLangCode;

    private TextToSpeech textToSpeech;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnectionService btService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.speech_process, container, false);

        ImageView microfon_icon = rootView.findViewById(R.id.record_button);
        microfon_icon.setOnClickListener(view -> onClickMicrofon(view));

        et_speech_input = rootView.findViewById(R.id.et_spoken_text);
        et_translation = rootView.findViewById(R.id.et_translated_text);
        iv_bot = rootView.findViewById(R.id.bot);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            iv_bot.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.bot_grey));
        }

        iv_bot.setOnClickListener(view ->onClickConnect(view));
        // initialize TextToSpeech
        textToSpeech = new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                   initialised = true;
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    public EditText getSpokenView() {
        return et_speech_input;
    }

    public EditText getTranslationView() {
        return et_translation;
    }


    /**
     * Checks if permissions RECORD_AUDIO is given
     *
     * @return true if permission granted; otherwise false
     */
    public boolean permissionCheck() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                // returns true if the user has previously denied the request, and returns
                // false if a user has denied a permission and selected the Don't ask again option
                showPermissionInfoDialog();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_XXX is an app-defined int constant.
                // The callback method gets the result of the request.
                return false;
            }
        } else {
            // Permission has already been granted
            //microfon_icon.setImageDrawable(getResources().getDrawable(R.drawable.microphone));
            return true;
        }
        return false;
    }

    /**
     * Inform user of needed permission and  give shortcut to settings menu
     */
    private void showPermissionInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_missing_permission);
        builder.setMessage(R.string.missing_record_permission);
        builder.setCancelable(true);

        // on ok go to settings page
        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    dialogInterface.dismiss();

                    Intent settingsIntent = new Intent();

                    // we want to display the application's settings page
                    settingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    settingsIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //identify our app
                    settingsIntent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
                    //start our intent
                    startActivity(settingsIntent);
                } catch (Exception e) {

                    Context context = getActivity().getApplicationContext();
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, e.getMessage(), duration);
                    toast.show();
                }
            }
        });
        // automatically creates and immediately shows the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    allowRecording = true;

                } else {
                    // permission denied. Disable the
                    // functionality that depends on this permission.
                    allowRecording = false;
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                // other 'case' lines to check for other
                // permissions this app might request.
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user enabled bluetooth.
                Toast.makeText(getActivity().getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getActivity().getApplicationContext(), "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Re-/Starts the speech recognition service
     *
     * @param view that was clicked
     */
    public void onClickMicrofon(View view) {
        // permission is either granted at runtime or beforehand
        if (!allowRecording && !permissionCheck())
            return;

        clearEditText();

        if (CheckConnection.isOnline(getActivity().getApplicationContext())) {
            // start service on first click
            if (!startListening) {
                AndroidSpeechRecognition.getInstance().startSpeechService(this);
                startListening = true;
            } else { //otherwise just restart listening
                AndroidSpeechRecognition instance = AndroidSpeechRecognition.getInstance();
                instance.restartListening();
            }
        } else {
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string.error_no_network, duration);
            toast.show();

        }
    }

    /**
     * Starts the discovery/connection process for a second device
     * @param view
     */
    private void onClickConnect(View view) {
        if(btService == null){
            btService = new BluetoothConnectionService((AppCompatActivity) getActivity());
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }else{
                btService.showDialog();

        }
    }

    /**
     * Remove any text in the EditText views
     */
    private void clearEditText() {
        if (et_speech_input != null)
            et_speech_input.setText(null);

        if (et_translation != null)
            et_translation.setText(null);
    }


    /**
     * Called when a response from the TranslationService is received.
     *
     * @param response
     */
    @Override
    public void onResponse(JSONObject response) {
        String translation;

        try {
            translation = response.getString("translation");
            getTranslationView().setText(translation);

            speak(translation);

        } catch (JSONException e) {
            e.printStackTrace();
            getTranslationView().setText(R.string.error_translation);
        }


    }

    /**
     *
     * @param sentence
     */
    private void speak(String sentence){
        if(initialised) {
            // language spoken
            int currLang = AndroidSpeechRecognition.getInstance().getLangCode();
            // language translated to
            currLang = LanguageDialogFragment.getOppositeCode(currLang);
            if(lastLangCode!=currLang) {
                textToSpeech.setLanguage(LanguageDialogFragment.getLocale(currLang));
                lastLangCode = currLang;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
            }

        }
    }

    /**
     * Callback method that an error has been occurred with the provided error code and optional
     * user-readable message.
     *
     * @param error
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        getTranslationView().setText(error.getCause().getMessage());
    }

    @Override
    public void onStop() {
        super.onStop();
        endSpeechService();
        if(textToSpeech != null)
            textToSpeech.stop();
        if(btService != null)
            btService.stop();
        TranslationService.getInstance(getActivity().getApplicationContext()).cancelRequests();
        Log.d(AndroidSpeechRecognition.TAG, "onStop SpeechRecognition");
    }

    @Override
    public void onPause() {
        super.onPause();
        endSpeechService();
        textToSpeech.stop();
        Log.d(AndroidSpeechRecognition.TAG, "onPause SpeechRecognition");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        endSpeechService();
        if(textToSpeech != null)
            textToSpeech.shutdown();
        if(btService != null)
            btService.shutdown();
        Log.d(AndroidSpeechRecognition.TAG, "onDestroy SpeechRecognition");
    }

    /**
     * Stop Listening and destroy SpeechService object
     */
    private void endSpeechService() {
        AndroidSpeechRecognition.getInstance().stopListening(true);
        AndroidSpeechRecognition.getInstance().shutdownService();
        startListening = false;
    }
}
