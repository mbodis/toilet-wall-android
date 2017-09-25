package com.svb.toiletwall.application;

import android.app.Application;

import com.svb.toiletwall.model.db.DaoMaster;
import com.svb.toiletwall.model.db.DaoSession;

import org.greenrobot.greendao.database.Database;

public class App extends Application {

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        // initial database
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "dbook-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
