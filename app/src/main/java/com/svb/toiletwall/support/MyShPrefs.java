package com.svb.toiletwall.support;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by mbodis on 10/21/15.
 */
public class MyShPrefs {

    public static final int BLOCK_COLS = 3; // default value
    public static final int BLOCK_ROWS = 2; // default value

    public static final String TAG = MyShPrefs.class.getName();
    public static final String TS_PREFS = "global_ts";

    private static final String KEY_BLOCK_ROWS = "display_block_rows";
    private static final String KEY_BLOCK_COLS = "display_block_cols";


    public static int getBlockRows(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(TS_PREFS,
                Context.MODE_PRIVATE);
        return Integer.parseInt(sp.getString(KEY_BLOCK_ROWS, BLOCK_ROWS+""));
    }

    public static int getBlockCols(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(TS_PREFS,
                Context.MODE_PRIVATE);
        return Integer.parseInt(sp.getString(KEY_BLOCK_COLS, BLOCK_COLS+""));
    }

}
