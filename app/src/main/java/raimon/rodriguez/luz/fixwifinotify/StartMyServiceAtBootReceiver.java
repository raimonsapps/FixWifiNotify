package raimon.rodriguez.luz.fixwifinotify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import android.app.ActionBar;


/**
 * Created by raimon on 16/02/15.
 */
public class StartMyServiceAtBootReceiver extends BroadcastReceiver {


    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context_un, Intent intent_un) {

        // Log
        MainActivity.Log_meu(context_un, "Reactivant ALARMA si es necesari després de reinici de dispositiu...");

        // si el que creda la funcio d'aquesta clase es que es device s'ha reiniciat llavors...
        if ("android.intent.action.BOOT_COMPLETED".equals(intent_un.getAction())) {

            // Extreu si s'alarma estava activada o no
            //TODO ATENCIO Segurament problema \n
            //Todo també modifique a MAINACTIVITY!!!!
            if(llegeix_arxiu(context_un, "preferencies_activat_o_no").equals("SI\n"))
            {
                alarmMgr = (AlarmManager)context_un.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context_un, AlarmReceiver.class);
                alarmIntent = PendingIntent.getBroadcast(context_un, 0, intent, 0);

                // la primera alarma sonarà al cap de 5 segons
                alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                        + MainActivity.MILISEGONS_ABANS_PRIMERA, alarmIntent);


                // i es repetirà alarma cada 4 MINUTs
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        1000 * 60 * MainActivity.MINUTS_REPETEICO_ALARMA, alarmIntent);

                MainActivity.Log_meu(context_un, "ALARMA ESTAVA ACTIVADA, AIXI QUE S'HA ACTIVAT!!!");
            }
            else
            {
                MainActivity.Log_meu(context_un, "ALARMA NO ESTAVA ACTIVADA:"+llegeix_arxiu(context_un, "preferencies_activat_o_no"));
            }


        }
    }

    private void escriu_a_arxiu(Context context, String nom_arxiu, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(nom_arxiu, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    // per llegir arxiu log en el que escriu AlarmReceiver
    private String llegeix_arxiu(Context context, String nom_arxiu) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(nom_arxiu);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        if(ret.length() == 0)
        {
            return "";
        }
        else
        {
            return ret;
        }
    }
}
