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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Locale;

/**
 * DialogFragment used to edit target language preferences
 */
public class LanguageDialogFragment extends DialogFragment {

    private OnLanguageSelectedListener callback;

    public static final int LANG_DE = 1;
    public static  final int LANG_EN = 2;
    private boolean isLanguageSelected = false;
    /**
     * Callback object to pass language selection changes to
     * @param activity must not be null
     */
    public void setOnLanguageSelectedListener(Activity activity){
        callback = (OnLanguageSelectedListener) activity;
    }

    /**
     * Container Activity must implement this interface
     * used to pass messages from fragment to containing UI if language selection changes
     */
    public interface OnLanguageSelectedListener{
        /**
         * Notifies observer about target language changes
         *
         * @param langCode new user preference
         */
        void onSelectedLanguage(int langCode);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.choose_lang_dialog, container, false);

        ImageView iv_DE = rootView.findViewById(R.id.dialog_lang_DE);
        iv_DE.setOnClickListener(view -> {
            callback.onSelectedLanguage(LANG_DE);
            isLanguageSelected = true;
            dismiss();
        });

        ImageView iv_EN = rootView.findViewById(R.id.dialog_lang_EN);
        iv_EN.setOnClickListener(view -> {
               callback.onSelectedLanguage(LANG_EN);
                isLanguageSelected = true;
               dismiss();
        });

        // user must choose one language
        setCancelable(false);
        return rootView;
    }

    /**
     * Returns the String representation of the language code
     * 1 - de
     * 2 - en
     * @param langCode int representation of the language code
     */
    public static String getCode(int langCode){
        switch (langCode) {
            case LANG_DE:
                return "de";
            case LANG_EN:
                return "en";
            default: return "en";
        }
    }

    /**
     * Dismiss the fragment and its dialog.
     * Checks if a language was selected before closing the dialog.
     * If not English is set as default target language
     */
    @Override
    public void dismiss() {
        super.dismiss();
        if(!isLanguageSelected)
            callback.onSelectedLanguage(LANG_EN);
    }

    /**
     * Returns the Locale of the language code
     * 1 - de
     * 2 - en
     * @param langCode int representation of the language code
     */
    public static Locale getLocale(int langCode){
        switch (langCode) {
            case LANG_DE:
                return Locale.GERMAN;
            case LANG_EN:
                return Locale.ENGLISH;
            default: return Locale.ENGLISH;
        }
    }

    /**
     * Returns the String representation of the opposite language code
     * 1 - de
     * 2 - en
     * @param langCode int representation of the language code
     */
    public static int getOppositeCode(int langCode){
        switch (langCode) {
            case LANG_DE:
                return LANG_EN;
            case LANG_EN:
                return LANG_DE;
            default: return  LANG_DE;
        }
        }
}
