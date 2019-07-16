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
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import tuwien.babelfish.LanguageDialogFragment;
import tuwien.babelfish.R;
import tuwien.babelfish.util.CheckConnection;

/**
 *  Implements the standard API SpeechRecognizer to convert speech to text
 *  Implementing RecognitionListener suppresses the Google Dialog
 */
public class AndroidSpeechRecognition extends UtteranceProgressListener implements RecognitionListener {

    public static final String UtteranceID = "babelfish.SpeechRec";
    private final String TAG = "AndroidSpeechRec";
    private static AndroidSpeechRecognition instance;

    private int langCode = LanguageDialogFragment.LANG_EN;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognitionIntent;private boolean listening = false;

    private SpeechService callingClass;
    private Context ctx;
    private AnimationDrawable animationMicrophone;

    private AndroidSpeechRecognition() {
        speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // has to be set
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // allow partial results to give instant feedback
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    }

    @Override
    public void onStart(String s) {
        Log.d(TAG, "Utterance onStart");
    }

    @Override
    public void onDone(String id) {
        Log.d(TAG, "Utterance onDone: " + id);
        if(UtteranceID.equals(id))
            callingClass.restartSpeechRecognizer();
    }

    /**
     * @param s
     * @deprecated
     */
    @Override
    public void onError(String s) {

    }

    /**
     * Creates a new AndroidSpeechRecognition instance and returns it. If the instance
     * was already created it is returned.
     *

     * @return AndroidSpeechRecognition instance
     */
    public static AndroidSpeechRecognition getInstance(SpeechService callingClass){
        if(instance==null) {
            instance = new AndroidSpeechRecognition();
        }

        if(callingClass != null) {
            instance.callingClass = callingClass;
        }
        return instance;
    }

    /**
     * Sets the AnimationDrawable giving user feedback about recognizer state.
     * Start is called on this animation when the recognizer is ready for speech (stop on end of speech).
     *
     * @param drawable non-null AnimationDrawable object
     */
    public void setAnimationDrawable(AnimationDrawable drawable){
        this.animationMicrophone = drawable;
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
     * Returns the current used language code of the SpeechRecognizer
     * @return source language code
     */
    public int getLangCode(){
        return this.langCode;
    }

    /**
     * Starts speech recognition service via Intent
     *   activity calling activity
     */
    public void startSpeechService( ){
        Log.d(TAG, "startSpeechService");
        ctx = callingClass.getActivity().getApplicationContext();
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, callingClass.getActivity().getPackageName());

        restartService();
    }

    /**
     * Restarts speech recognition service via Intent
     */
    private void restartService(){
        // check if recognition is supported by the phone
        if(SpeechRecognizer.isRecognitionAvailable(callingClass.getActivity())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(callingClass.getActivity());

            // set listener to suppress Google dialog
            speechRecognizer.setRecognitionListener(this);

            // start listening
            speechRecognizer.startListening(speechRecognitionIntent);
            Log.d(TAG, "startSpeechService");
        }else {
            // inform user that device does not support this function
            callingClass.getTranslationView().setText(R.string.speech_not_supported);
        }
    }
    
    public void restartListening( ){

        if(animationMicrophone.isRunning()){
            speechRecognizer.cancel();
            stopAnimation();
            return;
        }

        if(speechRecognizer == null) {
            startSpeechService();
        }else {
            Log.d(TAG, "restartSpeechRecognizer listening");
            //speechRecognizer.cancel();
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

    /**
     * Calls TranslationService to translate the text to current target language
     * @param text to be translated
     */
    private void translate(String text){
        // do we got text to translate
        if(text == null || text.isEmpty())
            return;

        if(!CheckConnection.isOnline(ctx)){
            Toast.makeText(ctx,  "No Internet connection", Toast.LENGTH_SHORT).show();
        }else{
            String from = LanguageDialogFragment.getCode(langCode);
            String to = LanguageDialogFragment.getCode(LanguageDialogFragment.getOppositeCode(langCode));
            TranslationService.getInstance(ctx).translate(text, from,to, callingClass, callingClass);
        }
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG, "onReadyForSpeech");
        listening = true;
        animationMicrophone.start();
        //animationMicrophone.setVisible(true, true);
    }

    @Override
    public void onBeginningOfSpeech() {
        //Log.d(TAG, "onBeginningOfSpeech");
        listening = true;
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
        stopAnimation();
        listening = false;
    }

    @Override
    public void onError(int i) {
        Log.d(TAG,  "error " +  i);

        // 8
        switch (i) {
            case 8:
                callingClass.getTranslationView().setText("Error_Code 8: SpeechRecognizer.ERROR_RECOGNIZER_BUSY");
                break;
            case 6: callingClass.getTranslationView().setText("Error_Code 6: SpeechRecognizer.ERROR_SPEECH_TIMEOUT");
                break;
                case 7 : callingClass.getTranslationView().setText("Error_Code 7: SpeechRecognizer.ERROR_NO_MATCH");
                   // listening = false;
                    break;

        }
        //6
        Log.d(TAG, "SpeechRecognizer ErrorCode: " + i);
        stopAnimation();

    }

    /**
     * Stops the animation and sets it invisible.
     */
    private void stopAnimation(){
        animationMicrophone.stop();
        animationMicrophone.selectDrawable(0);
    }
    @Override
    public void onResults(Bundle bundle) {
        // use partial results, often more accurate than the end result
       String text  = callingClass.getSpokenView().getText().toString();
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
            setViewText(callingClass.getSpokenView(), word);

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
