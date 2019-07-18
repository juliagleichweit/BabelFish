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
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import java.util.HashMap;

import tuwien.babelfish.LanguageDialogFragment;
import tuwien.babelfish.R;
import tuwien.babelfish.bluetooth.BluetoothConnectionService;
import tuwien.babelfish.util.CheckConnection;
import tuwien.babelfish.util.PermissionInfoDialog;

/**
 * Fragment used to implement the translation pipeline (speech-to-text -> translation -> text-to-speech)
 */
public class SpeechService extends Fragment implements Response.Listener<JSONObject>, Response.ErrorListener, BluetoothConnectionService.OnInputListener {

    private static final int REQUEST_RECORD_AUDIO = 5;
    private static final int REQUEST_COARSE_LOCATION = 10;
    private static final int REQUEST_ENABLE_BLUETOOTH = 15;
    private static final String SPEAKER = "Speaker";
    private static final String TAG = "SpeechService";

    private boolean allowRecording = false;

    private EditText et_speech_input;
    private EditText et_translation;
    private ImageView iv_bot;
    private ImageView microphone_icon;

    private boolean tts_initialised = false;
    private int lastLangCode;

    private TextToSpeech textToSpeech;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnectionService btService;

    private boolean permanentSpeak = true;
    private boolean lastIconState = false;

