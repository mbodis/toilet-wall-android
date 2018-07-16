package com.svb.toiletwall.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.svb.toiletwall.R;
import com.svb.toiletwall.bluetooth.ConnectionThreadPool;
import com.svb.toiletwall.fragment.ConnectionFragment;
import com.svb.toiletwall.fragment.ProgramAccGameFragment;
import com.svb.toiletwall.fragment.ProgramAnimationDetailFragment;
import com.svb.toiletwall.fragment.ProgramAnimationFragment;
import com.svb.toiletwall.fragment.ProgramDrawFragment;
import com.svb.toiletwall.fragment.ProgramRandomFragment;
import com.svb.toiletwall.fragment.ProgramSoundFragment;
import com.svb.toiletwall.fragment.ProgramTextFragment;
import com.svb.toiletwall.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = MainActivity.class.getName();

    // menu
    public static final int PAGE_CONNECTION = 0;
    public static final int PAGE_RANDOM = 10;
    public static final int PAGE_DRAW = 20;
    public static final int PAGE_ACC_GAME = 30;
    public static final int PAGE_SOUND = 40;
    public static final int PAGE_ANIMATION = 50;
    public static final int PAGE_ANIMATION_DETAIL = 51;
    public static final int PAGE_TEXT = 60;
    public static final int PAGE_SETTINGS = 70;
    int lastPage = PAGE_CONNECTION;

    // Bt connection logic
    private ConnectionThreadPool mConnectionThreadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setup fonts
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupView();
        setupViewDrawer();
        setupContent();

        mConnectionThreadPool = new ConnectionThreadPool(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
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

        } else if (id == R.id.nav_random) {
            setFragmentAsMain(PAGE_RANDOM, null);

        } else if (id == R.id.nav_draw) {
            setFragmentAsMain(PAGE_DRAW, null);

        } else if (id == R.id.nav_acc_game) {
            setFragmentAsMain(PAGE_ACC_GAME, null);

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

    private void setupContent() {
        // set init page
        setFragmentAsMain(PAGE_ACC_GAME, null);
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
    }

    private void setupViewDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int[] ids = {
                R.id.nav_connection,
                R.id.nav_random,
                R.id.nav_draw,
                R.id.nav_acc_game,
                R.id.nav_sound,
                R.id.nav_animation,
                R.id.nav_text,
                R.id.nav_settings,
        };
        FontAwesome.Icon icons[] = {
                FontAwesome.Icon.faw_bluetooth,
                FontAwesome.Icon.faw_code,
                FontAwesome.Icon.faw_code,
                FontAwesome.Icon.faw_code,
                FontAwesome.Icon.faw_code,
                FontAwesome.Icon.faw_code,
                FontAwesome.Icon.faw_code,
                FontAwesome.Icon.faw_cogs};

        for (int i = 0; i < ids.length; i++) {
            MenuItem mainMenu = navigationView.getMenu().findItem(ids[i]);
            mainMenu.setIcon(new IconicsDrawable(this)
                    .icon(icons[i])
                    .sizeDp(22));
        }
    }

    public void setFragmentAsMain(int position, Bundle args) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction frTransaction = fragmentManager.beginTransaction();

        switch (position) {
            case PAGE_CONNECTION:
                frTransaction.replace(R.id.container,
                        ConnectionFragment.newInstance(args), ConnectionFragment.TAG);
                break;

            case PAGE_RANDOM:
                frTransaction.replace(R.id.container,
                        ProgramRandomFragment.newInstance(args), ProgramRandomFragment.TAG);
                break;

            case PAGE_DRAW:
                frTransaction.replace(R.id.container,
                        ProgramDrawFragment.newInstance(args), ProgramDrawFragment.TAG);
                break;

            case PAGE_ACC_GAME:
                frTransaction.replace(R.id.container,
                        ProgramAccGameFragment.newInstance(args), ProgramAccGameFragment.TAG);
                break;

            case PAGE_SOUND:
                frTransaction.replace(R.id.container,
                        ProgramSoundFragment.newInstance(args), ProgramSoundFragment.TAG);
                break;

            case PAGE_ANIMATION:
                frTransaction.replace(R.id.container,
                        ProgramAnimationDetailFragment.newInstance(args), ProgramAnimationDetailFragment.TAG);
                break;

            case PAGE_ANIMATION_DETAIL:
                frTransaction.replace(R.id.container,
                        ProgramAnimationFragment.newInstance(args), ProgramAnimationFragment.TAG);
                break;

            case PAGE_TEXT:
                frTransaction.replace(R.id.container,
                        ProgramTextFragment.newInstance(args), ProgramTextFragment.TAG);
                break;

            case PAGE_SETTINGS:
                frTransaction.replace(R.id.container,
                        SettingsFragment.newInstance(args), SettingsFragment.TAG);
                break;
        }
        lastPage = position;
        frTransaction.commit();
    }
}
