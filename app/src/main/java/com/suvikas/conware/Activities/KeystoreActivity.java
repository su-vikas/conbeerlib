package com.suvikas.conware.Activities;

import android.content.DialogInterface;
import android.os.Bundle;

import com.suvikas.conbeer.ContainerRecon;
import com.suvikas.conware.KeystoreUtil;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.suvikas.conware.R;

import java.util.ArrayList;

public class KeystoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keystore);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ContainerRecon cr = new ContainerRecon(getApplicationContext());

        final ArrayList<String> keys = cr.getAllKeyAliases();
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, keys);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ListView listView = (ListView) findViewById(R.id.keys_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                KeystoreUtil keystoreUtil = new KeystoreUtil();
                String keyInfo = keystoreUtil.getKeyInfo(keys.get(pos));
                alertDialog.setTitle("Android KeyInfo")
                        .setMessage(keyInfo)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                            }
                        })
                        .show();
            }
        });
    }
}
