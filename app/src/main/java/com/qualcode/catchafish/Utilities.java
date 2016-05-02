package com.qualcode.catchafish;

import android.content.Context;

import java.util.HashMap;

public class Utilities {

    public static String GetInterest(Context ctx, final int id)
    {
        return ctx.getResources().getStringArray(R.array.pref_list_interests_titles)[id];
    }

    public static String GetRace(Context ctx, final int id)
    {
        return ctx.getResources().getStringArray(R.array.pref_list_race_titles)[id];
    }


    public static HashMap<String, String> convert(String str) {
        String[] tokens = str.split("&");
        HashMap<String, String> map = new HashMap<String, String>();

        final int length = tokens.length;

        for (int i = 0;i < length; i++)
        {
            String[] strings = tokens[i].split("=");

            if(strings.length == 2)
                map.put(strings[0], strings[1].replaceAll("%2C", ","));
        }

        return map;
    }
}
