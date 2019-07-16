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
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tuwien.babelfish.bluetooth.dialog.ConnectDialogFragment;


/**
 * Service managing the connection and maintenance of a Bluetooth connections.
 * ConnectDialogFragment is used to guide the user through the connection process.
 *
 */
public class BluetoothConnectionService  {
    private static final String TAG = "BluetoothConnectionServ";
    public static final UUID MY_UUID =
            UUID.fromString("df7006b6-af10-4ada-8a9c-4723c2a0511d");

    private static BluetoothConnectionService instance;
    private FragmentManager fm;
    private final BluetoothAdapter bluetoothAdapter;
    private Context context;
    private AppCompatActivity activity;
    private AcceptServerThread serverThread;
    private ConnectClientThread clientThread;

    private List<ConnectedThread> connectedThreads;
    private ArrayList<BluetoothDevice> devices;
    private ConnectDialogFragment dialogFragment;

    OnInputListener callback;

    /**
     * Set Callback to propagate connection events
     * @param listener must not be null
     */
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
         * @param input String from other device
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
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        // start();
                        showDialog();
                        return;
                    default: return;
                }
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver btBondedStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //case1 bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
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


    /**
     * Creates instance with the passed activity , further calls do not change the activity
     * @param activity AppCompatActivity instance; if no activity present, pass null
     * @return Instance with the first passed activity
     */
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
            this.context = activity.getApplicationContext();
            this.activity = activity;
        }

        this.devices = new ArrayList<>();

        // register receiver to be informed when bluetooth is on
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(btStatusReceiver, BTIntent);

        // register receiver to be informed of devices found
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(btFindReceiver, discoverDevicesIntent);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(btBondedStateReceiver, filter);
    }

    /**
     * Set FragmentManager from Activity.
     * Fragment.getActivity().getFragmentManager() returns manager with illegal states
     */
    public void setFragmentManager(FragmentManager fm){
        this.fm = fm;
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

    public synchronized void addConnection(ConnectedThread conn){
        connectedThreads.add(conn);
        callback.changeIcon(true);
        dismissDialog();
    }

    public synchronized void removeConnection(ConnectedThread conn){
        connectedThreads.remove(conn);
        if(connectedThreads.isEmpty())
            callback.changeIcon(false);
    }


    public void showDialog(){
        if(dialogFragment== null) {
            dialogFragment = new ConnectDialogFragment();
        }
            // show connection process dialog
            dialogFragment.show(fm, ConnectDialogFragment.TAG);

            start();
    }

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        clientThread = new ConnectClientThread(bluetoothAdapter, device, uuid);
        clientThread.start();
    }

    /**
     * Close all threads and disable bluetoothAdapter if necessary
     */
    public void stop() {

        devices.clear();

        if (serverThread != null)
            serverThread.cancel();

        if(clientThread != null)
            clientThread.cancel();

        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        stopClient();
    }

    /**
     * Close client connection over Bluetooth
     */
    public void stopClient(){
        // currently only one connection
        for(ConnectedThread c : connectedThreads)
            c.close();
        connectedThreads.clear();
    }

    /**
     * Check if currently a connection is running
     * @return true if a client is successfully connected, false otherwise
     */
    public boolean isConnected(){
        return !connectedThreads.isEmpty();
    }

    /**
     * Stop all threads and unregister Bluetooth receivers
     */
    public void shutdown() {
        stop();
        // unregister all broadcast receivers to free up resources
        context.unregisterReceiver(btStatusReceiver);
        context.unregisterReceiver(btFindReceiver);
        context.unregisterReceiver(btBondedStateReceiver);

        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    /**
     * Closes the dialog and cancels running Bluetooth discovery.
     */
    private void dismissDialog() {
        if(dialogFragment != null){
            dialogFragment.dismiss();
            if(bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
        }
    }


    /**
     * Write to the ConnectedThread in an un-synchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {

        Log.d(TAG, "write: Write Called.");
        //perform the write
        for(ConnectedThread c : connectedThreads)
            c.write(out);
    }

    /**
     * Toast message to inform client of connection problems
     */
    public void showConnectionError(int stringId){
        activity.runOnUiThread(() ->{
                Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
                callback.changeIcon(false);
                dismissDialog();
        });
    }
}
