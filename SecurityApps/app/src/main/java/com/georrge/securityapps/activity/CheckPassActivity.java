package com.georrge.securityapps.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.georrge.securityapps.R;
import com.georrge.securityapps.service.MessageToMonitorAppsService;

import org.greenrobot.eventbus.EventBus;

import static com.georrge.securityapps.activity.UsefulMethods.getPressmark;

/*
* Class for start activity. Check password and run app.
* */
public class CheckPassActivity extends AppCompatActivity {

    private EditText passField;

    // information about showed application
    private String packageName;
    private String appName;
    private String activityName;
    private String passwordApp;
    private Bitmap iconApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_app);

        passField = (EditText)findViewById(R.id.SA_password_field);

        Intent intent = getIntent();
        getDataFromIntent(intent);                  // get information about showed application from intent

        // update view data
        TextView titleTextView = (TextView) findViewById(R.id.SA_app_name);
        titleTextView.setText(appName);
        ImageView imageView = (ImageView)findViewById(R.id.SA_app_image);
        imageView.setImageBitmap(iconApp);
    }

    /*
    * Onclick method for button 'Start application'
    * */
    public void startApp(View v){
        // check password if true
        if(checkPass()){

            // send message to service: mark this application how allowed
            EventBus.getDefault().post(new MessageToMonitorAppsService(2, packageName));

            // run a selected application
            launchApp(packageName, activityName);
        }else{
            Toast.makeText(this, "Password is incorrect!", Toast.LENGTH_LONG).show();
        }

    }

    /*
    *  Verification of old password
    * */
    private boolean checkPass(){
        String inputPass =  "" + getPressmark(passField.getText().toString());

        return inputPass.equals(passwordApp);
    }


    /*
    * Get data from passed intent.
    * */
    private void getDataFromIntent(Intent intent){
        byte[] byteArray = intent.getExtras().getByteArray("iconAppBitmapImage");
        packageName = intent.getStringExtra("packageName");
        appName = intent.getStringExtra("appName");
        activityName = intent.getStringExtra("activityName");
        iconApp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        passwordApp = intent.getStringExtra("passwordApp");
    }

    /*
    * Run the selected application
    * */
    private void launchApp(String packageName, String activityName) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        startActivity(intent);
    }
}