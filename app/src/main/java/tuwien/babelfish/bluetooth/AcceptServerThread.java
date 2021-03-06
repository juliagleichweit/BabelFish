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
 **/
package tuwien.babelfish.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import tuwien.babelfish.R;

/**
 * This thread runs while listening for incoming connections. It behaves
 * like a server-side client. It runs until a connection is accepted
 * (or until cancelled).
 */
class AcceptServerThread extends Thread {
    private static final String TAG = "AcceptServerThread";
    private static final String appName = "tuwien.Babelfish";
    private static boolean canceled = false;

    // The local server socket
    private final BluetoothServerSocket serverSocket;

    public AcceptServerThread(BluetoothAdapter bluetoothAdapter ){
        BluetoothServerSocket tmp = null;


        // Create a new listening server socket
        try{
            tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, BluetoothConnectionService.MY_UUID);
            canceled = false;
        }catch (IOException e){
            Log.e(TAG, "AcceptServerThread: IOException: " + e.getMessage() );
        }
        serverSocket = tmp;
    }

    @Override
    public void run(){
        Log.d(TAG, "run: AcceptServerThread Running.");

        BluetoothSocket socket = null;
        while(!canceled) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception

                socket = serverSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            } catch (IOException e) {
                if (!canceled) {
                    Log.e(TAG, "AcceptServerThread: IOException: " + e.getMessage());
                    BluetoothConnectionService.getInstance(null).showConnectionError(R.string.bt_error_connect);
                }
            }

            // other device connected
            if (socket != null) {
                connected(socket);
            }
        }
        Log.i(TAG, "END AcceptThread ");
    }

    /**
     * Start a new ConnectedThread to manage the connection and perform transmissions
     * @param mmSocket to read from and write to
     */
    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        ConnectedThread connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();
    }

    /**
     * Stop thread (closes socket)
     */
    public void cancel() {
        Log.d(TAG, "cancel: Canceling AcceptServerThread.");
        try {
            canceled = true;
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: Close of AcceptServerThread ServerSocket failed. " + e.getMessage() );
        }
    }
}
