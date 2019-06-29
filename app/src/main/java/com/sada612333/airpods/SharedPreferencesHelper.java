package com.sada612333.airpods;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.Set;

import static com.sada612333.airpods.Constants.DB;


public class SharedPreferencesHelper {



    public static void setString(Activity activity, String key,String value){
        SharedPreferences.Editor editor = activity.getSharedPreferences(DB, Activity.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setStrSet(Activity activity, String key,Set<String> set){
        SharedPreferences.Editor editor = activity.getSharedPreferences(DB, Activity.MODE_PRIVATE).edit();
        editor.putStringSet(key, set);
        editor.commit();
    }

    public static String getString(Activity activity, String key){
        return activity.getSharedPreferences(DB, Activity.MODE_PRIVATE).getString(key,null);
    }

    public static Set<String> getStrSet(Activity activity, String key){
        return activity.getSharedPreferences(DB, Activity.MODE_PRIVATE).getStringSet(key,null);
    }

}