    private FragmentManager fm;
    private Bundle speakParams;
    private HashMap<Object, Object> speakParamsMap;
    private Runnable restartService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.speech_process, container, false);

        et_speech_input = rootView.findViewById(R.id.et_spoken_text);
        et_translation = rootView.findViewById(R.id.et_translated_text);

        setupMicrophoneView(rootView);
        setupBTView(rootView);
        setupSpeakerView(rootView);

        // initialize TextToSpeech
        textToSpeech = new TextToSpeech(getActivity().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts_initialised = true;
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
            }
        });

        textToSpeech.setOnUtteranceProgressListener(AndroidSpeechRecognition.getInstance(this));
        speakParams = new Bundle();
        speakParams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, AndroidSpeechRecognition.UtteranceID);
        speakParamsMap = new HashMap<>();
        speakParams.putAll(speakParams);

        restartService = () -> AndroidSpeechRecognition.getInstance(this).restartListening();
        return rootView;
    }

    /**
     * Initialize views for speech to text.
     *
     * @param rootView parent layout of the activity
     */
    private void setupMicrophoneView(View rootView) {
        microphone_icon = rootView.findViewById(R.id.record_button);
        microphone_icon.setBackgroundResource(R.drawable.animation_microphone);
        AnimationDrawable animationMicrophone = (AnimationDrawable) microphone_icon.getBackground();
        AndroidSpeechRecognition.getInstance(this).setAnimationDrawable(animationMicrophone);

        microphone_icon.setOnClickListener(this::onClickMicrophone);
    }


    /**
     * Initialize views for text to speech. Uses SharedPreferences to determine if translations
     * should be spoken or not.
     *
     * @param rootView parent layout of the activity
     */
    private void setupSpeakerView(View rootView) {
        ImageView iv_speaker = rootView.findViewById(R.id.iv_speak);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int drawable = permanentSpeak ? R.drawable.speaker : R.drawable.speaker_off;
        iv_speaker.setImageDrawable(getActivity().getResources().getDrawable(drawable));

        // if not present we still want it to be true
        permanentSpeak = sharedPref.getBoolean(SPEAKER, true);

        iv_speaker.setOnClickListener(view ->
        {
            permanentSpeak = !permanentSpeak;

           int newDrawable = permanentSpeak ? R.drawable.speaker : R.drawable.speaker_off;

            // store the preference if the user wants the translation to be always spoken
            iv_speaker.setImageDrawable(getActivity().getResources().getDrawable(newDrawable));
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(SPEAKER, permanentSpeak);
            editor.apply();

            // we want text to speech and a translation is present
            if (permanentSpeak && !TextUtils.isEmpty(et_translation.getText()))
                speak(et_translation.getText().toString(), true, false);
        });
    }

    /**
     * Initialize views for Bluetooth.
     * Toasts an error message if Bluetooth is not available on this device.
     *
     * @param rootView parent layout of the activity
     */
    private void setupBTView(View rootView) {
        iv_bot = rootView.findViewById(R.id.iv_bot);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            changeIcon(false);
            Toast.makeText(getActivity().getApplicationContext(), R.string.bt_not_supported, Toast.LENGTH_SHORT);
        } else {
            iv_bot.setOnClickListener(this::onClickConnect);
        }

        btService = BluetoothConnectionService.getInstance((AppCompatActivity) getActivity());
        btService.setConnectionListener(this);
        btService.setFragmentManager(fm);
        if(bluetoothAdapter.isEnabled())
            btService.start();
    }

    /**
     * Set FragmentManager from Activity.
     * Fragment.getActivity().getFragmentManager() returns manager with illegal states
     */
    public void setFragmentManager(FragmentManager fm) {
        this.fm = fm;
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
    private boolean audioPermissionCheck() {

        String msg = getActivity().getResources().getString(R.string.missing_record_permission);
        return checkPermission(Manifest.permission.RECORD_AUDIO, msg, REQUEST_RECORD_AUDIO);
    }

    /**
     * Checks if permissions ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION is given
     *
     * @return true if permission granted; otherwise false
     */
    private boolean locationPermissionCheck() {

        String msg = getActivity().getResources().getString(R.string.missing_location_permission);
        return checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, msg, REQUEST_COARSE_LOCATION);
    }

    /**
     * Checks if the permission is granted. If not pops up dialog with additional info
     *
     * @param permission  permission code
     * @param msg         to be displayed in the permission dialog
     * @param requestCode integer identifying your request
     * @return true if granted, otherwise false
     */
    private boolean checkPermission(String permission, String msg, int requestCode) {

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    permission)) {
                // returns true if the user has previously denied the request, and returns
                // false if a user has denied a permission and selected the Don't ask again option
                PermissionInfoDialog.show(getActivity(), msg);
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{permission}, requestCode);
                return false;
            }
        } else {
            // Permission has already been granted
            return true;
        }
        return false;
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
                    // permission denied.
                    allowRecording = false;
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Re-/Starts the speech recognition service
     *
     * @param view that was clicked
     */
    private void onClickMicrophone(View view) {
        // permission is either granted at runtime or beforehand
        if (!allowRecording && !audioPermissionCheck())
            return;

        clearEditText();

        if (CheckConnection.isOnline(getActivity().getApplicationContext())) {
            // start service on first click
            AndroidSpeechRecognition.getInstance(this).restartListening();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), R.string.error_no_network, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Starts the discovery/connection process for a second device
     *
     * @param view listener is attached to
     */
    private void onClickConnect(View view) {
        if (!locationPermissionCheck())
            return;

        // do we have a running instance we want to disconnect
        if (btService.isConnected()) {
            btService.stopClient();
            changeIcon(false);
            Toast.makeText(getActivity().getApplicationContext(), R.string.bt_end_connection, Toast.LENGTH_SHORT).show();
        } else { // we want to start a connection
            if (!bluetoothAdapter.isEnabled()) {  // we have to enable bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            } else { // we can show the dialog
                btService.showDialog();
            }
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

    @Override
    public void changeIcon(boolean connected) {
        if (lastIconState == connected)
            return;

        lastIconState = connected;
        int drawable = connected ? R.drawable.bot : R.drawable.bot_grey;

        iv_bot.post(()-> iv_bot.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), drawable)));
    }

    @Override
    public void readInput(String input) {
        Log.d(TAG, "onReadInput");
        // post event to message queue, otherwise wrong Thread exception
        getTranslationView().post(()->getTranslationView().setText(input));

        // let the text be read out loud
        if (permanentSpeak) {
            speak(input, false, false);
        }

        restartSpeechRecognizer();
        Log.e(TAG,"restart from readInput BT");
    }

    /**
     * Called when a response from the TranslationService is received.
     * If an error occurred onErrorResponse is called instead
     *
     * @param response from the translation service
     */
    @Override
    public void onResponse(JSONObject response) {
        String translation;
        try {
            translation = response.getString("translation");
            getTranslationView().setText(translation);

            if (permanentSpeak) {
                speak(translation, true, !btService.isConnected());
            } else {
                if(!btService.isConnected()) {
                    restartSpeechRecognizer();
                    Log.d(TAG, "restart from onResponse");
                }
            }

            if (btService.isConnected()) {
                btService.write(translation.getBytes());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            getTranslationView().setText(R.string.error_translation);
        }
    }

    /**
     * Callback method that an error has been occurred with the provided error code and optional
     * user-readable message.
     *
     * @param error passed from Volley
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Error translation: " + error.getMessage());
        System.out.println("\n\n " + error.networkResponse.statusCode);
        getTranslationView().setText(R.string.error_translation);
    }

    /**
     * Uses the Android default TextToSpeech implementation to read the sentence out loud
     *
     * @param sentence    to be voiced
     * @param useOpposite if true uses the target language; false uses the current input language
     * @param trackUtterance true if speech output should be used to restart SpeechRecognizer,
     *                       false otherwise
     */
    private synchronized void speak(String sentence, boolean useOpposite, boolean trackUtterance) {
        if (tts_initialised) {
            // language spoken
            int currLang = AndroidSpeechRecognition.getInstance(this).getLangCode();

            if (useOpposite)// language translated to
                currLang = LanguageDialogFragment.getOppositeCode(currLang);

            if (lastLangCode != currLang) {
                textToSpeech.setLanguage(LanguageDialogFragment.getLocale(currLang));
                lastLangCode = currLang;
            }

            Log.d(TAG, "Speak + utterance: " + trackUtterance);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                Bundle params = null;
                String id = null;

                if(trackUtterance){
                    params =speakParams;
                    id = AndroidSpeechRecognition.UtteranceID;
                }

                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, params, id);
            } else {
                HashMap params = trackUtterance ? speakParamsMap:null;
                textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, params);
            }
        }
    }

    /**
     * Restarts the SpeechRecognizer after 2s
     */
    public void restartSpeechRecognizer() {
        // post event to message queue, otherwise wrong Thread exception
        microphone_icon.postDelayed(restartService, 2000);
    }

    @Override
    public void onStop() {
        super.onStop();

        stopSpeechService();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        //stopSpeechService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDesrtoy");

        if (btService != null)
            btService.shutdown();

        if (textToSpeech != null)
            textToSpeech.shutdown();

        stopSpeechService();
        AndroidSpeechRecognition.getInstance(this).shutdownService();
    }

    /**
     * Stop Listening and destroy SpeechService object
     */
    private void stopSpeechService() {

        AndroidSpeechRecognition.getInstance(this).stopListening(false);
        AndroidSpeechRecognition.getInstance(this).stopAnimation();

        // remove pending restarts
        microphone_icon.removeCallbacks(restartService);

        if (textToSpeech != null)
            textToSpeech.stop();

        if (btService != null)
            btService.stop();

        //removing pending translations
        TranslationService.getInstance(getActivity().getApplicationContext()).cancelRequests();
    }


}
