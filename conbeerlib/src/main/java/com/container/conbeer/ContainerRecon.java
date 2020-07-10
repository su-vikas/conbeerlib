package com.container.conbeer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/***
 * Recon information about the virtual container.
 * Collect information about the container and other installed applications.
 */

public class ContainerRecon {

    private Context mContext;
    private String TAG = "CONBEER";

    public ContainerRecon(Context context){
        this.mContext = context;
    }

    /***
     * Get list of installed apps in the container.
     */
    public ArrayList<String> getInstalledApps(){
        String contDir = this.getContainerAppDir();
        ArrayList<String> installedApps = new ArrayList<>();

        if(contDir == null){
            return null;
        }

        File[] files = new File(contDir).listFiles();
        for(File f: files){
            if(f.isDirectory()){
                if(BuildConfig.DEBUG) { Log.d(this.TAG, "Directory: " + f.getName()); }
                installedApps.add(f.getName());
            }
        }
        return installedApps;
    }

    /**
     * Get directory of the Container.
     * Not to be confused with the application's directory, which is inside this container dir.
     */
    public String getContainerAppDir(){
        // Package Name: com.container.conware  Dir: /data/data/com.lbe.parallel.intl.arm64/parallel_intl/0/com.container.conware
        // TODO: parse out the package dir from this.
        String androidDataDir = "/data/data";
        String packageName = mContext.getPackageName();

        String appDir = this.getAppDir();
        String conDir = "";

        boolean isADataDir = appDir.startsWith(androidDataDir);

        if(BuildConfig.DEBUG) {
            Log.d(TAG, "Container app starts with /data/data/: " + isADataDir);
        }

        if(isADataDir){
            String[] arr = appDir.split(packageName);
            conDir = arr[0];
        } else{
            Log.d(this.TAG,"Container Application Directory is weird.");
        }

        if(BuildConfig.DEBUG) {
            Log.d(this.TAG, "Container Dir: " + conDir);
        }

        return conDir;
    }


    /**
     * @return all key aliases present in the keystore.
     */
    public ArrayList<String> getAllKeyAliases(){
        try {
            KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
            keystore.load(null);
            Enumeration<String> allKeys = keystore.aliases();
            ArrayList<String> keys = new ArrayList<>();
            for(String k: Collections.list(allKeys)){
                keys.add(k);
            }
            return keys;
        }catch(KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Exception occurred");
            }
            return null;
        }
    }

    /**
     * Get the application's storage directory
     * @return App directory string
     */
    public String getAppDir(){
        try {
            PackageManager pm = mContext.getPackageManager();
            String packageName = mContext.getPackageName();
            PackageInfo p = pm.getPackageInfo(packageName, 0);
            String appDir = p.applicationInfo.dataDir;
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Package Name: " + packageName + "  Dir: " + appDir);
            }
            return appDir;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the name of the virtual container app being used.
     * TODO: Perform signature detection
     */
    public void getContainerName(){
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
