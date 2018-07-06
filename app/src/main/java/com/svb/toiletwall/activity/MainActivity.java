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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.LayoutInflaterCompat;
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
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.svb.toiletwall.R;
import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.bluetooth.ConnectionThreadPool;
import com.svb.toiletwall.fragment.ProgramAnimationDetailFragment;
import com.svb.toiletwall.fragment.ProgramAnimationFragment;
import com.svb.toiletwall.fragment.ProgramDrawFragment;
import com.svb.toiletwall.fragment.ProgramRandomFragment;
import com.svb.toiletwall.fragment.ProgramSoundFragment;
import com.svb.toiletwall.fragment.ProgramTestFragment;
import com.svb.toiletwall.fragment.ProgramTextFragment;
import com.svb.toiletwall.fragment.SettingsFragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.svb.toiletwall.utils.MySupport.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private final String TAG = MainActivity.class.getName();

    public static final int PAGE_CONNECTION = 0;
    public static final int PAGE_TEST = 10;
    public static final int PAGE_RANDOM = 20;
    public static final int PAGE_DRAW = 30;
    public static final int PAGE_SOUND = 40;
    public static final int PAGE_ANIMATION = 50;
    public static final int PAGE_ANIMATION_DETAIL = 51;
    public static final int PAGE_TEXT = 60;
    public static final int PAGE_SETTINGS = 70;

    // GUI Components
    private View connectingLl, fragmentContainer;
    private Button mListPairedDevicesBtn, mDiscoverBtn;
    private Button bt1, bt2, bt3, bt4;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    int lastPage = PAGE_CONNECTION;

    // bluetooth
    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectionThreadPool mConnectionThreadPool; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    // bluetooth static
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    public static final int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    public static final int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    // toilet wall
    private static final String BT_TOILET_WALL_ADDRESS_1 = "98:D3:21:F4:7D:D3";
    private static final String BT_TOILET_WALL_NAME_1 = "HC-06";

    private static final String BT_TOILET_WALL_ADDRESS_2 = "98:D3:21:F4:7C:A4";
    private static final String BT_TOILET_WALL_NAME_2 = "HC-06";

    private static final String BT_TOILET_WALL_ADDRESS_3 = "98:D3:61:F9:3A:C4";
    private static final String BT_TOILET_WALL_NAME_3 = "HC-06";

    private static final String BT_TOILET_WALL_ADDRESS_4 = "00:21:13:01:F5:79";
    private static final String BT_TOILET_WALL_NAME_4 = "HC-05";

    private static final String[] BT_TW_ADDR = {BT_TOILET_WALL_ADDRESS_1, BT_TOILET_WALL_ADDRESS_2,
            BT_TOILET_WALL_ADDRESS_3, BT_TOILET_WALL_ADDRESS_4};
    private static final String[] BT_TW_NAME = {BT_TOILET_WALL_NAME_1, BT_TOILET_WALL_NAME_2,
            BT_TOILET_WALL_NAME_3, BT_TOILET_WALL_NAME_4};

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
        // setup fonts
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupView();
        setTitleStatus("Device not connected");
        setupViewDrawer();
        setupContent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(blReceiver);

        if (mConnectionThreadPool != null) {
            mConnectionThreadPool.cancel();
        }
        super.onDestroy();
    }

    public ConnectionThreadPool getConnectedThreadPool() {
        return mConnectionThreadPool;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else if (lastPage == PAGE_ANIMATION_DETAIL) {
            setFragmentAsMain(PAGE_ANIMATION, null);

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

        } else if (id == R.id.nav_sound) {
            setFragmentAsMain(PAGE_SOUND, null);

        } else if (id == R.id.nav_animation) {
            setFragmentAsMain(PAGE_ANIMATION, null);

        } else if (id == R.id.nav_text) {
            setFragmentAsMain(PAGE_TEXT, null);

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
        Log.d(TAG, "BT message: " + msg);
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

            case R.id.connectBt1:
                connectToDevice(BT_TW_ADDR[0], BT_TW_NAME[0], BTMODULEUUID, 0);
                break;
            case R.id.connectBt2:
                connectToDevice(BT_TW_ADDR[1], BT_TW_NAME[1], BTMODULEUUID, 1);
                break;
            case R.id.connectBt3:
                connectToDevice(BT_TW_ADDR[2], BT_TW_NAME[2], BTMODULEUUID, 2);
                break;
            case R.id.connectBt4:
                connectToDevice(BT_TW_ADDR[3], BT_TW_NAME[3], BTMODULEUUID, 3);
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
        bt1 = (Button) findViewById(R.id.connectBt1);
        findViewById(R.id.connectBt1).setOnClickListener(this);
        bt2 = (Button) findViewById(R.id.connectBt2);
        findViewById(R.id.connectBt2).setOnClickListener(this);
        bt3 = (Button) findViewById(R.id.connectBt3);
        findViewById(R.id.connectBt3).setOnClickListener(this);
        bt4 = (Button) findViewById(R.id.connectBt4);
        findViewById(R.id.connectBt4).setOnClickListener(this);

        connectingLl = findViewById(R.id.connecting_ll);
        mDiscoverBtn = (Button) findViewById(R.id.discover);
        mDiscoverBtn.setOnClickListener(this);
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);
        mListPairedDevicesBtn.setOnClickListener(this);
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                final String address = info.substring(info.length() - 17);
                final String name = info.substring(0, info.length() - 17);

                connectToDevice(address, name, BTMODULEUUID, -1);
            }
        });

        fragmentContainer = findViewById(R.id.container);

    }

    private void setupViewDrawer() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int menuIconSize = 22;

        Menu mMenu = navigationView.getMenu();

        MenuItem mainMenu = mMenu.findItem(R.id.nav_connection);
        mainMenu.setIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_bluetooth)
                .sizeDp(menuIconSize));
        mainMenu = mMenu.findItem(R.id.nav_test);
        mainMenu.setVisible(false);
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
        mainMenu = mMenu.findItem(R.id.nav_text);
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

            case PAGE_SOUND:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        ProgramSoundFragment.newInstance(args), ProgramSoundFragment.TAG);
                break;

            case PAGE_ANIMATION:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        ProgramAnimationDetailFragment.newInstance(args), ProgramAnimationDetailFragment.TAG);
                break;

            case PAGE_ANIMATION_DETAIL:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        ProgramAnimationFragment.newInstance(args), ProgramAnimationFragment.TAG);
                break;

            case PAGE_TEXT:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        ProgramTextFragment.newInstance(args), ProgramTextFragment.TAG);
                break;

            case PAGE_SETTINGS:
                toggleViews(false, true);
                frTransaction.replace(R.id.container,
                        SettingsFragment.newInstance(args), SettingsFragment.TAG);
                break;
        }
        lastPage = position;

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

    private void connectToDevice(final String address, final String name, final UUID btModuleUuid, final int idx) {
        Log.i(TAG, "connectToDevice: address:" + address);
        Log.i(TAG, "connectToDevice: name:" + name);

        if (!mBTAdapter.isEnabled()) {
            Toast.makeText(getBaseContext(), "Enable Bluetooth please", Toast.LENGTH_SHORT).show();
            return;
        }

        setTitleStatus("Connecting...");
        // Get the device MAC address, which is the last 17 chars in the View


        // Spawn a new thread to avoid blocking the GUI one
        new Thread() {
            public void run() {
                boolean success = true;

                BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                Log.d(TAG, "UUID:" + device.getUuids()[0].getUuid());

                try {
                    mBTSocket = createBluetoothSocket(device, btModuleUuid);
                } catch (IOException e) {
                    success = false;
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        success = false;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }

                if (success) {
                    ConnectedThread mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                    mConnectedThread.start();
                    if (mConnectionThreadPool == null) {
                        mConnectionThreadPool = new ConnectionThreadPool();
                    }
                    mConnectionThreadPool.addConnectionThread(mConnectedThread);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (idx) {
                                case 0:
                                    bt1.setEnabled(false);
                                    break;

                                case 1:
                                    bt2.setEnabled(false);
                                    break;

                                case 2:
                                    bt3.setEnabled(false);
                                    break;

                                case 3:
                                    bt4.setEnabled(false);
                                    break;
                            }
                        }
                    });

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            .sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, UUID btModuleUuid) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, btModuleUuid);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(btModuleUuid);
    }

}
