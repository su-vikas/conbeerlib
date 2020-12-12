package com.suvikas.conware.Activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.suvikas.conware.Constants;
import com.suvikas.conware.R;

import java.util.ArrayList;
import java.util.List;

public class RunningAppsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final List<RunningAppData> apps = getListofRunningApps();

        ArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.activity_listview, apps);
        ListView listView = (ListView) findViewById(R.id.applist);
        listView.setAdapter(adapter);

    }

    private class RunningAppData {

        private String uid;
        private String pid;
        private String appName;

        RunningAppData(String uid, String pid, String appName) {
            this.uid = uid;
            this.pid = pid;
            this.appName = appName;
        }
    }

    private class CustomArrayAdapter extends ArrayAdapter<RunningAppsListActivity.RunningAppData> {

        List<RunningAppsListActivity.RunningAppData> appDataList = new ArrayList<>();

        CustomArrayAdapter(@NonNull Context context, int resource, @NonNull List<RunningAppsListActivity.RunningAppData> objects) {
            super(context, resource, objects);
            appDataList = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.rowlayout_runningapps, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.label);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            RunningAppData appData = appDataList.get(position);
            if(position == 0){
                String textToDisplay = appData.uid + "\t\t\t\t\t" + appData.pid + "\t\t\t\t\t" + appData.appName;
                textView.setText(textToDisplay);
                imageView.setImageResource(R.drawable.app);
            }else {
                PackageManager pm = getApplicationContext().getPackageManager();
                Drawable drawableIcon = null;
                String textToDisplay = null;
                try {
                    ApplicationInfo applicationInfo = pm.getApplicationInfo(appData.appName, 0);
                    drawableIcon = applicationInfo.loadIcon(pm);
                    imageView.setImageDrawable(drawableIcon);
                    CharSequence appName = pm.getApplicationLabel(applicationInfo);
                    textToDisplay = appData.uid + "\t\t\t" + appData.pid + "\t\t\t" + appName;
                    textView.setText(textToDisplay);
                } catch (PackageManager.NameNotFoundException e) {
                    imageView.setImageResource(R.drawable.app);
                    textToDisplay = appData.uid + "\t\t\t" + appData.pid + "\t\t\t" + appData.appName;
                    textView.setText(textToDisplay);
                }
            }

            return rowView;
        }

    }

    private List<RunningAppsListActivity.RunningAppData> getListofRunningApps(){

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppsListActivity.RunningAppData> appDataList = new ArrayList<>();
        appDataList.add(new RunningAppData("UID","PID","Process Name"));
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();

        if(runningAppProcesses != null) {
            for (int i = 0; i < runningAppProcesses.size(); i++) {
                Log.d(Constants.TAG, "Process:"+ runningAppProcesses.get(i).processName);
                appDataList.add(new RunningAppData(Integer.toString(runningAppProcesses.get(i).uid),
                        Integer.toString(runningAppProcesses.get(i).pid), runningAppProcesses.get(i).processName));
            }
        }
        return appDataList;

    }

}

