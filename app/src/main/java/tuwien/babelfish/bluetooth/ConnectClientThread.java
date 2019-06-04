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
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
class ConnectClientThread extends Thread {
    private final BluetoothDevice device;
    private final UUID deviceUUID;
    private BluetoothSocket socket;
    private static final String TAG = "ConnectClientThread";

    private BluetoothAdapter bluetoothAdapter;

    private ConnectedThread connectedThread;

    public ConnectClientThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "ConnectClientThread: started.");
        this.bluetoothAdapter = bluetoothAdapter;
        this.device = device;
        deviceUUID = uuid;
    }

    public void run(){
        BluetoothSocket tmp = null;
        Log.i(TAG, "RUN mConnectThread ");

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            Log.d(TAG, "ConnectClientThread: Trying to create InsecureRfcommSocket using UUID: "
                    + BluetoothConnectionService.MY_UUID);
            tmp = device.createRfcommSocketToServiceRecord(deviceUUID);
        } catch (IOException e) {
            Log.e(TAG, "ConnectClientThread: Could not create InsecureRfcommSocket " + e.getMessage());
        }

        socket = tmp;

        // Always cancel discovery because it will slow down a connection
        bluetoothAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket

        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            socket.connect();

            Log.d(TAG, "run: ConnectClientThread connected.");
        } catch (IOException e) {
            // Close the socket
            try {
                socket.close();
                Log.d(TAG, "run: Closed Socket.");
            } catch (IOException e1) {
                Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
            }
            Log.d(TAG, "run: ConnectClientThread: Could not connect to UUID: " + BluetoothConnectionService.MY_UUID);
        }

        //will talk about this in the 3rd video
        connected(socket);
    }


    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();
    }

    public void cancel() {
        try {
            Log.d(TAG, "cancel: Closing Client Socket.");
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: close() of socket in Connectthread failed. " + e.getMessage());
        }
    }
}
