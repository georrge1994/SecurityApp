package com.georrge.securityapps.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import com.georrge.securityapps.database.FeedReaderContract;
import com.georrge.securityapps.database.FeedReaderDbHelper;
import com.georrge.securityapps.service.MessageToMainActivity;
import com.georrge.securityapps.service.MessageToMonitorAppsService;

import org.greenrobot.eventbus.EventBus;

import static com.georrge.securityapps.activity.UsefulMethods.getPressmark;

/*
* Class for adding password activity.
* */
public class AddPassActivity extends AppCompatActivity implements Constants{

    private EditText passField;

    // data of application
    private String packageName;
    private String appName;
    private Bitmap iconApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pass);

        passField = (EditText)findViewById(R.id.addPass_password_field);

        // get data from intent about a view application
        Intent intent = getIntent();
        getDataFromIntent(intent);

        // set name and img the current application
        TextView titleTextView = (TextView) findViewById(R.id.addPass_app_name);
        titleTextView.setText(appName);
        ImageView imageView = (ImageView)findViewById(R.id.addPass_app_image);
        imageView.setImageBitmap(iconApp);
    }

    /*
    * Onclick method for button 'add Password'
    * */
    public void saveAddedPass(View v){
        int length = passField.getText().toString().length();

        // check a password`s correction
        if( length > MIN_LENGTH_PASS && length < MAX_LENGTH_PASS) {
            addNewDataInDataBase(); // update information in data base

            Toast.makeText(this, "Password was added", Toast.LENGTH_LONG).show();

            // send name of sec.app. by service
            EventBus.getDefault().post(new MessageToMonitorAppsService(1, packageName.toString()));

            //send new password to main_activity for ListSecApps
            String pressmark = "" + getPressmark(passField.getText().toString());
            EventBus.getDefault().post(new MessageToMainActivity(0, packageName.toString(), pressmark));

            // return by main activity
            Intent intent = new Intent(AddPassActivity.this, MainActivity.class);
            startActivity(intent);

        }else{
            Toast.makeText(this, "Password is incorrect!", Toast.LENGTH_LONG).show();
        }
    }

    /*
    * Add new row in table(name_package's_application, password)
    * */
    private void addNewDataInDataBase(){
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int pressmark = getPressmark(passField.getText().toString());

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME_APP, packageName);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_PASS, pressmark);
        db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
    }

    /*
    * Get data from passed intent.
    * */
    private void getDataFromIntent(Intent intent){
        byte[] byteArray = intent.getExtras().getByteArray("iconAppBitmapImage");
        packageName = intent.getStringExtra("packageName");
        appName = intent.getStringExtra("appName");
        iconApp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
