/**
 * BabelFish
 * Copyright (C) 2019  Julia Gleichweit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tuwien.babelfish.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import tuwien.babelfish.CheckConnection;
import tuwien.babelfish.LanguageDialogFragment;
import tuwien.babelfish.R;

/**
 *  Implements the standard API SpeechRecognizer to convert speech to text
 *  Implementing RecognitionListener suppresses the Google Dialog
 */
public class AndroidSpeechRecognition implements RecognitionListener {

    public static final String TAG = "AndroidSpeechRec";
    private static AndroidSpeechRecognition instance;

    private final int REQUEST_SPEECH_RECOGNIZER = 10;

    private int langCode = LanguageDialogFragment.LANG_EN;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognitionIntent;
    private boolean listening = false;

    private SpeechService callingActivity;
    private Context ctx;

    private AndroidSpeechRecognition() {

        speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // has to be set
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // allow partial results to give instant feedback
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

    }

    public static AndroidSpeechRecognition getInstance(){
        if(instance==null)
            instance = new AndroidSpeechRecognition();

        return instance;
    }

    /**
     * Adds language preference to SpeechRecognizer to improve results
     * @param langCode {@link tuwien.babelfish.LanguageDialogFragment}
     */
    public void setLangPreference(int langCode){
        this.langCode = langCode;

        if(speechRecognizer != null) {
            speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langCode);
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    /**
     * Returns the current used language code of the SpeechRecognizer (i.e. spoken language)s
     * @return
     */
    public int getLangCode(){
        return this.langCode;
    }
    /**
     * Starts speech recognition service via Intent
     * @param activity calling activity
     */
    public void startSpeechService(SpeechService activity){
        callingActivity = activity;
        ctx = callingActivity.getActivity().getApplicationContext();
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, callingActivity.getActivity().getPackageName());

        restartService();
    }

    /**
     * Restarts speech recognition service via Intent
     */
    private void restartService(){
        // check if recognition is supported by the phone
        if(SpeechRecognizer.isRecognitionAvailable(callingActivity.getActivity())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(callingActivity.getActivity());

            // set listener to surpress Google dialog
            speechRecognizer.setRecognitionListener(this);

            // start listening
            speechRecognizer.startListening(speechRecognitionIntent);
            Log.d(TAG, "startSpeechService");
        }else {
            // inform user that device does not support this function
            callingActivity.getTranslationView().setText(R.string.speech_not_supported);
        }
    }

    /**
     * Calls TranslationService to translate the text to current target language
     * @param text to be translated
     */
    private void translate(String text){
        // do we got text to translate
        if(text == null || text.isEmpty())
            return;

        if(!CheckConnection.isOnline(ctx)){
            // make toast
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(ctx,  "No Internet connection", duration);
            toast.show();
        }else{
            String from = LanguageDialogFragment.getCode(langCode);
            String to = LanguageDialogFragment.getCode(LanguageDialogFragment.getOppositeCode(langCode));
            TranslationService.getInstance(ctx).translate(text, from,to,callingActivity,callingActivity);
        }
    }

    public void restartListening(){
        if(speechRecognizer == null) {
            restartService();
        }else {
            // if(!listening)
            Log.d(TAG, "restart listening");
            speechRecognizer.cancel();
            speechRecognizer.startListening(speechRecognitionIntent);
        }
    }

    /**
     * Stop listening to speech input
     * @param force when true cancels service;
     */
    public void stopListening(boolean force){
        if(speechRecognizer == null)
            return;

        if(listening)
            if(force) {
                speechRecognizer.cancel();
            }else {
                speechRecognizer.stopListening();
            }
    }

    /**
     * Shuts down SpeechRecognizer
     */
    public void shutdownService(){
        Log.d(TAG, "shutdown AndroidSpeechRecognition");
        if(speechRecognizer != null)
            speechRecognizer.destroy();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG, "onReadyForSpeech");
        listening = true;
    }

    @Override
    public void onBeginningOfSpeech() {
        //Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float v) {
        //Log.d(TAG, "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        //Log.d(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndofSpeech");
        listening = false;
        //restartListening();
    }

    @Override
    public void onError(int i) {
        Log.d(TAG,  "error " +  i);

        // 8
        //SpeechRecognizer.ERROR_RECOGNIZER_BUSY

        Log.d(TAG, "SpeechRecognizer ErrorCode: " + i);
        //setViewText(callingActivity.getTranslationView(), "SpeechRecognizer ErrorCode: " + i);
        listening = false;

    }

    @Override
    public void onResults(Bundle bundle) {
         // get all results
        ArrayList data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        //results have been found
       /* if(data != null) {
            // display all results
            String word = (String) data.get(data.size() - 1);
            setViewText(callingActivity.getSpokenView(), word);

            translate(word);
        }else{ // no matching results found
            Log.d(TAG, "No results");
        }*/
       String text  = callingActivity.getSpokenView().getText().toString();
       translate(text);
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        // get all partial results
        ArrayList data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if(data != null) {
            // display all results
            // gives instant recognition feedback to the user
            String word = (String) data.get(data.size() - 1);
            setViewText(callingActivity.getSpokenView(), word);

        }else{ //no matching results found
         Log.d(TAG, "No partial results");
        }
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.d(TAG, "onEvent  " + i );
    }

    /**
     * Display recognition feedback to user
     *
     * @param v view to be written to
     * @param msg  to be displayed
     */
    private void setViewText(EditText v, String msg){
        if(v != null)
            v.setText(msg);
    }


}
