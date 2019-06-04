package tuwien.babelfish.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tuwien.babelfish.R;


public class DeviceListAdapter extends ArrayAdapter<String> {

    private LayoutInflater layoutInflater;
    private ArrayList<String> devices;

    static class ViewHolder {
        public TextView deviceName;
        public TextView deviceAddress;
    }

    public DeviceListAdapter(Context context, ArrayList<String> devices){
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
        String device = devices.get(position);

        if (device != null) {
            holder.deviceName.setText(device);
            holder.deviceAddress.setText(device + " address");
        }

        return rowView;
    }

}
