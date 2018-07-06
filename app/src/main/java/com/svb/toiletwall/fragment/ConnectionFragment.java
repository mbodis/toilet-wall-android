package com.svb.toiletwall.fragment;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.svb.toiletwall.R;
import com.svb.toiletwall.activity.MainActivity;
import com.svb.toiletwall.bluetooth.ConnectionThreadPool;

import java.util.Set;

public class ConnectionFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = ConnectionFragment.class.getName();

    public static final String INTENT_ACTION_DISABLE_BTN = "action_disable_btn";

    private Button bt1, bt2, bt3, bt4;
    private BluetoothAdapter mBTAdapter;
    private ArrayAdapter<String> mBTArrayAdapter;

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }

            if (INTENT_ACTION_DISABLE_BTN.equals(action)) {
                setupButtons();
            }
        }
    };

    public static ConnectionFragment newInstance(Bundle args) {
        ConnectionFragment fragment = new ConnectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTArrayAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        }
        setupView(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        getActivity().registerReceiver(blReceiver, new IntentFilter(INTENT_ACTION_DISABLE_BTN));
    }

    @Override
    public void onDestroy() {
        try {
            getActivity().unregisterReceiver(blReceiver);
        } catch (Exception e) {
            // don't care
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.PairedBtn:
                listPairedDevices();
                break;

            case R.id.discover:
                discover();
                break;

            case R.id.connectBt1:
                ((MainActivity) getActivity()).getConnectedThreadPool()
                        .connectToDevice(ConnectionThreadPool.BT_TW_ADDR[0], ConnectionThreadPool.BT_TW_NAME[0], ConnectionThreadPool.BTMODULEUUID, 0);
                break;
            case R.id.connectBt2:
                ((MainActivity) getActivity()).getConnectedThreadPool()
                        .connectToDevice(ConnectionThreadPool.BT_TW_ADDR[1], ConnectionThreadPool.BT_TW_NAME[1], ConnectionThreadPool.BTMODULEUUID, 1);
                break;
            case R.id.connectBt3:
                ((MainActivity) getActivity()).getConnectedThreadPool()
                        .connectToDevice(ConnectionThreadPool.BT_TW_ADDR[2], ConnectionThreadPool.BT_TW_NAME[2], ConnectionThreadPool.BTMODULEUUID, 2);
                break;
            case R.id.connectBt4:
                ((MainActivity) getActivity()).getConnectedThreadPool()
                        .connectToDevice(ConnectionThreadPool.BT_TW_ADDR[3], ConnectionThreadPool.BT_TW_NAME[3], ConnectionThreadPool.BTMODULEUUID, 3);
                break;
        }
    }

    private void setupView(View mView) {
        bt1 = (Button) mView.findViewById(R.id.connectBt1);
        mView.findViewById(R.id.connectBt1).setOnClickListener(this);
        bt2 = (Button) mView.findViewById(R.id.connectBt2);
        mView.findViewById(R.id.connectBt2).setOnClickListener(this);
        bt3 = (Button) mView.findViewById(R.id.connectBt3);
        mView.findViewById(R.id.connectBt3).setOnClickListener(this);
        bt4 = (Button) mView.findViewById(R.id.connectBt4);
        mView.findViewById(R.id.connectBt4).setOnClickListener(this);

        mView.findViewById(R.id.discover).setOnClickListener(this);
        mView.findViewById(R.id.PairedBtn).setOnClickListener(this);
        mBTArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        ListView mDevicesListView = (ListView) mView.findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                final String address = info.substring(info.length() - 17);
                final String name = info.substring(0, info.length() - 17);

                ((MainActivity) getActivity()).getConnectedThreadPool()
                        .connectToDevice(address, name, ConnectionThreadPool.BTMODULEUUID, -1);
            }
        });

        setupButtons();
    }

    private void setupButtons(){
        boolean[] connected = ((MainActivity) getActivity()).getConnectedThreadPool()
                .getConnectedDevices();
        bt1.setEnabled(connected[0]);
        bt2.setEnabled(connected[1]);
        bt3.setEnabled(connected[2]);
        bt4.setEnabled(connected[3]);
    }

    private void discover() {
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getActivity(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getActivity(), "Discovery started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void listPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            mBTArrayAdapter.clear();
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getActivity(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getActivity(), "Enable Bluetooth please", Toast.LENGTH_SHORT).show();
    }

    public static void refreshButtons(Context ctx){
        Intent mIntent = new Intent(INTENT_ACTION_DISABLE_BTN);
        ctx.sendBroadcast(mIntent);
    }
}
