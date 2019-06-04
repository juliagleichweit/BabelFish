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
package tuwien.babelfish.bluetooth;


import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.UUID;

import tuwien.babelfish.LanguageDialogFragment;
import tuwien.babelfish.R;

/**
 *
 */
public class BluetoothConnectionService  {
    private static final String TAG = "BluetoothConnectionServ";
    public static final UUID MY_UUID =
            UUID.fromString("df7006b6-af10-4ada-8a9c-4723c2a0511d");


    private final BluetoothAdapter bluetoothAdapter;
    Context context;

    private AcceptServerThread serverThread;
    private ConnectClientThread clientThread;

    private BluetoothDevice device;

    private UUID deviceUUID;
    private ConnectDialogFragment dialogFragment;


    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver btStatusReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        // start();
                        showDialog();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    public BluetoothConnectionService(AppCompatActivity activity) {
        this.context = activity.getApplicationContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // show connection process dialog
        dialogFragment = new ConnectDialogFragment();
        dialogFragment.activity = activity;
        // register receiver to be informed when bluetooth is on
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(btStatusReceiver, BTIntent);
    }


    /**
     * Start the chat service. Specifically start AcceptServerThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (clientThread != null) {
            clientThread.cancel();
            clientThread = null;
        }
        if (serverThread == null) {
            serverThread = new AcceptServerThread(bluetoothAdapter);
            serverThread.start();
        }
    }

    public void showDialog(){
        if(dialogFragment!= null) {
            dialogFragment.showDialog();
        }else{
            Log.e(TAG, "dialog is null");
        }
    }

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        //initprogress dialog
        /*progressDialog = ProgressDialog.show(context,"Connecting Bluetooth"
                ,"Please Wait...",true);
*/
        this.device = device;
        clientThread = new ConnectClientThread(bluetoothAdapter, device, uuid);
        clientThread.start();
    }

    public void stop() {
    if(dialogFragment != null )
       // dialogFragment.dismiss();

        if (serverThread != null)
            serverThread.cancel();

        if(clientThread != null)
            clientThread.cancel();
    }

    public void shutdown() {
        stop();
        context.unregisterReceiver(btStatusReceiver);
    }
}
