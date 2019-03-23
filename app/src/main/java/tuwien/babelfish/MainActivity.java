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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        /*// Get a string resource from your app's Resources
        String hello = getResources().getString(R.string.first_entry);

        // Or supply a string resource to a method that requires a string
        TextView textView = findViewById(R.id.hello_world);
        textView.setText(R.string.first_entry);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, "Seventeen right here!", duration);
                toast.show();
                FragmentManager fm = getFragmentManager();
                LanguageDialogFragment dialogFragment = new LanguageDialogFragment();
                dialogFragment.show(fm, "Sample Fragment");
            }
        });*/
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

        if(langPrefMenu !=null) {
            switch (langCode) {
                case LanguageDialogFragment.LANG_DE:
                    langPrefMenu.setIcon(getResources().getDrawable(R.drawable.de));
                    langPrefMenu.setVisible(true);
                    break;
                case LanguageDialogFragment.LANG_EN:
                    langPrefMenu.setIcon(getResources().getDrawable(R.drawable.en));
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
}
