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

package tuwien.babelfish;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import tuwien.babelfish.speech.AndroidSpeechService;

/**
 * This is used to launch speacch processing activities
 */
public class MainActivity extends AppCompatActivity implements LanguageDialogFragment.OnLanguageSelectedListener {

    private Button button;
    private int lastLang = -1;

    private Menu optionsMenu;
    private MenuItem langPrefMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add custom toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        //  store the menu to var when creating options menu
        optionsMenu = menu;
        langPrefMenu = optionsMenu.findItem(R.id.action_lang);
        checkLanguagePref();
        return true;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof LanguageDialogFragment) {
            LanguageDialogFragment langDiaFrag= (LanguageDialogFragment) fragment;
            langDiaFrag.setOnLanguageSelectedListener(this);
        }

        if(fragment instanceof SpeechRecognition){

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
         super.onRequestPermissionsResult(requestCode,permissions,grantResults);

         // push results to fragments, needed for < API 23;
        // otherwise requestPermission could be directly called from a Fragment and the result
        // processed in Fragment.onRequestPermissionResult
         for(android.support.v4.app.Fragment f: getSupportFragmentManager().getFragments())
             f.onRequestPermissionsResult(requestCode, permissions,grantResults);
    }
    private void checkLanguagePref() {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int lang = sharedPref.getInt(getString(R.string.first_entry), -1);
        lastLang = lang;

        if(lastLang ==-1)
        {
            FragmentManager fm = getFragmentManager();
            LanguageDialogFragment dialogFragment = new LanguageDialogFragment();
            dialogFragment.show(fm, "Sample Fragment");
        }
        updateLangIcon(lang);
    }

    private void updateLangIcon(int langCode){
        //MenuItem menu_lang = optionsMenu.findItem(R.id.action_lang);
        lastLang = langCode;

        //set correct source language
        AndroidSpeechService.getInstance().setLangPreference(LanguageDialogFragment.getOppositeCode(langCode));
        if(langPrefMenu !=null) {
            switch (langCode) {
                case LanguageDialogFragment.LANG_DE:
                    langPrefMenu.setIcon(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.en_to_de));
                    langPrefMenu.setVisible(true);

                    break;
                case LanguageDialogFragment.LANG_EN:
                    langPrefMenu.setIcon(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.de_to_en));
                    langPrefMenu.setVisible(true);
                    break;
                default:
                    langPrefMenu.setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_lang:
                // User chose the "Language" item, open  target language preference
                FragmentManager fm = getFragmentManager();
                LanguageDialogFragment dialogFragment = new LanguageDialogFragment();
                dialogFragment.show(fm, "Sample Fragment");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSelectedLanguage(int langCode) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, langCode == 1 ? "Deutsch": "Englisch", duration);
        toast.show();

        //only consume real preference changes
        if(lastLang!=langCode) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.first_entry), langCode);
            editor.apply();

            updateLangIcon(langCode);
        }
    }

  @Override
    public void onStop() {
        super.onStop();
        //endSpeechService();
    }

    @Override
    public void onPause() {
        super.onPause();
       // endSpeechService();
    }
  /*
    private void endSpeechService(){
        AndroidSpeechService.getInstance().stopListening(true);
        AndroidSpeechService instance = AndroidSpeechService.getInstance();
        instance.stopListening(true);
        instance.destroy();
    }*/
}
