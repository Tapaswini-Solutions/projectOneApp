package com.example.projectone;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Button syncButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        syncButton = findViewById(R.id.sync_button);

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    synchronizeContacts();
                } else {
                    requestPermission();
                }
            }
        });
    }

    private boolean checkPermission() {
        int readContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int writeContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);
        return readContactsPermission == PackageManager.PERMISSION_GRANTED &&
                writeContactsPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSION_REQUEST_CODE);
    }

    private void synchronizeContacts() {
        // Retrieve and sync contacts
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

//        if (cursor != null) {
//            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
//            int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
//
//            while (cursor.moveToNext()) {
//                if (nameColumnIndex != -1 && idColumnIndex != -1) {
//                    String contactName = cursor.getString(nameColumnIndex);
//                    String contactId = cursor.getString(idColumnIndex);
//
//                    // Sync contact information as needed
//                    Log.d("ContactSync", "Name: " + contactName + ", ID: " + contactId);
//                } else {
//                    // Handle the case where the columns don't exist
//                    Log.e("ContactSync", "DISPLAY_NAME or _ID columns do not exist in the cursor.");
//                }
//            }
//
//            cursor.close();
//        }
//        else {
//            // Handle the case where the cursor is null
//            Log.e("ContactSync", "Cursor is null.");
//        }
        cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {  // Check if the cursor contains data
                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);

                if (nameColumnIndex == -1 || idColumnIndex == -1) {
                    // Handle the case where the columns don't exist
                    Log.e("ContactSync", "DISPLAY_NAME or _ID columns do not exist in the cursor.");
                } else {
                    do {
                        String contactName = cursor.getString(nameColumnIndex);
                        String contactId = cursor.getString(idColumnIndex);

                        // Retrieve phone numbers for this contact
                        Cursor phoneCursor = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{contactId},
                                null
                        );

                        if (phoneCursor != null) {
                            while (phoneCursor.moveToNext()) {
                                int phoneNumberColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                if (phoneNumberColumnIndex != -1) {
                                    String phoneNumber = phoneCursor.getString(phoneNumberColumnIndex);

                                    // Display the contact information
                                    Log.d("ContactSync", "Name: " + contactName + ", ID: " + contactId + ", Phone: " + phoneNumber);

                                    // You can also display this information in your app's user interface
                                    // For example, you can append it to a TextView or use it in any way you need.
                                } else {
                                    // Handle the case where the phone number column doesn't exist
                                    Log.e("ContactSync", "Phone number column does not exist in the phoneCursor.");
                                }
                            }
                            phoneCursor.close();
                        } else {
                            // Handle the case where phoneCursor is null
                            Log.e("ContactSync", "PhoneCursor is null for contact ID: " + contactId);
                        }
                    } while (cursor.moveToNext());  // Move to the next contact
                }
                cursor.close();
            } else {
                // Handle the case where the cursor is empty
                Log.e("ContactSync", "Cursor is empty.");
            }
        } else {
            // Handle the case where the cursor is null
            Log.e("ContactSync", "Cursor is null.");
        }
        // Perform the contact synchronization process here....
        // This is where you can save or send contact data to your server, for example
        // Implement your synchronization logic based on your app's requirements
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                synchronizeContacts();
            } else {
                Toast.makeText(this, "Permission denied. Cannot sync contacts.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
