package com.example.callsguard.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.PowerManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.callsguard.Activity.MainActivity;
import com.example.callsguard.Class.Contact;
import com.example.callsguard.Class.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class CallReceiver extends BroadcastReceiver {
    DatabaseReference myRef;
    FirebaseDatabase database;
    static String callingNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        database = FirebaseDatabase.getInstance();

        //Keep CPU working
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "callGuard:tag");
        wl.acquire();

        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            myRef = database.getReference();

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot keyNode : snapshot.getChildren()) {
                        for (DataSnapshot keyNode2 : keyNode.child("contacts").getChildren()) {
                            Contact contact=keyNode2.getValue(Contact.class);
                            if(PhoneNumberUtils.compare(User.getInstance().getId(),contact.getNumber())||PhoneNumberUtils.compare(callingNumber,contact.getNumber())){
                                contact.setBusy(true);
                                keyNode2.getRef().setValue(contact);
                            }
                        }

                    }
                }


                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

          //  showToast(context, "Call started");

        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            myRef = database.getReference();

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot keyNode : snapshot.getChildren()) {
                        for (DataSnapshot keyNode2 : keyNode.child("contacts").getChildren()) {
                            Contact contact=keyNode2.getValue(Contact.class);
                            if(PhoneNumberUtils.compare(User.getInstance().getId(),contact.getNumber())||PhoneNumberUtils.compare(callingNumber,contact.getNumber())){
                                contact.setBusy(false);
                                keyNode2.getRef().setValue(contact);
                            }
                        }

                    }
                }


                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });


          //  showToast(context, "Call ended");

        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            callingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (callingNumber != null) {
                myRef = database.getReference();

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        boolean flag=false;
                        for (DataSnapshot keyNode : snapshot.getChildren()) {
                            for (DataSnapshot keyNode2 : keyNode.child("contacts").getChildren()) {
                                Contact contact=keyNode2.getValue(Contact.class);
                                if(PhoneNumberUtils.compare(callingNumber,contact.getNumber())){
                                    contact.setBusy(true);
                                    keyNode2.getRef().setValue(contact);
                                    showToast(context,contact.getName());
                                    flag=true;
                                    break;
                                }
                                if(PhoneNumberUtils.compare(User.getInstance().getId(),contact.getNumber())){
                                    contact.setBusy(true);
                                    keyNode2.getRef().setValue(contact);
                                }
                            }
                        if (flag){
                            break;
                        }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }
    }

    void showToast(Context context, String massage) {
        Toast toast = Toast.makeText(context, massage, Toast.LENGTH_LONG);
        toast.show();
    }

}
