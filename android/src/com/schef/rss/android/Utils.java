package com.schef.rss.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by scheffela on 11/28/14.
 */
public class Utils {

    private static final String PAPER_PREFERENCE = "pref.paper";

    public static String getPaper(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PAPER_PREFERENCE, "http://www.evilcorgi.com/contentservice/site/vic");
    }

    public static void setPaper(Context context, String paper){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PAPER_PREFERENCE, paper);
        editor.apply();
    }
}
