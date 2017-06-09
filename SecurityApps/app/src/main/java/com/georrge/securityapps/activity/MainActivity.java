package com.georrge.securityapps.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.georrge.securityapps.R;
import com.georrge.securityapps.database.FeedReaderContract;
import com.georrge.securityapps.database.FeedReaderDbHelper;
import com.georrge.securityapps.service.MessageToMainActivity;
import com.georrge.securityapps.service.MessageToMonitorAppsService;
import com.georrge.securityapps.service.MonitorAppsService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* Class main_activity. It functions:
* 1. Work with service and transmit message to it
* 2. Show the list applications and work with other activity
* 3. Work with database (little)
* */
public class MainActivity extends AppCompatActivity implements Constants, AdapterView.OnItemClickListener {

    private PackageManager pm;
    private List<ResolveInfo> launchers;                        // information about installed applications
    private ListView appList;                                   // list-view for showed inst.app.
    private AppListAdapter appListAdapter;                      // adapter for view this list

    private List<Button> itemButtons = new LinkedList<>();      // list of visible buttons for select app.
    private int idCurrentItem = 0;                              // id of selected item in view-list

    private Map<String, String> passList = new HashMap<>();     // package name and password.
                                                                // data get from database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pm = this.getPackageManager();

        // get list with inf. about inst. app.
        Intent main=new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        launchers=pm.queryIntentActivities(main, 0);

        // create list view of inst.app.
        appListAdapter = new AppListAdapter(this, pm, launchers);// create adaptor for  list view
        appList = (ListView) findViewById(R.id.main_appList);    // get a main view list
        appList.setAdapter(appListAdapter);
        appList.setOnItemClickListener(this);           // set onclick method by items in list-view
        appList.clearChoices();
        appListAdapter.notifyDataSetChanged();

        // restart a service
        startMonitorAppsService();

        // create password list for apps from database
        initPassList();

        // init a green bus communication
        if(!EventBus.getDefault().hasSubscriberForEvent(MessageToMainActivity.class))
            EventBus.getDefault().register(this);

    }



    /*
    * Event-method for Green-Bus (class MessageToMainActivity).
    * 12*/
    @Subscribe
    public void onEvent(MessageToMainActivity event){

        // check code of message
        if(event.code == 0){ // it is message received when passList should be supplemented
            passList.put(event.message, event.additional);
        }
        // receive a package name of violator-application
        else if (event.code == 1){

            String packageName = event.message;

            // get id of violator-application in launchers list
            int idItem;
            for(idItem = 0; idItem < launchers.size(); idItem++)
                if(launchers.get(idItem).activityInfo.applicationInfo.packageName.equals(packageName))
                    break;

            // start a CheckPasswordActivity
            Intent intent = new Intent(MainActivity.this, CheckPassActivity.class);
            UsefulMethods um = new UsefulMethods(pm, launchers, idItem);
            intent = um.addData(intent);
            intent = um.addPassword(intent, passList.get(packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        // delete select app from sec.list (when password was deleted)
        else if(event.code == 2){
            passList.remove(event.message);
            // send message to service: mark this application how allowed
            EventBus.getDefault().post(new MessageToMonitorAppsService(2, event.message));
        }
    }


    /*
    * Start application, or run startAppActivity (onclick)
    * */
    public void startApplication(View v){
        String packageName = launchers.get(idCurrentItem).activityInfo.packageName;
        String activityName = launchers.get(idCurrentItem).activityInfo.name;

        // if app has a password, then run CheckPassActivity (input password)
        if(passList.containsKey(packageName)){

            Intent intent = new Intent(MainActivity.this, CheckPassActivity.class);
            UsefulMethods um = new UsefulMethods(pm, launchers, idCurrentItem);
            intent = um.addData(intent);
            intent = um.addPassword(intent, passList.get(packageName));
            startActivity(intent);

        }else{ // else start application

            launchApp(packageName, activityName);
        }
    }

    /*
    EventBus.getDefault().unregister(this);
    */

    /*
    * Run a changePassActivity (onclick)
    * */
    public void changePass(View v){
        String packageName = launchers.get(idCurrentItem).activityInfo.packageName;
        Intent intent = new Intent(MainActivity.this, ChangePassActivity.class);
        UsefulMethods um = new UsefulMethods(pm, launchers, idCurrentItem);
        intent = um.addData(intent);
        intent = um.addPassword(intent, passList.get(packageName));
      //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }


    /*
    * Run a addPassActivity (onclick)
    * */
    public void addPass(View v){
        Intent intent = new Intent(MainActivity.this, AddPassActivity.class);
        UsefulMethods um = new UsefulMethods(pm, launchers, idCurrentItem);
        intent = um.addData(intent);
       // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }


    /* It is onclick method for button update.
    * Reload information about installed applications
    * */
    public void updateListInstallApps(View v){
        pm = this.getPackageManager();
        Intent main=new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        launchers=pm.queryIntentActivities(main, 0);
        appList.clearAnimation();
        appListAdapter = new AppListAdapter(this, pm, launchers);
        appList.setAdapter(appListAdapter);
    }


    /* It is method of selection for list view
    *  Current item has visible buttons, other hidden buttons
    * */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        idCurrentItem = position;   // save id for new item

        for(Button b : itemButtons) // hidden unused, previously buttons
            b.setVisibility(View.GONE);

        itemButtons.clear();

        // create list of visible buttons
        itemButtons.add((Button)view.findViewById(R.id.list_startApp_button));
        if(passList.containsKey(launchers.get(idCurrentItem).activityInfo.packageName))
            itemButtons.add((Button)view.findViewById(R.id.list_changePass_button));
        else
            itemButtons.add((Button)view.findViewById(R.id.list_addPass_button));

        for(Button b : itemButtons) // show buttons for current items
            b.setVisibility(View.VISIBLE);
    }


    /*
    * Restart MonitorAppsService
    * */
    private void startMonitorAppsService(){
       /* if(isMyServiceRunning(MonitorAppsService.class)) {
            // stop service
            EventBus.getDefault().post(new MessageToMonitorAppsService(0, "stop_MonitorAppsService"));
            stopService(new Intent(this, MonitorAppsService.class));
        }*/

        if(!isMyServiceRunning(MonitorAppsService.class)) {
            // start service
            Intent intent = new Intent(this, MonitorAppsService.class);
            startService(intent);
        }
    }


    /*
    * Return true if service is working
    * */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /*
    * Run the selected application
    * */
    private void launchApp(String packageName, String activityName) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }



    /*
    * Get all password from DB to PassList
    * */
    private void initPassList(){
        // create connection
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        //mDbHelper.onCreate(db);

        // The columns to return
        String[] projection = {
                FeedReaderContract.FeedEntry.COLUMN_NAME_NAME_APP,
                FeedReaderContract.FeedEntry.COLUMN_NAME_PASS
        };

        // query
        Cursor cursor = db.query(FeedReaderContract.FeedEntry.TABLE_NAME, projection, null, null, null, null, null);

        // parsing data by passList
        if (cursor.moveToFirst()) {
            do {
                String appPackageName = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME_APP));
                String password= cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_PASS));
                passList.put(appPackageName, password);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
