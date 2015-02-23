package raimon.rodriguez.luz.fixwifinotify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.Toast;


public class LogTxt extends FragmentActivity {

    private static final int ALERTTAG = 0, PROGRESSTAG = 1;
    protected static final String TAG = "AlertDialogActivity";
    private Button mShutdownButton = null;
    private DialogFragment mDialog;
    TextView txt;
    EditText edit_txt;
    boolean primer_cop = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_txt);

        edit_txt = (EditText) findViewById(R.id.edit_txt1);
        txt = (TextView) findViewById(R.id.textView_log);
        txt.setText(llegeix_arxiu(this, MainActivity.LOG_FILE));
        txt.requestFocus();

        // recupera el valor de variable primer_cop per tal de que no sigui reiniciada cada vegada que es gira sa pantalla
        if (savedInstanceState != null) {
            primer_cop = savedInstanceState.getBoolean("key_primercop");
            edit_txt.setText(savedInstanceState.getString("key_edittxt"));
        }

        // dona benvinguda
        if (primer_cop) {
            primer_cop = false;

            Toast.makeText(getApplicationContext(), "Benvingut!", Toast.LENGTH_SHORT).show();

            // torna el volum a tope
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    audio.setStreamVolume(AudioManager.STREAM_RING, audio.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                    audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

                    if (audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && audio.getStreamVolume(AudioManager.STREAM_RING) == audio.getStreamMaxVolume(AudioManager.STREAM_RING)) {
                        Toast.makeText(getApplicationContext(), "El volum ha sigut pujat al màixm!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "ATENCIÓ! Revisa com ha quedat el volum... RING_MODE="+audio.getRingerMode()+" VOLUM="+audio.getStreamVolume(AudioManager.STREAM_RING)+" MAX_VOL="+audio.getStreamMaxVolume(AudioManager.STREAM_RING), Toast.LENGTH_LONG).show();
                    }
                }
            }, 2000);
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        // salva sa variable primer_cop per evitar que es borri cada vegada que giram pantalla
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("key_primercop", primer_cop);
        savedInstanceState.putString("key_edittxt", edit_txt.getText().toString());
    }




    public void eliminar_log(View view){
        showDialogFragment(ALERTTAG);
    }

    public void eliminar_log_ok(){
        // Buida Text
        escriu_a_arxiu(this, MainActivity.LOG_FILE, "");

        // Actualitza
        txt.setText(llegeix_arxiu(this, MainActivity.LOG_FILE));
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

    private String llegeix_arxiu(Context context, String nom_arxiu) {

            String ret = "";

            try {
                InputStream inputStream = context.openFileInput(nom_arxiu);

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                        stringBuilder.append("\n");
                    }

                    inputStream.close();
                    ret = stringBuilder.toString();
                }
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }

            if (ret.length() == 0) {
                return "";
            } else {
                return ret;
            }
    }

    /* Insert text to the log_file*/
    public void insert_txt(View view){
        MainActivity.Log_meu(this, edit_txt.getText().toString());
        txt.setText(llegeix_arxiu(this, MainActivity.LOG_FILE));
    }


    /***************** apartir d'aquí TOT es msgbox confirmarcio *********************************/
    void showDialogFragment(int dialogID) {
        switch (dialogID) {
            case ALERTTAG:
                mDialog = AlertDialogFragment.newInstance();
                mDialog.show(getSupportFragmentManager(), "Alert");
                break;
            case PROGRESSTAG:
                mDialog = ProgressDialogFragment.newInstance();
                mDialog.show(getSupportFragmentManager(), "Shutdown");
                break;
        }
    }

    protected void continueShutdown(boolean shouldContinue) {
        if (shouldContinue) {
            eliminar_log_ok();
        } else {
            mDialog.dismiss();
        }
    }


    public static class AlertDialogFragment extends DialogFragment {

        public static AlertDialogFragment newInstance() {
            return new AlertDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Estas segur que vols buidar l'arxiu log?")
                    .setCancelable(false)
                    .setNegativeButton("NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    ((LogTxt) getActivity())
                                            .continueShutdown(false);
                                }
                            })
                    .setPositiveButton("ELIMINAR",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        final DialogInterface dialog, int id) {
                                    ((LogTxt) getActivity())
                                            .continueShutdown(true);
                                }
                            }).create();
        }
    }

    public static class ProgressDialogFragment extends DialogFragment {

        public static ProgressDialogFragment newInstance() {
            return new ProgressDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Activity Shutting Down.");
            dialog.setIndeterminate(true);
            return dialog;
        }
    }
}


