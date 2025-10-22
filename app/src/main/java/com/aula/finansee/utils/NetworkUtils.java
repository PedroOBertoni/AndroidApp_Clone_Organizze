package com.aula.finansee.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

// NetworkUtils.java
public class NetworkUtils {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean conectado = false;

        if (cm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // Para Android 6.0 (API 23) e superior
                NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                conectado = (nc != null &&
                        (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)));
            } else {
                // Para versões antigas (pré-M)
                NetworkInfo ni = cm.getActiveNetworkInfo();
                conectado = (ni != null && ni.isConnected());
            }
        }
        return conectado;
    }
}