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
package tuwien.babelfish.bluetooth.dialog;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tuwien.babelfish.R;

/**
 * Custom adapter to provide views for the ListView.
 * Displays Bluetoothdevice name and address
 */
public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater layoutInflater;
    private ArrayList<BluetoothDevice> devices;

    static class ViewHolder {
        public TextView deviceName;
        public TextView deviceAddress;
    }

    public DeviceListAdapter(Context context, ArrayList<BluetoothDevice> devices){
        super(context, R.layout.device_adapter_view,devices);
        this.devices = devices;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // reuse view
        if(rowView == null){
            rowView = layoutInflater.inflate(R.layout.device_adapter_view,null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.deviceName = rowView.findViewById(R.id.tv_device_name);
            viewHolder.deviceAddress= rowView.findViewById(R.id.tv_device_address);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        //BluetoothDevice device = devices.get(position);
        BluetoothDevice device = devices.get(position);

        if (device != null) {
            String name = device.getName();
            if(name == null || name.isEmpty())
                name = getContext().getResources().getString(R.string.no_name);

            holder.deviceName.setText(name);
            holder.deviceAddress.setText(device.getAddress());
        }

        return rowView;
    }

}
