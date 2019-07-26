# BabelFish

BabelFish translates spoken content from one language to another. Via Bluetooth devices can interact with each other to send the respective translation to the other person.
The following services are used:

- Speech recognition: Android API [SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer.html)
- Translation: Free Online Translator Frengly (http://www.frengly.com/)
- Speech synthesis: Android API [TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech)

The service is limited to German and English. 

# Current Status
<figure>
  <img src="/current_stat/0.3.0/overview_babelfish.png"/>
  <figcaption>Overview on user's device.</figcaption>
</figure>

#### (1) Language Selection 
On start the application checks if you have already selected a target language. If not it prompts you to do so and updates the selected language in the status bar. The target language can always be changed via the status bar. 
The language icon in the front depicts the target language. In the above case we want to translate into English. 
The icon in the back is the source language (we are speaking German). 

#### (2) Spoken Text
Recognition results are given in a real-time fashion by displaying partial results while speaking. After the last detected word by the SpeechRecognizer  the result is passed to the translation service.

#### (3) Translated Text
The result of the Frengly translation service is displayed here.
 If a connection to a different device has been established the translated text is send to the other device via Bluetooth. The receiving device displays the translation in its translation view and uses the TextToSpeech service from Android to speak the text. The service uses English if your target language is English, otherwise German. It is assumed that connected devices always have different target languages. 
 In the case of an error it is also displayed in the translation view, but not sent to the connected device.
 
#### (4) Listen
Uses the TextToSpeech service to vocalize the translation in your target language. On default the translations are always converted into speech.

#### (5) Connect
<img style="float: right;" src="/current_stat/0.3.0/connect_dialog.jpg">
Opens the dialog to connect to an available bluetooth device. Both devices must have the app installed and Bluetooth must be enabled.
If Bluetooth is not enabled, the app asks for permission to do so. If granted Bluetooth is enabled automatically. 

After choosing a device it tries to establish a connection (also indicated by the changed title [Connecting...]). If successful the dialog is closed and the bot is colored. Otherwise an error message is displayed and the bot stays grey.

#### (6) Speak
Clicking the microfon starts the speech recognition process. A working internet connection must be present. If not an appropriate error message is displayed.
The beginning and end of the recognition process is accompanied by a short sound and animation. 
Permission to use the microfon has to be granted.

If the speech input could be processed and translated the speech recognition process is automatically restarted:
* after 2 seconds if the translation is only presented in text
* 2 seconds after the speech synthesis is finished