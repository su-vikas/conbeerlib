package com.suvikas.conware.Activities;

import android.os.Bundle;

import com.suvikas.conware.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandExecActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView commandText;
    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_exec);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button idBtn = findViewById(R.id.id);
        Button netBtn = findViewById(R.id.env);
        Button processCmd = findViewById(R.id.processcmd);
        commandText = findViewById(R.id.command);
        outputText = findViewById(R.id.output);
        outputText.setMovementMethod(new ScrollingMovementMethod());

        idBtn.setOnClickListener(this);
        netBtn.setOnClickListener(this);
        processCmd.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        String output = null;
        switch(view.getId()){
            case R.id.id:
                output = getProcessesThroughCommand("id");
                break;
            case R.id.env:
                output = getProcessesThroughCommand("env");
                break;
            case R.id.processcmd:
                output = getProcessesThroughCommand(commandText.getText().toString());
                break;
        }
        if(output != null){
            outputText.setText(output);
        }
    }
    private String getProcessesThroughCommand(String command) {

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


}
