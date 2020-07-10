package com.container.conware;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.container.conbeer.utils.FakeBroadcastReceiver;
import com.container.conware.Activities.AppListActivity;
import com.container.conware.Activities.AttackAndOTP;
import com.container.conware.Activities.CommandExecActivity;
import com.container.conware.Activities.DetectContainer;
import com.container.conware.Activities.KeystoreActivity;
import com.container.conware.Activities.RunningAppsListActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String [] list = new String[]{"List Installed Apps", "List Running Apps", "Keys in Keystore", "Command Execution", "Attack andOTP", "Detect Virtual Container"};

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, list);
        ListView listView = (ListView) findViewById(R.id.main_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                switch(pos){
                    case 0:
                        startActivity(new Intent(getApplicationContext(), AppListActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(getApplicationContext(), RunningAppsListActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(getApplicationContext(), KeystoreActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(getApplicationContext(), CommandExecActivity.class));
                        break;
                    case 4:
                        startActivity(new Intent(getApplicationContext(), AttackAndOTP.class));
                        break;
                    case 5:
                        startActivity(new Intent(getApplicationContext(), DetectContainer.class));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if(BuildConfig.DEBUG) {
            Log.d("CONBEER", ">> Enabling BroadcastReceiver...");
        }
        //ComponentName componentName = new ComponentName("com.container.conbeer", FakeBroadcastReceiver.class.getName());
        ComponentName componentName = new ComponentName(this.getApplicationContext(), FakeBroadcastReceiver.class);
        this.getPackageManager().setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
