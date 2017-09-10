package com.svb.toiletwall.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.R;
import com.svb.toiletwall.model.ToiletDisplay;
import com.svb.toiletwall.programs.DrawProgram;
import com.svb.toiletwall.programs.ProgramIface;
import com.svb.toiletwall.programs.RandomProgram;
import com.svb.toiletwall.programs.TestingProgram;
import com.svb.toiletwall.support.MySupport;
import com.svb.toiletwall.view.ToiletView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.svb.toiletwall.support.MySupport.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final int BLOCK_COLS = 3; // TODO move to settings
    public static final int BLOCK_ROWS = 2; // TODO move to settings

    public static final String PROGRAM_TEST = "test";
    public static final String PROGRAM_RANDOM = "random";
    public static final String PROGRAM_DRAW = "draw";
    public static final String PROGRAM_DRAW_ANIMATION = "animation";

    // GUI Components
    private View connectingLl;
    private ToiletView drawView;
    private Button mListPairedDevicesBtn, mDiscoverBtn, startProgramBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private Spinner programSpinner;

    // GUI animation
    private View animationPanel1, animationPanel2;

    // GUI draw
    private View drawPanel;


    private final String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    ProgramIface program;

    // #defines for identifying shared types between calling functions

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
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        stopProgram();
        super.onDestroy();
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {

        } else if (id == R.id.nav_manage) {

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
                setTitleStatus("Enabled");
            } else
                setTitleStatus("Disabled");
        }
    }

    private void setTitleStatus(String msg){
        getSupportActionBar().setTitle(msg);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startProgram:
                stopProgram();
                String selectedProgram = (String) programSpinner.getAdapter().getItem(programSpinner.getSelectedItemPosition());

                if (selectedProgram.equals(PROGRAM_TEST)) {
                    program = new TestingProgram(BLOCK_COLS, BLOCK_ROWS, mConnectedThread);

                } else if (selectedProgram.equals(PROGRAM_RANDOM)) {
                    program = new RandomProgram(BLOCK_COLS, BLOCK_ROWS, mConnectedThread);

                } else if (selectedProgram.equals(PROGRAM_DRAW)) {
                    program = new DrawProgram(BLOCK_COLS, BLOCK_ROWS, mConnectedThread);
                    drawView.setToiletDisplay(program.getToiletDisplay());
                    drawView.startDrawImage();
                    connectionView(false);
                    drawView.setVisibility(View.VISIBLE);
                    drawPanel.setVisibility(View.VISIBLE);

                } else if (selectedProgram.equals(PROGRAM_DRAW_ANIMATION)) {
                    program = new DrawProgram(BLOCK_COLS, BLOCK_ROWS, mConnectedThread);
                    drawView.setToiletDisplay(program.getToiletDisplay());
                    drawView.startDrawImage();
                    drawView.setVisibility(View.VISIBLE);
                    connectionView(false);
                    drawPanel.setVisibility(View.VISIBLE);
                    animationPanel1.setVisibility(View.VISIBLE);
                    animationPanel1.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.PairedBtn:
                listPairedDevices(view);
                break;

            case R.id.discover:
                discover(view);
                break;

            case R.id.drawClear:
                drawView.getToiletDisplay().clearScreen();
                break;

            case R.id.play:
            case R.id.prev:
            case R.id.next:
            case R.id.animationClear:
            case R.id.newframe:
                Toast.makeText(getApplicationContext(), "TODO", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void connectionView(boolean visible) {
        connectingLl.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void stopProgram() {
        if (program != null) {
            program.onDestroy();
            program = null;
        }
    }

    private void setupContent() {
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

        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            setTitleStatus("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(), "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
        }
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
        startProgramBtn = (Button) findViewById(R.id.startProgram);
        startProgramBtn.setOnClickListener(this);
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // animation
        animationPanel1 = findViewById(R.id.animationPanel1);
        animationPanel1.setVisibility(View.GONE);
        animationPanel2 = findViewById(R.id.animationPanel2);
        animationPanel2.setVisibility(View.GONE);
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.prev).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.newframe).setOnClickListener(this);
        findViewById(R.id.animationClear).setOnClickListener(this);



        // draw
        drawPanel = findViewById(R.id.drawPanel);
        drawPanel.setVisibility(View.GONE);
        findViewById(R.id.drawClear).setOnClickListener(this);

        setupViewAdapter();
        setupDrawView();
    }

    private void setupViewAdapter() {
        programSpinner = (Spinner) findViewById(R.id.select_program);

        List<String> list = new ArrayList<String>();
        list.add(PROGRAM_DRAW_ANIMATION);
        list.add(PROGRAM_DRAW);
        list.add(PROGRAM_RANDOM);
        list.add(PROGRAM_TEST);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        programSpinner.setAdapter(spinnerArrayAdapter);
    }

    private void setupDrawView(){
        drawView = (ToiletView) findViewById(R.id.drawView);
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
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if (!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
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
