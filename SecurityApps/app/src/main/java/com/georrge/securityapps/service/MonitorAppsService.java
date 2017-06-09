package com.georrge.securityapps.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.georrge.securityapps.database.FeedReaderContract;
import com.georrge.securityapps.database.FeedReaderDbHelper;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Георгий on 23.01.2017.
 */
public class MonitorAppsService extends IntentService {

    private boolean runService = true;
    private List<String> listSecurityApps = new LinkedList<>();

    public MonitorAppsService() {
        super("Start MonitorAppsService");
    }


    /*
    * Start service
    * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listSecurityApps.clear();       // clear list with password security apps

        // init green bus () - communication library
        if(!EventBus.getDefault().hasSubscriberForEvent(MessageToMonitorAppsService.class))
            EventBus.getDefault().register(this);

        // send message to main-activity, that service is start
        EventBus.getDefault().post(new MessageToMainActivity(0, "MonitorAppsService_is_start"));

        // get list the sec. apps.
        initPassList();

        return super.onStartCommand(intent,flags,startId);
    }


    /*
    * Main monitor cycle. Here processes is checked  by allow to work.
    * */
    @Override
    protected void onHandleIntent(Intent intent) {
        // while to receive command "stop_service" from main_activity
        while (runService) {
            // list of running application
            List<AndroidAppProcess> processes_list = AndroidProcesses.getRunningAppProcesses();
            for(AndroidAppProcess process : processes_list) {
                for (String processName : listSecurityApps) {
                    // check if running application recovery record
                    if (processes_list.get(processes_list.size() - 1).name.equals(processName))
                        // send to main_activity name of violator (then start checkPasswordActivity for violator)
                        EventBus.getDefault().post(new MessageToMainActivity(1, processName));

                    // if violator-application is running
                    if (process.name.equals(processName))
                        // stop him
                        killApplication(process.getPackageName());
                }
            }
        }
    }


    /*
    *  Destroy service.
    * */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // destroy green bus event
        EventBus.getDefault().unregister(this);

        // if allowed - restart the service
        if(runService)
            startService(new Intent(this, MonitorAppsService.class));
    }


    /*
    * Event-method for Green-Bus (class MessageToMonitorAppsService).
    * */
    @Subscribe
    public void onEvent(MessageToMonitorAppsService event){

        // 2 - allowed app
        if(event.code == 2)
            listSecurityApps.remove(event.message);

        //1 - password security app
        else if(event.code == 1)
            listSecurityApps.add(event.message);

        //0 - stop service
        else if(event.code == 0)
            runService = false;
            return;
    }

    /*
    * Kill background processes by package name
    * */
    private void killApplication(String KillPackage){
        ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(KillPackage);
    }

    /*
    * Get list password security apps from DB
    * */
    private void initPassList(){
        // create connection
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // The columns to return
        String[] projection = {
                FeedReaderContract.FeedEntry.COLUMN_NAME_NAME_APP
        };

        // query
        Cursor cursor = db.query(FeedReaderContract.FeedEntry.TABLE_NAME, projection, null, null, null, null, null);

        // parsing data by passList
        if (cursor.moveToFirst()) {
            do {
                String appPackageName = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME_APP));
                listSecurityApps.add(appPackageName);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }
}
