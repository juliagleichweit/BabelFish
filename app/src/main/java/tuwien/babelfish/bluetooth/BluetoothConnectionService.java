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


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tuwien.babelfish.R;


/**
 *
 */
public class BluetoothConnectionService  {
    private static final String TAG = "BluetoothConnectionServ";
    public static final UUID MY_UUID =
            UUID.fromString("df7006b6-af10-4ada-8a9c-4723c2a0511d");

    private static BluetoothConnectionService instance;

    private final BluetoothAdapter bluetoothAdapter;
    Context context;
    AppCompatActivity activity;
    private AcceptServerThread serverThread;
    private ConnectClientThread clientThread;

    private List<ConnectedThread> connectedThreads;
    private BluetoothDevice device;

    private ConnectDialogFragment dialogFragment;

    OnInputListener callback;

    public void setConnectionListener(OnInputListener listener){
        callback = listener;
    }


    /**
     * Container Fragment must implement this interface:
     * used to pass messages from different devices over BT
     *  visualize if connection is established
    */
    public interface OnInputListener{

        /**
         * Process data received over bluetooth connection
         * @param input
         */
        void readInput(String input);

        /**
         * Change the bot icon according to the client connection status
         * @param connected true if connection is running, otherwise false
         */
        void changeIcon(boolean connected);
    }

    /**
     *  Create a BroadcastReceiver for ACTION_STATE_CHANGE to be informed of bluetooth state
     */
    private final BroadcastReceiver btStatusReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);
                switch(state){

                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        // start();
                        showDialog();
                        break;

                }
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    device = mDevice;
                    dismissDialog();
                }
            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     */
    private BroadcastReceiver btFindReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
               if(dialogFragment != null)
                    dialogFragment.addDevice(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
            }
        }
    };


    public static synchronized BluetoothConnectionService getInstance(AppCompatActivity activity) {
        if (instance == null) {
            instance = new BluetoothConnectionService(activity);
        }
        return instance;
    }

    private BluetoothConnectionService(AppCompatActivity activity) {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectedThreads = new ArrayList<>();

        // show connection process dialog
        dialogFragment = new ConnectDialogFragment();

        if(activity != null){
            dialogFragment.activity = activity;
            this.context = activity.getApplicationContext();
            this.activity = activity;
        }


        // register receiver to be informed when bluetooth is on
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(btStatusReceiver, BTIntent);

        // register receiver to be informed of devices found
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(btFindReceiver, discoverDevicesIntent);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(mBroadcastReceiver4, filter);
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

    public void addConnection(ConnectedThread conn){
        connectedThreads.add(conn);
        callback.changeIcon(true);
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

        this.device = device;
        clientThread = new ConnectClientThread(bluetoothAdapter, device, uuid);
        clientThread.start();
    }

    public void stop() {

        if (serverThread != null)
            serverThread.cancel();

        if(clientThread != null)
            clientThread.cancel();

        if(bluetoothAdapter.isDiscovering())
          bluetoothAdapter.cancelDiscovery();

        stopClients();
    }

    public void stopClients(){
        for(ConnectedThread c : connectedThreads)
            c.close();
    }

    public boolean isConnected(){
        return !connectedThreads.isEmpty();}

    public void shutdown() {
        stop();
        context.unregisterReceiver(btStatusReceiver);
        context.unregisterReceiver(btFindReceiver);
        context.unregisterReceiver(mBroadcastReceiver4);

        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    public void dismissDialog() {
        dialogFragment.dismiss();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        for(ConnectedThread c : connectedThreads)
            c.write(out);
    }

    public void showConnectionError(){
        activity.runOnUiThread(() ->{
                Toast.makeText(context, R.string.bt_error_connect, Toast.LENGTH_SHORT).show();
                dismissDialog();
        });
    }
}
