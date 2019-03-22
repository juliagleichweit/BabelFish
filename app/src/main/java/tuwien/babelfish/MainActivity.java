package tuwien.babelfish;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LanguageDialogFragment.OnLanguageSelectedListener{

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add custom toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Get a string resource from your app's Resources
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
                toast.show();*/
                FragmentManager fm = getFragmentManager();
                LanguageDialogFragment dialogFragment = new LanguageDialogFragment();
                dialogFragment.show(fm, "Sample Fragment");
            }
        });
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof LanguageDialogFragment) {
            LanguageDialogFragment langDiaFrag= (LanguageDialogFragment) fragment;
            langDiaFrag.setOnLanguageSelectedListener(this);
        }
    }

    @Override
    public void onSelectedLanguage(int lang_Code) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, lang_Code == 1 ? "Deutsch": "Englisch", duration);
        toast.show();
    }
}
