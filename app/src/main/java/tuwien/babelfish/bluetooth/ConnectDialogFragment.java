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

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import tuwien.babelfish.R;

/**
 * Fragment used to guide bluetooth connection process
 */
public class ConnectDialogFragment extends DialogFragment {

    OnDeviceSelectedListener callback;

    private TextView tv_title;
    private ListView lv_devices;
    private ProgressBar progressBar;

    private ArrayList<String> devices;
    private DeviceListAdapter listAdapter;
    public AppCompatActivity activity;

    public void setOnDeviceSelectedListener(AppCompatActivity activity){
        callback = (OnDeviceSelectedListener) activity;
    }

    //Container Activity must implement this interface
    //used to pass messages from fragment to containing UI
    public interface OnDeviceSelectedListener{
        void onSelectedDevice(UUID device);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bluetooth_dialog, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        tv_title = rootView.findViewById(R.id.tv_bt_title);
        lv_devices = rootView.findViewById(R.id.lv_devices);

        setTitle(R.string.bt_title_start);

        lv_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(getActivity().getApplicationContext(), "Clicked on " + devices.get(position), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        devices = new ArrayList<>();
        devices.add("Bang Chan");
        devices.add("Hwang Hyunjin");
        devices.add("Lee Felix");
        devices.add("Lee Minho");

        listAdapter = new DeviceListAdapter(getActivity().getApplicationContext(), devices);
        lv_devices.setAdapter(listAdapter);
        return rootView;
    }

    public void setTitle(int title){
        this.tv_title.setText(getResources().getString(title));
    }

    public void addDevice(BluetoothDevice device){
        //devices.add(device);
        // notify listview of new data
        listAdapter.notifyDataSetChanged();
    }

    public void showDialog(){
        try {
            this.show(activity.getSupportFragmentManager(), "ConnectDialogFragment");
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }


}
