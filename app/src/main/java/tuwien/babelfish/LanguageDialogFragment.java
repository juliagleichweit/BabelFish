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
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Fragment used to edit target language preferences
 */
public class LanguageDialogFragment extends DialogFragment {

    static LanguageDialogFragment newInstance() {
        return new LanguageDialogFragment();
    }

    OnLanguageSelectedListener callback;

    public static final int LANG_DE = 1;
    public static  final int LANG_EN = 2;

    private int lastLang = -1;

    public void setOnLanguageSelectedListener(Activity activity){
        callback = (OnLanguageSelectedListener) activity;
    }

    //Container Activity must implement this interface
    //used to pass messages from fragment to containing UI
    public interface OnLanguageSelectedListener{
        void onSelectedLanguage(int langCode);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.choose_lang_dialog, container, false);

        ImageView iv_DE = rootView.findViewById(R.id.dialog_lang_DE);
        iv_DE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onSelectedLanguage(LanguageDialogFragment.LANG_DE);
                dismiss();
            }
        });
        ImageView iv_EN = rootView.findViewById(R.id.dialog_lang_EN);
        iv_EN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Context context = getActivity().getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, view.getContentDescription(), duration);
                toast.show();*/
               callback.onSelectedLanguage(LanguageDialogFragment.LANG_EN);
               dismiss();
            }
        });

        return rootView;
    }

}
