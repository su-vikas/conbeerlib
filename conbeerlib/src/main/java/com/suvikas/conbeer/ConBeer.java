package com.suvikas.conbeer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * ConBeer class implements various mechanisms for detecting presence of Android Virtual Containers.
 */

public class ConBeer {

    private Context mContext;
    private String TAG = "CONBEER";
    private ArrayList<String> checksDetected ;
    private ArrayList<String> appServiceNames ;

    /**
     * @param context - Android Application context.
     * @param appServiceNames - ArrayList of service, which will be created by the application.
     */
    public ConBeer(Context context, ArrayList<String> appServiceNames){
        this.mContext = context;
        this.checksDetected = new ArrayList<>();
        this.appServiceNames = appServiceNames;
    }

    /**
     * Get the list of checks which were detected.
     * @return List of checks detected.
     */
    public ArrayList<String> getListOfChecksDetected(){
        return checksDetected;
    }

    /**
     * Check if the application is running inside the container. The checks are divided into 3 parts
     * @return True, if virtual container detected
     */
    public boolean isContainer(){
        boolean isManifest = false;
        boolean isAppComponents = false;
        boolean isAppRuntime =  false;

        isAppRuntime = checkAppRuntime();
        isManifest = checkManifest();
        isAppComponents = checkAppComponents();

        if(isAppRuntime || isManifest || isAppComponents ) {
            return true;
        }
        return false;
    }

    /**
     * Check application's manifest related information.
     * @return True, if virtual container is detected.
     */
    public boolean checkManifest(){
        boolean bIsContainer = this.checkPermissions();
        if(bIsContainer){checksDetected.add(Checks.MANIFEST_PERMISSIONS);}

        //checkAppServiceName(null);
        return bIsContainer;
    }

    /**
     * Check application's runtime related information
     * 1. /proc/self/maps if any suspicious files included
     * 2. Check Environment variables
     * 3. Check Internal storage directory
     * 4. Check StackTrace
     * @return True, if virtual container is detected.
     */
    public boolean checkAppRuntime(){
        boolean isCheckProcMaps = checkProcMaps();
        if(isCheckProcMaps){ checksDetected.add(Checks.PROC_MAPS);}

        boolean isCheckEnvironment = checkEnvironment();
        if(isCheckEnvironment){checksDetected.add(Checks.ENVIRONMENT);}

        boolean isInternalStorageDir = checkInternalStorageDir();
        if(isInternalStorageDir){checksDetected.add(Checks.INTENAL_STORAGE_DIR);}

        boolean isInStackTrace = checkStackTrace();
        if(isInStackTrace) {checksDetected.add(Checks.STACKTRACE);}

        if(isCheckEnvironment || isCheckProcMaps || isInternalStorageDir || isInStackTrace){
            return true;
        }
        return false;
    }

    /**
     * Checks for application's component
     * 1. Check for Running App services
     * 2. Dynamically enable app components
     *
     * @return True, if virtual container is detected
     */

    public boolean checkAppComponents(){
        // TODO: somehow get input for this function from caller;
        boolean isCheckRunningAppServices = checkRunningAppServices(this.appServiceNames);
        if(isCheckRunningAppServices){checksDetected.add(Checks.RUNNING_SERVICES);}

        boolean isCheckAppComponentPropertyAtRuntime = checkAppComponentPropertyAtRuntime();
        if(isCheckAppComponentPropertyAtRuntime){checksDetected.add(Checks.COMPONENT_RUNTIME);}

        if(isCheckRunningAppServices || isCheckAppComponentPropertyAtRuntime){
            return true;
        }
        return false;
    }


    /**
     * Compares the MAC groups the app is assigned to with the permissions requested.
     * If there is an anomaly, then it is an indication of running inside a container
     *
     * @return True, if virtual container detected.
     */
    private boolean checkPermissions() {
        boolean bIsContainer = false;

        List<Pair<String,String>> permissionSEgroupList = new ArrayList<>();
        permissionSEgroupList.add(new Pair<>("android.permission.INTERNET", "inet"));
        permissionSEgroupList.add(new Pair<>("android.permission.BLUETOOTH_ADMIN","net_bt_admin"));
        permissionSEgroupList.add(new Pair<>("android.permission.BLUETOOTH", "net_bt"));

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = packageInfo.requestedPermissions;

            String segroupInfo = execute("id");
            if(segroupInfo != null) {

                for (int i = 0; i < permissionSEgroupList.size(); i++) {
                    Pair<String, String> pair = permissionSEgroupList.get(i);
                    if (segroupInfo.contains(pair.second)) {
                        if (requestedPermissions!= null && requestedPermissions.length != 0) {
                            boolean bIsPresent = false;
                            for (String s : requestedPermissions) {
                                if (s.contains(pair.first)) {
                                    bIsPresent = true;
                                    break;
                                }
                            }
                            if(!bIsPresent){
                                bIsContainer = true;
                                break;
                            }
                        } else {
                            bIsContainer = true;
                            break;
                        }
                    }
                }
            }
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return bIsContainer;
    }

