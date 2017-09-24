package com.svb.toiletwall.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.svb.toiletwall.R;
import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.fragment.ProgramAnimationFragment;
import com.svb.toiletwall.fragment.ProgramDrawFragment;
import com.svb.toiletwall.fragment.ProgramRandomFragment;
import com.svb.toiletwall.fragment.ProgramTestFragment;
import com.svb.toiletwall.fragment.SettingsFragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import static com.svb.toiletwall.support.MySupport.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private final String TAG = MainActivity.class.getName();

    public static final int PAGE_CONNECTION = 0;
    public static final int PAGE_TEST = 1;
    public static final int PAGE_RANDOM = 2;
    public static final int PAGE_DRAW = 3;
    public static final int PAGE_ANIMATION = 4;
    public static final int PAGE_SETTINGS = 5;

    // GUI Components
    private View connectingLl, fragmentContainer;
    private Button mListPairedDevicesBtn, mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;

    // bluetooth
    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    // bluetooth static
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    public static final int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    public static final int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupView();
        setupViewDrawer();
        setupContent();
        setTitleStatus("Device not connected");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(blReceiver);
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        super.onDestroy();
    }

    public ConnectedThread getConnectedThread() {
        return mConnectedThread;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_connection) {
            setFragmentAsMain(PAGE_CONNECTION, null);

        } else if (id == R.id.nav_test) {
            setFragmentAsMain(PAGE_TEST, null);

        } else if (id == R.id.nav_random) {
            setFragmentAsMain(PAGE_RANDOM, null);

        } else if (id == R.id.nav_draw) {
            setFragmentAsMain(PAGE_DRAW, null);

        } else if (id == R.id.nav_animation) {
            setFragmentAsMain(PAGE_ANIMATION, null);

        } else if (id == R.id.nav_settings) {
            setFragmentAsMain(PAGE_SETTINGS, null);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                setTitleStatus("Bluetooth Enabled");
            } else
                setTitleStatus("Bluetooth Disabled");
        }
    }

    private void setTitleStatus(String msg) {
        getSupportActionBar().setTitle(msg);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.PairedBtn:
                listPairedDevices(view);
                break;

            case R.id.discover:
                discover(view);
                break;
        }
    }

    private void toggleViews(boolean connectionViewVisible, boolean fragmentContainerVisible) {
        connectingLl.setVisibility(connectionViewVisible ? View.VISIBLE : View.GONE);
        fragmentContainer.setVisibility(fragmentContainerVisible ? View.VISIBLE : View.GONE);
    }


    private void setupContent() {
        // bt message handler
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        setTitleStatus("Connected to Device: " + (String) (msg.obj));
                    else
                        setTitleStatus("Connection Failed");
                }
            }
        };

        // setup bt adapter
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            setTitleStatus("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        }

        // set init page
        setFragmentAsMain(PAGE_CONNECTION, null);
    }

    private void setupView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // connecting
        connectingLl = findViewById(R.id.connecting_ll);
        mDiscoverBtn = (Button) findViewById(R.id.discover);
        mDiscoverBtn.setOnClickListener(this);
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);
        mListPairedDevicesBtn.setOnClickListener(this);
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        fragmentContainer = findViewById(R.id.container);

    }

    private void setupViewDrawer(){

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int menuIconSize = 22;

        Menu mMenu = navigationView.getMenu();

        MenuItem mainMenu = mMenu.findItem(R.id.nav_connection);
        mainMenu.setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_bluetooth)
                .sizeDp(menuIconSize));
        mainMenu = mMenu.findItem(R.id.nav_test);
        mainMenu.setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_code)
                .sizeDp(menuIconSize));
        mainMenu = mMenu.findItem(R.id.nav_random);
        mainMenu.setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_code)
                .sizeDp(menuIconSize));
        mainMenu = mMenu.findItem(R.id.nav_draw);
        mainMenu.setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_code)
                .sizeDp(menuIconSize));
        mainMenu = mMenu.findItem(R.id.nav_animation);
        mainMenu.setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_code)
                .sizeDp(menuIconSize));
        mainMenu = mMenu.findItem(R.id.nav_settings);
        mainMenu.setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_cogs)
                .sizeDp(menuIconSize));
    }

    public void setFragmentAsMain(int position, Bundle args) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction frTransaction = fragmentManager.beginTransaction();

        switch (position) {
            case PAGE_CONNECTION:
                toggleViews(true, false);
                break;

            case PAGE_TEST:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                            ProgramTestFragment.newInstance(args), ProgramTestFragment.TAG);

                break;

            case PAGE_RANDOM:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        ProgramRandomFragment.newInstance(args), ProgramRandomFragment.TAG);
                break;

            case PAGE_DRAW:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        ProgramDrawFragment.newInstance(args), ProgramDrawFragment.TAG);
                break;

            case PAGE_ANIMATION:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        ProgramAnimationFragment.newInstance(args), ProgramAnimationFragment.TAG);
                break;

            case PAGE_SETTINGS:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        SettingsFragment.newInstance(args), SettingsFragment.TAG);
                break;
        }

        frTransaction.commit();
    }

    private void discover(View view) {
        // Check if the device is already discovering
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void listPairedDevices(View view) {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            mBTArrayAdapter.clear();
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Enable Bluetooth please", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if (!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Enable Bluetooth please", Toast.LENGTH_SHORT).show();
                return;
            }

            setTitleStatus("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread() {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

}
