package com.example.callsguard.Activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.callsguard.Adapter.ContactsAdapter;
import com.example.callsguard.Class.Contact;
import com.example.callsguard.R;
import com.example.callsguard.Class.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private String phoneNumber;
    private RecyclerView mRecyclerView;
    public static ContactsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        phoneNumber = getIntent().getStringExtra("phoneNum");
        mRecyclerView = findViewById(R.id.contacts_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);


        RequestMultiplePermissions.launch(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG});


    }


    private void getContact() {
        contacts = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (isContainAbc(name)&&!phoneNo.contains("-") && !phoneNo.contains(" ") ) {
                            contacts.add(new Contact(name, phoneNo));
                        }
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        //Remove duplicates
        HashSet<Contact> hashSet = new HashSet(contacts);
        contacts.clear();
        contacts.addAll(hashSet);

        Collections.sort(contacts, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        User.initUser(MainActivity.this, phoneNumber, contacts.size(), contacts);

        myRef = database.getReference(User.getInstance().getId()).child("contactsNum");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    myRef.setValue(User.getInstance());
                } else if (snapshot.getValue(Integer.class) != User.getInstance().getContactsNum()) {
                    myRef.setValue(User.getInstance());

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        //Display contacts recycleView
        myRef = database.getReference(User.getInstance().getId());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User.getInstance().getContacts().clear();
                for (DataSnapshot keyNode : snapshot.child("contacts").getChildren()) {
                    Contact contact = keyNode.getValue(Contact.class);
                    if (!contact.getNumber().contains("-") && !contact.getNumber().contains(" ")) {
                        User.getInstance().getContacts().add(contact);
                    }
                }


                mAdapter = new ContactsAdapter(User.getInstance().getContacts());
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_seatch);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private final ActivityResultLauncher<String[]> RequestMultiplePermissions =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {
                boolean granted = true;
                for (Map.Entry<String, Boolean> x : isGranted.entrySet())
                    if (!x.getValue()) granted = false;
                if (granted) {
                    Log.d("pttt", "Is Granted");
                    Log.d("pttt", "action ! !");
                    // Permission is granted. Continue the action or workflow in your app.
                    getContact();
                } else {
                    getPermissions(Manifest.permission.READ_CONTACTS);
                    getPermissions(Manifest.permission.READ_PHONE_STATE);
                    getPermissions(Manifest.permission.READ_CALL_LOG);

                    Log.d("pttt", "No Granted");
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

                }
            });

    private void getPermissions(String permission) {
        if (shouldShowRequestPermissionRationale(permission)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            requestWithExplainDialog();

        } else if (!shouldShowRequestPermissionRationale(permission)) {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            manuallyDialog(permission);
        } else {
            Log.d("pttt", "Cant Action ! !");
        }
    }

    private void requestWithExplainDialog() {
        AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Need permission for send your code")
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    private void manuallyDialog(String permission) {
        if (shouldShowRequestPermissionRationale(permission)) {
            Log.d("pttt", "Cant Action ! !");
            return;
        }

        String message = "Setting screen if user have permanently disable the permission by clicking Don't ask again checkbox.";
        AlertDialog alertDialog =
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        manuallyActivityResultLauncher.launch(intent);
                                        dialog.cancel();
                                    }
                                }).show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    ActivityResultLauncher<Intent> manuallyActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            Log.d("pttt", "action ! !");
                            getContact();
                        } else {
                            // getPermissions(Manifest.permission.READ_CONTACTS);
                            // getPermissions(Manifest.permission.READ_PHONE_STATE);
                            //  getPermissions(Manifest.permission.READ_CALL_LOG);

                        }
                    }
                }
            });
    public boolean isContainAbc(String name){
        boolean flag=false;
        for(int i = 0;i<name.length();i++){
            if(!Character.isDigit(name.charAt(i))&&name.charAt(i)!='-'&&name.charAt(i)!='+'&&name.charAt(i)!='*'&&name.charAt(i)!=' '){
                flag=true;
                break;
            }
        }
        return flag;
    }

}