    /**
     * /proc/self/maps should not have libraries or APK from other /data/app and /data/data folders,
     * other than the one of the app's.
     * Example:
     *  Dr.Clone: /data/app/com.trendmicro.tmas-nX-nxxGWSIQ3FOKGnz-Xbg==/lib/arm/libnativehook.so
     *            /data/app/com.trendmicro.tmas-nX-nxxGWSIQ3FOKGnz-Xbg==/lib/arm/libsubstrate.so
     *  Parallel Space:
     *           /data/app/com.lbe.parallel.intl-bp5H8cQ_sHHz72STgLNWfg==/lib/arm/libdaclient_64.so
     *
     * @return True, if virtual container detected
     */
    private boolean checkProcMaps(){
        boolean isContainer = false;
        try {
            if(BuildConfig.DEBUG){
                Log.d(TAG, ">>>>>>>>>>>>>> CHECK PROC MAPS <<<<<<<<<<<<<<<");
            }

            String packageName = mContext.getPackageName();
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream("/proc/self/maps"),
                    Charset.defaultCharset());
            BufferedReader bufferedReader = new BufferedReader(reader);

            List<String> paths = new ArrayList<String>();

            // get all the paths in proc/self/maps
            try {
                String line;
                do {
                    line = bufferedReader.readLine();
                    if (line == null)
                        break;

                    String[] parts = line.split(" ");

                    String tmp = parts[parts.length - 1];
                    paths.add(tmp);
                } while (true);

                // Check paths does not contain files from other /data/data and /data/app locations
                for(String p: paths){
                    if(p.startsWith("/data/app") || p.startsWith("/data/data")){
                        if(!p.contains(packageName)) {
                            isContainer = true;

                            if(BuildConfig.DEBUG) {
                                Log.d(TAG, "checkProcMaps: Suspicious file: " + p);
                            }

                            break;
                        }
                    }
                }
            } finally {
                try{
                    bufferedReader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e){
            throw new RuntimeException("Unable to open /proc/self/maps");
        }

        if(BuildConfig.DEBUG) {Log.d(TAG, "checkProcMaps: " + isContainer);}

        if(BuildConfig.DEBUG){
            Log.d(TAG, ">>>>>>>>>>>>>> CHECK PROC MAPS: DONE <<<<<<<<<<<<<<<");
        }
        return isContainer;
    }

    /**
     * Checks the internal storage directory of the application.
     * In a non-container scenario, it will be /data/data/package_name, while in case of container
     * it will be withing containers internal storage.
     * @return True, if virtual container detected
     */
    private boolean checkInternalStorageDir(){
        // Package Name: com.container.com.suvikas.conware  Dir: /data/data/com.lbe.parallel.intl.arm64/parallel_intl/0/com.container.com.suvikas.conware

        //TODO: improve exception handling, throw or handle it here?
        if(BuildConfig.DEBUG){
            Log.d(TAG, ">>>>>>>>>>>>>> CHECK INTERNAL STORAGE DIR <<<<<<<<<<<<<<<");
        }

        boolean isContainer = false;
        try {
            PackageManager pm = mContext.getPackageManager();
            String packageName = mContext.getPackageName();
            PackageInfo p = pm.getPackageInfo(packageName, 0);
            String appDir = p.applicationInfo.dataDir;

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Package Name: " + packageName + "  Dir: " + appDir);
            }

            String expectedAppDir = "/data/data/" + packageName;

            //TODO: check what will be case with multiple users
            String expectedAppDir2 = "/data/user/0/" + packageName;

            // generally container will have something like:
            // "/data/data/com.lbe.parallel.intl.arm64/parallel_intl/0/com.container.com.suvikas.conware"
            if(!appDir.startsWith(expectedAppDir) && !appDir.startsWith(expectedAppDir2)){
                Log.d(TAG, "check1: " + expectedAppDir);
                Log.d(TAG, "check2: " + expectedAppDir2);
                isContainer = true;
            }
        }catch(Exception e){ e.printStackTrace(); }

        if(BuildConfig.DEBUG){
            Log.d(TAG, "checkInternalStorageDir: " + isContainer);
            Log.d(TAG, ">>>>>>>>>>>>>> CHECK INTERNAL STORAGE DIR: DONE <<<<<<<<<<<<<<<");
        }

        return isContainer;
    }

   // TODO: check external storage, parallel space altered that too.

    /**
     * Check the env command output to see if LD_PRELOAD is present and points to a legitimate path.
     * If path is pointing to local sandbox data, then it is an indication of running inside a
     * container
     */
    private boolean checkEnvironment(){
        boolean bIsContainer = false;
        if(BuildConfig.DEBUG){Log.d(TAG,">>>>>> Environment Variables <<<<<");}
        String environmentStatus = execute("env");
        String [] blacklistedEnvs = Constants.BLACKLISTED_ENV_VARIABLES;

        if(environmentStatus != null) {
            String[] envs = environmentStatus.split("\n");

            for(String env: envs){
                if(env.contains("LD_PRELOAD")){
                    String [] preload = env.split("=");
                    if(BuildConfig.DEBUG){Log.d(TAG, "LD_PRELOAD: " + preload[1]);}
                    if(preload.length > 0) {
                        if( (preload[1].contains("/data/data")) || preload[1].contains("/data/app")){
                            bIsContainer = true;
                            break;
                        }
                    }
                }else{
                    for(String blacklistedEnv : blacklistedEnvs){
                        if(env.contains(blacklistedEnv)){
                            bIsContainer = true;
                            break;
                        }
                    }
                    if(bIsContainer)
                        break;
                }
            }
        }

        if(BuildConfig.DEBUG){Log.d(TAG,">>>>>> Environment Variables: DONE <<<<<");}
        return bIsContainer;
    }

