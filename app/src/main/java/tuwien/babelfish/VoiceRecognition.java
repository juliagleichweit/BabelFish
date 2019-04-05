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
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment used to record speech and convert it to text
 */
public class VoiceRecognition extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 15;
    private boolean allowRecording = false;
    private TextView statusMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.voice_recognition, container, false);

        allowRecording = permissionCheck();
        ImageView dummy_record = rootView.findViewById(R.id.dummy_record);
        dummy_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getActivity().getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, "I want to record", duration);
                toast.show();
                permissionCheck();
            }
        });

        statusMsg = rootView.findViewById(R.id.voice_rec_stat);
        return rootView;
    }

    public boolean permissionCheck() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
           /* if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                // returns true if the user has previously denied the request, and returns
                // false if a user has denied a permission and selected the Don't ask again option
            } else */{
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return false;
            }
        } else {
            // Permission has already been granted
            if(statusMsg != null)
                statusMsg.setText(getResources().getString(R.string.voice_rec_status_on));

            return true;
        }
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
                    if(statusMsg != null)
                        statusMsg.setText(getResources().getString(R.string.voice_rec_status_on));

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    allowRecording = false;
                    if(statusMsg != null)
                        statusMsg.setText(getResources().getString(R.string.voice_rec_status_off));
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
