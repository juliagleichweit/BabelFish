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

package tuwien.babelfish;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import tuwien.babelfish.speech.AndroidSpeechService;
import tuwien.babelfish.speech.SpeechService;

/**
 * Fragment used to record speech and convert it to text
 */
public class SpeechRecognition extends Fragment implements SpeechService {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 5;
    private boolean allowRecording = false;

    private EditText et_speech_input;
    private EditText et_translation;
    private boolean startListening = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.speech_process, container, false);

        //allowRecording = permissionCheck();
        ImageView microfon_icon = rootView.findViewById(R.id.record_button);
        microfon_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickMicrofon(view);
            }
        });

        et_speech_input = rootView.findViewById(R.id.et_spoken_text);
        et_translation = rootView.findViewById(R.id.et_translated_text);

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
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

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
                    settingsIntent.setData(Uri.fromParts("package", getActivity().getPackageName(), null)); //parse("package: " + getActivity().getPackageName()));

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
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
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


    /**
     * Re-/Starts the speech recognition service
     * @param view that was clicked
     */
    public void onClickMicrofon(View view) {
        // permission is either granted at runtime or beforehand
        if (!allowRecording && !permissionCheck())
            return;

        /*Context context = getActivity().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, view.getContentDescription(), duration);
        toast.show();*/

        clearEditText();

        // start service on first click
        if (!startListening) {
            AndroidSpeechService.getInstance().startSpeechService(this);
            startListening = true;
        } else { //otherwise just restart listening
            AndroidSpeechService instance = AndroidSpeechService.getInstance();
            instance.restartListening();
        }
    }

    /**
     * Remove any text in the EditText views
     */
    private void clearEditText() {
        if(et_speech_input != null)
            et_speech_input.setText(null);

        if(et_translation != null)
            et_translation.setText(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        endSpeechService();
        Log.d(AndroidSpeechService.TAG, "onStop SpeechRecognition");
    }

    @Override
    public void onPause() {
        super.onPause();
        endSpeechService();
        Log.d(AndroidSpeechService.TAG, "onPause SpeechRecognition");
    }

    /**
     * Stop Listening and destroy SpeechService object
     */
    private void endSpeechService() {
        AndroidSpeechService.getInstance().stopListening(true);
        AndroidSpeechService instance = AndroidSpeechService.getInstance();
        instance.stopListening(true);
        instance.destroy();
        startListening = false;
    }

}
