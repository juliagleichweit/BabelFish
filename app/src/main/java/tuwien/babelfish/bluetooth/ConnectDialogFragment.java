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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import tuwien.babelfish.R;

/**
 * Fragment used to guide bluetooth connection process
 */
public class ConnectDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    public static final String TAG = "ConnectdialogFragement";

    private TextView tv_title;
    private ArrayList<BluetoothDevice> devices;
    private DeviceListAdapter listAdapter;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bluetooth_dialog, container, false);

        ProgressBar progressBar = rootView.findViewById(R.id.progressBar);
        tv_title = rootView.findViewById(R.id.tv_bt_title);
        ListView lv_devices = rootView.findViewById(R.id.lv_devices);

        setTitle(R.string.bt_title_scanning);

        lv_devices.setOnItemClickListener(this);

        //set up bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = new ArrayList<>();
        listAdapter = new DeviceListAdapter(getActivity().getApplicationContext(), devices);
        lv_devices.setAdapter(listAdapter);

        // start discovery
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        bluetoothAdapter.startDiscovery();
        return rootView;
    }

    private void setTitle(int title){
        this.tv_title.setText(getResources().getString(title));
    }

    public void addDevice(BluetoothDevice device){
        devices.add(device);
        // notify listview of new data
        listAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        //Toast.makeText(activity.getApplicationContext(), "Clicked on " + devices.get(position).getName(), Toast.LENGTH_SHORT).show();
       if(bluetoothAdapter.isDiscovering()) {
           bluetoothAdapter.cancelDiscovery();
       }

        // start connection to remote device -> init pairing if not done?
        BluetoothConnectionService.getInstance(null).startClient(devices.get(position), BluetoothConnectionService.MY_UUID);
        setTitle(R.string.bt_title_connect);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}
