package es.upsa.mimo.android.diexpenses.utils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.models.User;

/**
 * Created by Diego on 29/3/16.
 */
public class Diexpenses {

    private static final String TAG = Diexpenses.class.getSimpleName();

    private static final String CURRENCY_FORMAT = "###,##0.00";
    private static final DecimalFormat currencyFormatter = new DecimalFormat(CURRENCY_FORMAT);
    private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
    private static final DecimalFormat decimalFormatWithLocale = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());

    public static void setUserInSharedPreferences(Context context, User user) {
        Log.d(TAG, user.toString());
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putBoolean(Constants.SharedPreferences.Parameters.User.IS_LOGGED, true);
        editor.putLong(Constants.SharedPreferences.Parameters.User.ID, user.getId());
        editor.putString(Constants.SharedPreferences.Parameters.User.USER, user.getUser());
        editor.putString(Constants.SharedPreferences.Parameters.User.NAME, user.getName());
        editor.putString(Constants.SharedPreferences.Parameters.User.AUTH_TOKEN, user.getAuthToken());
        editor.commit();
    }

    @Nullable
    public static User getUserFromSharedPreferences(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.NAME, Activity.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(Constants.SharedPreferences.Parameters.User.IS_LOGGED, false)) {
            return null;
        }

        Long id = sharedPreferences.getLong(Constants.SharedPreferences.Parameters.User.ID, -1);
        String name = sharedPreferences.getString(Constants.SharedPreferences.Parameters.User.NAME, null);
        String user = sharedPreferences.getString(Constants.SharedPreferences.Parameters.User.USER, null);
        String authToken = sharedPreferences.getString(Constants.SharedPreferences.Parameters.User.AUTH_TOKEN, null);

        if(id == -1 || name == null || user == null || authToken == null) {
            return null;
        }

        User userLogged = new User(id, authToken, user, name);
        return userLogged;
    }

    public static void removeUserFromSharedPreferences(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.remove(Constants.SharedPreferences.Parameters.User.IS_LOGGED);
        editor.clear();
        editor.commit();
    }

    public static void checkDialog(FragmentManager fragmentManager, String tag) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
    }

    public static String formatAmount(BigDecimal amount) {
        String methodName = "formatAmount - ";
        Log.d(TAG, methodName + "start. Amount=" + amount);
        String amountFormatted = currencyFormatter.format(amount);
        Log.d(TAG, methodName + "end. Amount formatted=" + amountFormatted);
        return amountFormatted;
    }

    public static String formatDate(Date date) {
        String methodName = "formatDate - ";
        Log.d(TAG, methodName + "start. Date=" + date.toString());

        String strDate = dateFormat.format(date);

        Log.d(TAG, methodName + "end. Date formatted=" + strDate);
        return strDate;
    }

    public static Date parseDate(String strDate) throws ParseException {
        String methodName = "parseDate - ";
        Log.d(TAG, methodName + "start. String Date=" + strDate);

        Date date = dateFormat.parse(strDate);

        Log.d(TAG, methodName + "end. Date formatted=" + date.toString());
        return date;
    }

    private static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static List<String> capitalize(List<String> lstData) {
        List<String> lstDataAux = new ArrayList<String>();
        for(int i = 0 ; i < lstData.size() ; i++) {
            lstDataAux.add(capitalize(lstData.get(i)));
        }
        return lstDataAux;
    }

    public static String getCurrency(Context context) {
        String methodName = "getCurrency - ";
        Log.d(TAG, methodName + "start.");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int currencyValue = Integer.parseInt(sharedPreferences.getString("currency", "0"));
        Log.d(TAG, methodName + "CurrencyValue=" + currencyValue);
        String[] currencies = context.getResources().getStringArray(R.array.currencies);
        String currency = currencies[currencyValue];
        Log.d(TAG, methodName + "end. Currency=" + currency);
        return currency;
    }

    public static int incrementAppUse(Context context) {
        String methodName = "incrementAppUse - ";
        Log.d(TAG, methodName + "start.");
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.NAME, Activity.MODE_PRIVATE);
        int counterOfUses = sharedPreferences.getInt(Constants.SharedPreferences.Parameters.Rate.COUNTER_OF_USES, 0);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putInt(Constants.SharedPreferences.Parameters.Rate.COUNTER_OF_USES, ++counterOfUses);
        editor.commit();
        Log.d(TAG, methodName + "end. CounterOfUses=" + counterOfUses);
        return counterOfUses;
    }

    public static boolean hasRated(Context context) {
        String methodName = "hasRated - ";
        Log.d(TAG, methodName + "start.");
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.NAME, Activity.MODE_PRIVATE);
        boolean hasRated = sharedPreferences.getBoolean(Constants.SharedPreferences.Parameters.Rate.HAS_RATE, false);
        Log.d(TAG, methodName + "end. HasRated=" + hasRated);
        return hasRated;
    }

    public static String getAppFolder(final Context context) throws Exception {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
    }

    public static BigDecimal parseAmount(String strAmount) throws ParseException {
        decimalFormatWithLocale.setParseBigDecimal(true);
        BigDecimal amount = (BigDecimal) decimalFormatWithLocale.parseObject(strAmount);
        return amount;
    }
}
