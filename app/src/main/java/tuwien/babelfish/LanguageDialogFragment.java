package tuwien.babelfish;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class LanguageDialogFragment extends DialogFragment {
    OnLanguageSelectedListener callback;

    public void setOnLanguageSelectedListener(Activity activity){
        callback = (OnLanguageSelectedListener) activity;
    }

    //Container Activity must implement this interface
    //used to pass messages from fragment to containing UI
    public interface OnLanguageSelectedListener{
        void onSelectedLanguage(int lang_Code);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.choose_lang_dialog, container, false);

        ImageView iv_DE = rootView.findViewById(R.id.dialog_lang_DE);
        iv_DE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onSelectedLanguage(1);
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
               callback.onSelectedLanguage(2);
               dismiss();
            }
        });

        return rootView;
    }

}
