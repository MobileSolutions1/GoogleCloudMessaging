package chat.ulife.com.br.gcm_push;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String ID_REGISTRO_RET = "ID_REGISTRO_RET";
    private String num_projeto = "779755635636";
    Button btnRegistrar, btnLimpar;
    TextView txvMsg;

    GoogleCloudMessaging gcm;
    Context context;
    String id_resg_google;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRegistrar = (Button) findViewById(R.id.btnRegistarIDApp);
        btnLimpar = (Button) findViewById(R.id.btnLimpar);
        txvMsg = (TextView) findViewById(R.id.txvMsg);

        context = getApplicationContext();
    }

    public void onClick(final View view) {

        if (view == btnRegistrar) {

            gcm = GoogleCloudMessaging.getInstance(this);

            id_resg_google = getRegistrationId(context);

            if (id_resg_google.equals("")) {
                registerInBackground();
            }

            String msg_ = id_resg_google;
            txvMsg.setText(msg_);
            Log.i("GCM", msg_);
        } else {
            txvMsg.setText("");
        }
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(ID_REGISTRO_RET, "");
        if (registrationId.equals("")) {
            return "";
        }

        int registeredVersion = prefs.getInt("1", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGcmPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    id_resg_google = gcm.register(num_projeto);
                    msg = "Device registrado, ID=" + id_resg_google;

                    storeRegistrationId(context, id_resg_google);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                txvMsg.setText(msg + "\n");
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ID_REGISTRO_RET, regId);
        editor.putInt("1", appVersion);
        editor.commit();
    }
}
