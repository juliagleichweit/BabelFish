# BabelFish

BabelFish translates spoken content from one language to another.
The following services are used for:

- Speech recognition: Android API SpeechRecognizer
- Translation: 
- Speech synthesis: 

The service is limited to German and English. 

# Current Status

On start the application checks if you have already selected a target language. If not it prompts you to do so and updates the selected language in the status bar. The target language can always be changed via the status bar.

Clicking the microfon starts the speech recognition process. Partial results are shown at the top and the returned end result by SpeechRecognizer is shown at the bottom

![BabelFish Demo](/current_stat/babelfish_0.1.0.gif)
 
![BabelFish Demo](/current_stat/babelfish_0.2.0.gif)