package raimon.rodriguez.luz.fixwifinotify;

import android.app.AlarmManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;
import android.app.PendingIntent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;





public class MainActivity extends ActionBarActivity  implements View.OnTouchListener {

    //variables globals
    protected static final String KEYPREF_ACTIVAT_O_NO = "preferencies_activat_o_no";
    protected static final String KEYPREF_PRIMER_COP = "primer_cop_que_empra_app";
    protected static final int MILISEGONS_ABANS_PRIMERA = 5000;
    protected static final int MINUTS_REPETEICO_ALARMA = 4;
    public static final String LOG_FILE = "LOG_FILE";

    //variables
    protected SharedPreferences mPrefs;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    AnimationDrawable anim_wifi;
    ImageView imatge_animada;
    protected boolean activat_o_no;
    protected boolean primera_vegada_obre_app;
    protected static final String ETIQUETA = "Fix Wifi Notify";
    protected TextView txt_info;
    int conta_butto_tocat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // llegeix preferencies
        llegeix_preferencies();

        // crea alarma però no L'ACTIVA
        Context context = this;
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // asigna textview
        txt_info = (TextView) findViewById(R.id.textView);

        // prepara animacio
        imatge_animada = (ImageView) findViewById(R.id.imageView2);


        // fa tocable la imatge
        imatge_animada.setOnTouchListener(this);

    }

    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        // llegeix preferencies i mostre segons opcions...

        // si es sa primera vegada que s'obre sa app activa alarma
        if(primera_vegada_obre_app)
        {
            // Log
            Log_meu(this, "Es la primera vegada que s'obre la APP. ");

            // activa alarma
            startAlert(this);

            // estableix que ja NO es sa primera vegada
            primera_vegada_obre_app = false;
            escriu_a_arxiu(this, KEYPREF_PRIMER_COP, "NO");
            mPrefs.edit().putBoolean(KEYPREF_PRIMER_COP, false).commit();
        }
        else
        {
            // Log
            Log_meu(this, "NOOOOO es la primera vegada que s'obre la APP.");
        }

        // si esta activat
        if(activat_o_no)
        {
            show_activat();
        }
        else
        {
            show_desactivat();
        }
    }

    // si esta activat es mostra així
    private void show_activat(){

        // text info
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // se app NO fa rés a no ser que hi hagi és WIFI encès (així estalviem bateria)
        if(wifiManager.isWifiEnabled()) {
            txt_info.setText(getString(R.string.txt_tot_on));
            // inicia_animacions
            imatge_animada.setImageResource(R.drawable.animation);
            anim_wifi = (AnimationDrawable) imatge_animada.getDrawable();
            anim_wifi.start();
        }else{
            // on però sense Wifi
            txt_info.setText(getString(R.string.txt_on_wifi_off));
            imatge_animada.setImageResource(R.drawable.logo_on_3);
        }

        // Log
        Log_meu(this, "Mostrant boto com activat.");
    }


    // si esta desactivat es mostra així...
    private void show_desactivat(){

        // inicia_animacions
        if(anim_wifi != null) {
            anim_wifi.stop();
        }
        imatge_animada.setImageResource(R.drawable.logo_off);

        // text info
        txt_info.setText(getString(R.string.txt_off));

        // Log
        Log_meu(this, "Mostrant boto com desactivat.");
    }


    public void startAlert(Context context) {

        // guarda a preferencies que esta activada
        activat_o_no = true;
        mPrefs.edit().putBoolean(KEYPREF_ACTIVAT_O_NO, true).commit();
        escriu_a_arxiu(getApplicationContext(), KEYPREF_ACTIVAT_O_NO, "SI");

        // la primera alarma sonarà al cap de 5 segons
        alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + MILISEGONS_ABANS_PRIMERA, alarmIntent);


        // i es repetirà alarma cada 4 MINUTs
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * MINUTS_REPETEICO_ALARMA, alarmIntent);

        // fica el boto com activat
        show_activat();

        // Log
        Log_meu(this, "Alarma ACTIVADA!");
    }


    /** Called when the user clicks the Send button */
    public void cancelar_alarma() {

        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        // guarda a preferencies que esta desactivada
        activat_o_no = false;
        mPrefs.edit().putBoolean(KEYPREF_ACTIVAT_O_NO, false).commit();
        escriu_a_arxiu(getApplicationContext(), KEYPREF_ACTIVAT_O_NO, "NO");

        // mostra el boto com a desactivat
        show_desactivat();

        // Log
        Log_meu(this, "Alarma CANCELADA.");
    }


    public void llegeix_preferencies() {
        // Extreu ses preferencies de s'usuari
        mPrefs = getPreferences(MODE_PRIVATE);

        // si encara no ha sigut guardat, serà TRUE per defecte
        activat_o_no = mPrefs.getBoolean(KEYPREF_ACTIVAT_O_NO, true);
        if(llegeix_arxiu(this, KEYPREF_PRIMER_COP) == "NO\n")
        {
            primera_vegada_obre_app = false;
        }
        else
        {
            primera_vegada_obre_app = true;
        }

        // Log
        Log_meu(this, "Les preferencies han sigut llegides.");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                // si esta activat desactiva i al revés
                if(activat_o_no)
                {
                    cancelar_alarma();
                }
                else
                {
                    startAlert(this);
                }
                break;
        }
        return true;
    }

    private static void escriu_a_arxiu(Context context, String nom_arxiu, String data) {
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
    private static String llegeix_arxiu(Context context, String nom_arxiu) {

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


    /* Així quan vulgui eliminar es log, simplement faig que no faci res aquesta funcio i ja està*/
    protected static void Log_meu(Context context, String missatge){
        // log a consola
        Log.i("LOGMEU", missatge);

        // per tal de incloure data davant el missatge
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        // limita el nombre de lineas de s'arxiu de regristre
        String txt_arxiu_registre = llegeix_arxiu(context, MainActivity.LOG_FILE) + currentDateTimeString + " ---"+missatge + "---";
        String[] lines = txt_arxiu_registre.split("\\r?\\n");
        //Log.i("LOGMEU", "lines logarxiu="+lines.length);

        int max_nombre_lineas_arxiu_registre = 20000;
        String txt_arxiu_registre_reduit = "";

        if(lines.length > max_nombre_lineas_arxiu_registre)
        {
            // només mantindra ses darreres lineas
            for (int i = lines.length - max_nombre_lineas_arxiu_registre; i < lines.length; i++) {
                txt_arxiu_registre_reduit += lines[i]+"\n";
            }

            // i escriu finalment sa versio reduida del registre
            escriu_a_arxiu(context, MainActivity.LOG_FILE, txt_arxiu_registre_reduit);
        }
        else
        {
            // pot esciure a arxiu també a registre
            escriu_a_arxiu(context, MainActivity.LOG_FILE, llegeix_arxiu(context, MainActivity.LOG_FILE) + currentDateTimeString+" ---"+missatge+"---");
        }
    }


    /* Funcio per activar log que esta amagat */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.MILLISECOND);
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && seconds > 500)){
            //Do something
            conta_butto_tocat += 1;
            if(conta_butto_tocat == 20) {

                conta_butto_tocat = 0;

                Intent logtxt = new Intent(getApplicationContext(), LogTxt.class);
                startActivity(logtxt);

            }

            //If you handled the event, return true. If you want to allow the event to be handled by the next receiver, return false.
            //return true;
        }

        return super.onKeyDown(keyCode, event);

    }

}


