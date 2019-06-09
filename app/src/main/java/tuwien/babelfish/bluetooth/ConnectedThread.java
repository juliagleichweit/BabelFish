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

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
 receiving incoming data through input/output streams respectively.
 **/
class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";

    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public ConnectedThread(BluetoothSocket socket) {
        Log.d(TAG, "ConnectedThread: Starting.");
        this.socket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = this.socket.getInputStream();
            tmpOut = this.socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        inputStream = tmpIn;
        outputStream = tmpOut;
        BluetoothConnectionService.getInstance(null).addConnection(this);
    }

    public void run(){
        byte[] buffer = new byte[1024];  // buffer store for the stream

        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            // Read from the InputStream
            try {
                bytes = inputStream.read(buffer);
                String incomingMessage = new String(buffer, 0, bytes);
                BluetoothConnectionService.getInstance(null).callback.readInput(incomingMessage);
                Log.d(TAG, "InputStream: " + incomingMessage);
            } catch (IOException e) {
                Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                BluetoothConnectionService.getInstance(null).removeConnection(this);
                break;
            }
        }
    }

    /**
     * Send data to other device.
     * @param bytes data you want to send over the connection
     */
    public void write(byte[] bytes) {
        String text = new String(bytes, Charset.defaultCharset());
        Log.d(TAG, "write: Writing to outputstream: " + text);
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            BluetoothConnectionService.getInstance(null).removeConnection(this);
        }
    }

    /**
     *  Shutdown connection
     */
    public void close() {
        try {
            if(inputStream != null)
                inputStream.close();
            if(outputStream != null)
                outputStream.close();

            socket.close();
        } catch (IOException e) { }
    }
}
