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

import org.greenrobot.eventbus.EventBus;

import static com.georrge.securityapps.activity.UsefulMethods.getPressmark;

/*
* Class for activity change password.
**/
public class ChangePassActivity extends AppCompatActivity{

    private EditText currentPassField, newPassField;

    // information about showed application
    private String packageName;
    private String appName;
    private String passwordApp;
    private Bitmap iconApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        currentPassField = (EditText)findViewById(R.id.chPass_old_password_field);
        newPassField = (EditText)findViewById(R.id.chPass_new_password_field);

        Intent intent = getIntent();
        getDataFromIntent(intent);              // get information about showed application from intent

        // update view data
        TextView titleTextView = (TextView) findViewById(R.id.chPass_app_name);
        titleTextView.setText(appName);
        ImageView imageView = (ImageView)findViewById(R.id.chPass_app_image);
        imageView.setImageBitmap(iconApp);
    }


    /*
    * Onclick method for button 'change Password'
    * */
    public void saveChangedPass(View v){

        // check current password is true
        if(checkCurrentPass()){
            String newPass = newPassField.getText().toString();

            // check a password`s correction
            if(newPass.length() < 1 || newPass.length() > 16){

                Toast.makeText(this, "New password is incorrect!", Toast.LENGTH_LONG).show();

            }else{
                changePassInDataBase();// update information in data base

                //send new password to main_activity for ListSecApps
                String pressmark = "" + getPressmark(newPassField.getText().toString());
                EventBus.getDefault().post(new MessageToMainActivity(0, packageName.toString(), pressmark));

                Toast.makeText(this, "Password was changed!", Toast.LENGTH_LONG).show();

                // return by main activity
                Intent intent = new Intent(ChangePassActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }else{
            Toast.makeText(this, "Old password is incorrect!", Toast.LENGTH_LONG).show();
        }
    }

    public void deletePassword(View view) {
        // check current password is true
        if(checkCurrentPass()){

            removePassInDataBase();// remove app from sec. table

            //send new password to main_activity for ListSecApps
            EventBus.getDefault().post(new MessageToMainActivity(2, packageName.toString()));

            Toast.makeText(this, "Password was deleted!", Toast.LENGTH_LONG).show();

            // return by main activity
            Intent intent = new Intent(ChangePassActivity.this, MainActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Old password is incorrect!", Toast.LENGTH_LONG).show();
        }
    }

    /*
    * Rewrite password in database(name_package's_application, password)
    * */
    private void changePassInDataBase(){
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        int pressmark = getPressmark(newPassField.getText().toString());

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_PASS, pressmark); // здесь должно быть шифрование
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_NAME_APP + " LIKE ?";
        String[] selectionArgs = { String.valueOf(packageName) };

        db.update(FeedReaderContract.FeedEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private void removePassInDataBase(){
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_NAME_APP + " LIKE ?";
        String[] selectionArgs = { String.valueOf(packageName) };
        db.delete(FeedReaderContract.FeedEntry.TABLE_NAME, selection, selectionArgs);
    }

    /*
    * Get data from passed intent.
    * */
    private void getDataFromIntent(Intent intent){
        byte[] byteArray = intent.getExtras().getByteArray("iconAppBitmapImage");
        packageName = intent.getStringExtra("packageName");
        appName = intent.getStringExtra("appName");
        iconApp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        passwordApp = intent.getStringExtra("passwordApp");
    }

    /*
    *  Verification of old password
    * */
    private boolean checkCurrentPass(){
        String inputCurrentPass =  "" + getPressmark(currentPassField.getText().toString());
        return inputCurrentPass.equals(passwordApp);
    }


}
