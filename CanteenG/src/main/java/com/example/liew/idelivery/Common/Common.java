package com.example.liew.idelivery.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.liew.idelivery.Model.User;

import java.util.Calendar;
import java.util.Locale;

public class Common {

    public static String topicName = "News";

    public static User currentUser;
    public static String currentKey;


    public static String PHONE_TEXT = "userPhone";

    public static final String INTENT_FOOD_ID = "FoodId";
    public static final int PICK_IMAGE_REQUEST = 71;
    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "password";

    public static String convertCodeToStatus(String status) {

        if(status.equals("0"))
            return "Order Confirmed";
        else if(status.equals("1"))
            return "Preparing Orders";
        else if(status.equals("2"))
            return "Shipping";
        else
            return "Delivered";
    }


    public static boolean isConnectedToInternet(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null){

            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info != null){

                for(int i=0; i<info.length; i++){
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static String getDate(long time)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(android.text.format.DateFormat.format("dd-MM-yyyy HH:mm"
                , calendar).toString());
        return date.toString();
    }

}
