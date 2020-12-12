package com.suvikas.conware.Activities;

import androidx.appcompat.app.AppCompatActivity;

import com.suvikas.conware.AndOTP;
import com.suvikas.conware.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class AttackAndOTP extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attack_apps);

        AndOTP aOtp = new AndOTP(getApplicationContext());
        String token = aOtp.decryptBackup();


        AlertDialog.Builder builder1 = new AlertDialog.Builder(AttackAndOTP.this);
        builder1.setTitle("HOTP Token Value");
        builder1.setMessage(token);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

   }
}
