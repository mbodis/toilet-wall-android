package com.svb.toiletwall.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by mbodis on 7/4/17.
 */

public class FontManager {
    private static final String ROOT = "fonts/",
            FONTAWESOME = ROOT + "fontawesome-webfont.ttf";

    private static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    private static void markAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child, typeface);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        } else if (v instanceof Button) {
            ((Button) v).setTypeface(typeface);

        }
    }

    public static void setupRecursivelyForView(Context ctx, View v){
        Typeface iconFont = FontManager.getTypeface(ctx, FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(v, iconFont);
    }

    public static void setupRecursivelyById(Activity act, int layoutId){
        Typeface iconFont = FontManager.getTypeface(act, FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(act.findViewById(layoutId), iconFont);
    }

    public static void setFontAwesomeMenuItem (AppCompatActivity act, Menu menu, int rIdString, int rIdIdOfElement) {
        SpannableString s = new SpannableString(act.getString(rIdString));
        s.setSpan(new TypefaceSpan("fontawesome-webfont.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        menu.findItem(rIdIdOfElement).setTitle(s);
    }
}
