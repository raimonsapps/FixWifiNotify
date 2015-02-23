package raimon.rodriguez.luz.fixwifinotify;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import java.util.List;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {


        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

//        se app NO fa rés a no ser que hi hagi és WIFI encès (així estalviem bateria)
        if(wifiManager.isWifiEnabled()) {


            // llegeix traffic, espera uns 5 segons i torna a llegir, si no hi ha hagut canvi llavors desconecta wifi
            final long BytesRebutsDesdeBoot_ini = TrafficStats.getTotalRxBytes();
            final long BytesEnviatsDsdBoot_ini = TrafficStats.getTotalTxBytes();


            final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);


            // a partir d'aqui s'executa 5 segons més tart per poder saber si hi ha alguna conexió a la xarxa funcionant
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    // torna a llegir bytes conneccions per saber si movil esta fent alguna tasca
                    long BytesRebutsDesdeBoot_fin = TrafficStats.getTotalRxBytes();
                    long BytesEnviatsDsdBoot_fin = TrafficStats.getTotalTxBytes();
                    boolean device_is_ussing_net;

                    // si el total de bytes transmesos en 3 segons (enviats+rebuts) es inferior a 800 voldir que NO hi ha cap conecció activa
                    //...i per lo tant podem procedir a reiniciar wifi.
                    long total_bytes_rebuts = BytesRebutsDesdeBoot_fin - BytesRebutsDesdeBoot_ini;
                    long total_bytes_enviats = BytesEnviatsDsdBoot_fin - BytesEnviatsDsdBoot_ini;
                    if ((total_bytes_rebuts + total_bytes_enviats) < 600) {
                        device_is_ussing_net = false;

                    } else {
                        device_is_ussing_net = true;

                    }



                    // comprova si hi ha conexió online (sino NO reiniciarà wifi)
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();


                    // aixo es per reiniciar whatsapp (només és reinicia si tens conexió a internet)
                    if (isConnected) {
                        //Log
                        MainActivity.Log_meu(context, "Es mòbil està conectat online... mirant si NO empra xarxa...!!!");

                        // es wifi es reinicia només si esta activat, si tens conexió a internet i sino hi ha cap activitat online en marxa.
                        if (!device_is_ussing_net) {

                            //Log
                            MainActivity.Log_meu(context, "Es mòbil NO està emprant xarxa així que SONANT ALARMA!!!");

                            // apaga wifi
                            wifiManager.setWifiEnabled(false);
                            MainActivity.Log_meu(context, "ATURANT Wifi...");

                            wifiManager.setWifiEnabled(true);
                            MainActivity.Log_meu(context, "REACTIVANT Wifi...");

                            // Aixo s'executa si wifi esta activiat i tens conexió online i sa pantalla esta apagada (es a dir s'usuari NO empra es movil)
                            //TODO fes prova per saber si això té algun efecte. SINO elimineu....
                            //per sa saber si sa pantalla està encesa
                            boolean pantalla_on;
                            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                            MainActivity.Log_meu(context, "SDK_INT ="+currentapiVersion);
                            if (currentapiVersion > Build.VERSION_CODES.KITKAT){
                                pantalla_on =  powerManager.isInteractive();
                            } else{
                                pantalla_on = powerManager. isScreenOn();
                            }


                            // ! vol dir negació, així que només farà el seguent si pantalla està apagada.
                            if(!pantalla_on) {

                                MainActivity.Log_meu(context, "Sa pantalla esta apagada així que farem intent a whatsapp");

/*   FICAR AIXO???? ==>         ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                                activityManager.killBackgroundProcesses("com.whatsapp");
                                clearMemory(context);*/

                                // Obrir Whatsapp --- de moment NO, a veure que
                                final Intent sendIntent = new Intent();
                                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// has afegit això
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, ""); // aqui hi havia text
                                sendIntent.setPackage("com.whatsapp");
                                sendIntent.setType("text/plain");
                                context.startActivity(sendIntent);
                                MainActivity.Log_meu(context, "Intent a Whatsapp...");



                                // programa reinici wifi per al cap de pocs segons
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {


                                        // reactivant wifi
                                        // això ho has mogut...

                                        MainActivity.Log_meu(context, "Va a pantalla principal");
                                        //va a pantalla inicial android
                                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                                        startMain.addCategory(Intent.CATEGORY_HOME);
                                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(startMain);

                                    }
                                }, 3000);

                            }



                            //TODO cancela intent whatsapp o reiniciar servei whatsapp...
/*                            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                            activityManager.killBackgroundProcesses("com.whatsapp");
                            clearMemory(context);*/



                            //


                        }
                        else
                        {
                            MainActivity.Log_meu(context, "SI que empra xarxa amb bytes = "+(total_bytes_rebuts + total_bytes_enviats));
                        }

                    }

                }
            }, 3000);
        }
        else{
            MainActivity.Log_meu(context, "Es wifi esta APAGAT, així que NO fa res");
        }

    }


    // funcio fa net memoria, reinicia processos background de TOTES apps???
    public static void clearMemory(Context context) {
        ActivityManager activityManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
        if (list != null)
            for (int i = 0; i < list.size(); i++) {
                ActivityManager.RunningAppProcessInfo apinfo = list.get(i);

                String[] pkgList = apinfo.pkgList;

                if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE ) {
                    for (int j = 0; j < pkgList.length; j++) {
                        activityManger.killBackgroundProcesses(pkgList[j]);
                    }
                }
            }
    }

}
