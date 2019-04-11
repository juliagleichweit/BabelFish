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

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Fragment used to record speech and convert it to text
 */
public class VoiceRecognition extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 15;
    private boolean allowRecording = false;
    private ImageView microfon_icon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.voice_recognition, container, false);

        //allowRecording = permissionCheck();
        microfon_icon = rootView.findViewById(R.id.microfon);
        microfon_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissionCheck();
            }
        });


        return rootView;
    }

    public boolean permissionCheck() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                // returns true if the user has previously denied the request, and returns
                // false if a user has denied a permission and selected the Don't ask again option
                showPermissionInfoDialog();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_XXX is an app-defined int constant.
                // The callback method gets the result of the request.
                return false;
            }
        } else {
            // Permission has already been granted
            //microfon_icon.setImageDrawable(getResources().getDrawable(R.drawable.microphone));
            return true;
        }
        return false;
    }

    private void showPermissionInfoDialog() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_missing_permission);
        builder.setMessage(R.string.missing_record_permission);
        builder.setCancelable(true);

        // on ok go to settings page
        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    dialogInterface.dismiss();

                    Intent settingsIntent = new Intent();

                    // we want to display the application's settings page
                    settingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    settingsIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //identify our app
                    settingsIntent.setData(Uri.fromParts("package", getActivity().getPackageName(), null)); //parse("package: " + getActivity().getPackageName()));

                    //start our intent
                    startActivity(settingsIntent);
                }catch (Exception e){

                    Context context = getActivity().getApplicationContext();
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, e.getMessage(), duration);
                    toast.show();
                }
            }
        });
        // automatically creates and immediately shows the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    allowRecording = true;

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    allowRecording = false;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
