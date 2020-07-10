package com.container.conware.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.container.conbeer.ConBeer;
import com.container.conware.KeystoreUtil;
import com.container.conware.R;

import java.util.ArrayList;

public class DetectContainer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_container);

        ConBeer cb = new ConBeer(getApplicationContext(), null);
        if (cb.isContainer()) {
            Toast.makeText(getApplicationContext(), "Running in Container", Toast.LENGTH_SHORT).show();
        }

        final ArrayList<String> checksDetected = cb.getListOfChecksDetected();
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, checksDetected);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ListView listView = (ListView) findViewById(R.id.keys_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                KeystoreUtil keystoreUtil = new KeystoreUtil();
                String keyInfo = keystoreUtil.getKeyInfo(checksDetected.get(pos));
                alertDialog.setTitle("Container Checks Detected")
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

