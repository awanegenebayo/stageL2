package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;


public class BrowseContacts extends Activity {

    ListView lv;
    SQLiteHelper sqLiteHelper;
    String itemToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_contacts);


        lv = (ListView)findViewById(R.id.ListContacts);
        sqLiteHelper = new SQLiteHelper(this);

        updateView();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ;
                String itemValue = (String) lv.getItemAtPosition(position);
                goToSendActivity(itemValue);
            }

        });

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemToDelete = (String) lv.getItemAtPosition(position);

                new AlertDialog.Builder(BrowseContacts.this)
                        .setTitle("Delete contact")
                        .setMessage("Are you sure you want to delete this contact?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            User u = sqLiteHelper.getUserByName(itemToDelete);

                            public void onClick(DialogInterface dialog, int which) {
                                sqLiteHelper.deleteUser(u);

                                updateView();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });

    }

    public void updateView(){
        List<User> users = sqLiteHelper.getAllUsers();

        String[] lv_arr = new String[users.size()];

        for(int i = 0 ; i < users.size() ; i++)
            lv_arr[i] = users.get(i).getName();

        lv.setAdapter(new ArrayAdapter<String>(BrowseContacts.this, android.R.layout.simple_list_item_1, lv_arr));
    }


    public void goToSendActivity(String name){
        Intent intent = new Intent(this, SendMessage.class);
        intent.putExtra("name", name);
        intent.putExtra("pk", sqLiteHelper.getUserByName(name).getPublicKey());
        intent.setAction("NewActivity");
        startActivity(intent);
        finish();
    }


}
