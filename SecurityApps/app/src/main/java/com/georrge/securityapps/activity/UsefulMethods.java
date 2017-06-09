package com.georrge.securityapps.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by Георгий on 28.01.2017.
 */

/*
* Class consist useful methods.
* They are separate in a individual class for the simplification of the main_activity
* */
public class UsefulMethods {

    private PackageManager pm;
    private List<ResolveInfo> launchers;
    private int idCurrentItem;

    UsefulMethods(PackageManager pm, List<ResolveInfo> launchers, int idCurrentItem){
        this.pm = pm;
        this.launchers = launchers;
        this.idCurrentItem = idCurrentItem;
    }


    /*
    * Return encrypted password.
    * */
    public static int getPressmark(String password){
        return password.hashCode();
    }


    /*
    * Add a correctly password, gotten from dataBase.
    * */
    public Intent addPassword(Intent intent, String password){
        intent.putExtra("passwordApp",password);
        return intent;
    }

    /*
    *  Added some information about application.
    * */
    public Intent addData(Intent intent){
        byte[] iconItemApp = getBitmapAppImage();
        String appName = launchers.get(idCurrentItem).loadLabel(pm).toString();
        String packageName = launchers.get(idCurrentItem).activityInfo.packageName;
        String activityName = launchers.get(idCurrentItem).activityInfo.name;

        intent.putExtra("packageName",packageName);
        intent.putExtra("appName",appName);
        intent.putExtra("iconAppBitmapImage", iconItemApp);
        intent.putExtra("activityName", activityName);

        return intent;
    }

    /* Convert Drawable in byte array.
    *  Conversion is needed for pass icon item app by new activity.
    * */
    private byte[] getBitmapAppImage(){
        Drawable aw = launchers.get(idCurrentItem).loadIcon(pm);
        Bitmap bmp = ((BitmapDrawable)aw).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

}