    /**
     * Container may create stub component to fool the AMS in order to create a component that not
     * defined in the manifest file. API getRunningServices of ActivityManager to get the
     * information of the running service, we will get the name of the stub service,
     * such as stub.ServiceStubStubP08P00 if using DroidPlugin.
     * Example:
     *          In ParallelSpace:
     *                  com.lbe.parallel.service.KeyguardService
     *                  cn.thinkingdata.android.TDQuitSafelyService$TDKeepAliveService
     *                  com.lbe.parallel.install.AppInstallService
     *                  com.lbe.doubleagent.service.proxy.KeepAliveService
     * @param appServices: ArrayList of application' services
     *
     * @return True, if container detected.
     */
    @SuppressWarnings("deprecation")
    private boolean checkRunningAppServices(ArrayList<String> appServices){
        // IDEAS: https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android/608600#608600
        boolean isContainer = false;

        ArrayList<String> runningServices = new ArrayList<>();

        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        if(BuildConfig.DEBUG){Log.d(TAG, ">>>>>>>> APP SERVICE NAMES <<<<<<<<<<<<<");}

        // get services running for this application
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            String tmp = service.service.getClassName();
            runningServices.add(tmp);
            if(BuildConfig.DEBUG){Log.d(TAG, tmp);}
        }

        if(appServices == null){
           if(runningServices.size() > 0)
               // If no app services specified, there should be services running
               isContainer = true;
        }else{
            for(String srvc: appServices){
                runningServices.remove(srvc);
            }
            // apart from app services no other services should be there.
            if(runningServices.size() > 0)
                isContainer = true;
        }

        Log.d(TAG, "checkAppServiceName: " + isContainer);
        Log.d(TAG, ">>>>>>>> APP SERVICE NAMES: DONE <<<<<<<<<<<<<");
        return isContainer;
    }

    private boolean checkAppComponentPropertyAtRuntime(){
        boolean isContainer = false;
        if(BuildConfig.DEBUG){
            Log.d(TAG, ">>>>>>>>>>>> APP COMPONENT PROPERTY AT RUNTIME: <<<<<<<<<<<<<<<<");
        }

        // send message
        Intent intent = new Intent("com.container.conbeer.intent.TEST");
        intent.setPackage("com.container.com.suvikas.conware");
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        mContext.sendBroadcast(intent);
        try{
            Thread.sleep(1000); // to ensure broadcastreceiver process runs and updates sharedprefs
        }catch(Exception e){
            e.printStackTrace();
        }

        SharedPreferences settings = mContext.getSharedPreferences("Prefs", 0);
        boolean isReceived = settings.getBoolean("received", false);
        if(isReceived){
            if(BuildConfig.DEBUG){ Log.d(TAG, "Broadcast intent received"); }
            isContainer = false;
        }else{
            if(BuildConfig.DEBUG){ Log.d(TAG, "Broadcast intent NOT received"); }
            isContainer = true;
        }

        if(BuildConfig.DEBUG){
            Log.d(TAG, ">>>>>>>>>>>> APP COMPONENT PROPERTY AT RUNTIME: DONE<<<<<<<<<<<<<<<<");
        }

        return isContainer;
    }

    private String execute(String command) {
        try{
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String buffer = null;
            while((buffer = bufferedReader.readLine()) != null) {
                builder.append("\n");
                builder.append(buffer);
            }
            return builder.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the stacktrace for the current execution and check for the presence of blacklisted
     * classes in the stacktrace
     * @return True, if virtual container detected
     */
    private boolean checkStackTrace() {
        if(BuildConfig.DEBUG){
            Log.d(TAG, ">>>>>>>>>>>> STACKTRACE AT RUNTIME: START<<<<<<<<<<<<<<<<");
        }
        String[] blackListedClassNameList = Constants.BLACKLISTED_STACKTRACE_CLASSES;
        boolean bRet = false;
        StackTraceElement[] stackTraces = new Throwable().getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {
            final String clazzName = stackTrace.getClassName();
            Log.d(TAG, clazzName);
            for(String blacklistedClassName: blackListedClassNameList){
                if(clazzName.contains(blacklistedClassName)){
                    bRet = true;
                }
            }
        }
        if(BuildConfig.DEBUG){
            Log.d(TAG, ">>>>>>>>>>>> STACKTRACE AT RUNTIME: DONE<<<<<<<<<<<<<<<<");
        }
        return bRet;
    }
}
