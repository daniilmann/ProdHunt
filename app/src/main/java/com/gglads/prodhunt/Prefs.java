package com.gglads.prodhunt;


import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Prefs {

    private static Prefs instance = null;
    private SharedPreferences shPref = null;
    private SharedPreferences.Editor shPrefEditor = null;
    private static String SHP_KEY = "SHP_KEY";
    private Context context = null;
    private boolean inited = false;
    private static String ERROR_MSG = "Preferences must be specified!";

    enum KEYS {
        CUR_CAT,
        CATS,
        MIN_DATE,
        MAX_DATE,
        UPD_PERIOD
    }

    private String currentCat = null;
    private Set<String> addedCats = null;
    private Map<String, Boolean> notCats = null;
    private Map<String, Integer> lastPostIDs = null;
    private Date minDate = null;
    private Date maxDate = null;

    public static synchronized Prefs getInstance() {
        if (instance == null)
            instance = new Prefs();
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        if (!inited) {
            shPref = context.getSharedPreferences(SHP_KEY, context.MODE_PRIVATE);
            shPrefEditor = shPref.edit();
            currentCat = shPref.getString(KEYS.CUR_CAT.name(), "tech");
            addedCats = shPref.getStringSet(KEYS.CATS.name(), new HashSet<String>());
            lastPostIDs = new HashMap<>();
            for (String k : addedCats) {
                lastPostIDs.put(k, shPref.getInt(k + "ID", -1));
                notCats.put(k, shPref.getBoolean(k + "N", false));
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -2);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String today = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
            try {
                minDate = format.parse(shPref.getString(KEYS.MIN_DATE.name(), today));
                maxDate = format.parse(shPref.getString(KEYS.MAX_DATE.name(), today));
            } catch (ParseException e) {
                // and some debug here
            }
            inited = true;
        }
    }

    public boolean isNotificated(String category) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        return notCats.get(category);
    }

    public void setNotifcate(String category, Boolean notificate) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        shPrefEditor.putString(category + "N", notificate.toString());
        notCats.put(category, notificate);
    }

    public int getLastPostID(String category) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        return lastPostIDs.containsKey(category) ? lastPostIDs.get(category) : -1;
    }

    public void setLastPostID(String category, Integer ID) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        addCategory(category);
        shPrefEditor.putString(category + "D", Integer.toString(ID));
        lastPostIDs.put(category, ID);
    }

    public boolean isAdded(String category) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        return addedCats.contains(category);
    }

    public Set<String> getAddedCats() throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        return addedCats;
    }

    public void addCategory(String category) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        if (!addedCats.contains(category)) {
            addedCats.add(category);
            shPrefEditor.putStringSet(KEYS.CATS.name(), addedCats);
        }
    }

    public void removeCategory(String category) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        if (addedCats.contains(category)) {
            addedCats.remove(category);
            shPrefEditor.putStringSet(KEYS.CATS.name(), addedCats);
        }
    }

    public String getCurrentCat() throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        return currentCat;
    }

    public void setCurrentCat(String category) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        currentCat = category;
        shPrefEditor.putString(KEYS.CUR_CAT.name(), category);
    }

    public void setUpdatePeriod(int sec) throws IllegalAccessException {
        if (!inited)
            throw new IllegalAccessException(ERROR_MSG);

        shPrefEditor.putInt(KEYS.UPD_PERIOD.name(), sec);
    }

    public Integer getUpdatePeriod() {
        if (shPref != null)
            return shPref.getInt(KEYS.UPD_PERIOD.name(), 60);
        return 60;
    }

}
