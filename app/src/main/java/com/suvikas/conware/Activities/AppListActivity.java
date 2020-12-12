package com.suvikas.conware.Activities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.suvikas.conware.Constants;
import com.suvikas.conware.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final List<AppData> apps = getListofAppsinContainer();

        ArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.activity_listview, apps);
        ListView listView = (ListView) findViewById(R.id.applist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppData appData = apps.get(i);
                String destinationPath = "/mnt/sdcard/"+appData.appName+".zip";
                zipDatafolder(apps.get(i).appDataPath, destinationPath );

                Toast.makeText(getApplicationContext(), "AppData exported to external Directory", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class AppData {

        private String packageName;
        private String appName;
        private String appDataPath;

        AppData(String packageName, String appName, String appDataPath){
            this.packageName = packageName;
            this.appName = appName;
            this.appDataPath = appDataPath;
        }
    }

    private class CustomArrayAdapter extends ArrayAdapter<AppData> {

        List<AppData> appDataList = new ArrayList<>();
        CustomArrayAdapter(@NonNull Context context, int resource, @NonNull List<AppData> objects) {
            super(context, resource, objects);
            appDataList = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.rowlayout_applist, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.label);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            String packageName = appDataList.get(position).packageName;
            ImageView zipView = (ImageView) rowView.findViewById(R.id.zip);
            zipView.setImageResource(R.drawable.zip);

            PackageManager pm = getApplicationContext().getPackageManager();
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                Drawable drawableIcon = applicationInfo.loadIcon(pm);
                imageView.setImageDrawable(drawableIcon);
                textView.setText(appDataList.get(position).appName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            return rowView;
        }

    }

    private List<AppData> getListofAppsinContainer(){

        List<AppData> appDataList = new ArrayList<>();
        String path = getApplicationContext().getApplicationInfo().dataDir;
        String[] conDir = path.split(getPackageName());

        File dir = new File(conDir[0]);
        File[] files = dir.listFiles();
        if(files != null) {
            for (File file : files) {
                PackageManager pm = getApplicationContext().getPackageManager();
                try {
                    ApplicationInfo applicationInfo = pm.getApplicationInfo(file.getName(), 0);

                    appDataList.add(new AppData(file.getName(), pm.getApplicationLabel(applicationInfo).toString(), file.getPath()));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return appDataList;

    }

    private void zipDatafolder(String filepath, String destinationPath){
        ZipOutputStream zipOutputStream = null;
        try{
            Log.d(Constants.TAG, "Path:"+filepath);
            FileOutputStream fileOutputStream = new FileOutputStream(destinationPath);
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

            File appDir = new File(filepath);
            zipFolder("",appDir,zipOutputStream);

            zipOutputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }


    private void zipFolder(String basePath, File f, ZipOutputStream zipOutputStream) throws IOException {

        File[] files = f.listFiles();
        if(files != null) {
            for (File f1 : files) {
                if (f1.isDirectory()) {
                    String path = basePath + f1.getName() + "/";
                    zipOutputStream.putNextEntry(new ZipEntry(path));
                    zipFolder(path, f1, zipOutputStream);
                    zipOutputStream.closeEntry();
                } else {
                    byte[] buf = new byte[1024];
                    int len;

                    FileInputStream in = new FileInputStream(f1);
                    zipOutputStream.putNextEntry(new ZipEntry(basePath + f1.getName()));

                    while ((len = in.read(buf)) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                    zipOutputStream.closeEntry();
                    in.close();
                }
            }
        }


    }
}
