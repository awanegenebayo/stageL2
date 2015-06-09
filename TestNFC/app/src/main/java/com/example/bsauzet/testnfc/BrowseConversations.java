package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class BrowseConversations extends Activity {

    ListView lv;
    SQLiteHelper sqLiteHelper;
    ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_conversations);

        users = new ArrayList<User>();
        lv = (ListView)findViewById(R.id.listViewConversation);
        sqLiteHelper = new SQLiteHelper(this);

        //Je récupere tous les messages qui me concernent
        List<Message> messages = sqLiteHelper.getMessagesFromPublicKeyDest(KeysHelper.getMyPublicKey());

        for(int i = 0 ; i < messages.size() ; i++)
            if(!userExists(messages.get(i).getPublicKeySource())){
                User temp = sqLiteHelper.getUserByPublicKey(messages.get(i).getPublicKeySource());
                if(temp != null)
                    users.add(temp);
                else
                    users.add(new User("Unknown("+nextUnknownId()+")", messages.get(i).getPublicKeySource()));
            }


        String[] lv_arr = new String[users.size()];

        for(int i = 0 ; i < users.size() ; i++)
            lv_arr[i] = users.get(i).getName();

        lv.setAdapter(new ArrayAdapter<String>(BrowseConversations.this, android.R.layout.simple_list_item_1, lv_arr));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {;
                String itemValue = (String) lv.getItemAtPosition(position);
                goToBrowseMessagesActivity(itemValue);
            }

        });
    }

    public int nextUnknownId(){
        int cmp = 0;
        for(int i = 0 ; i < users.size() ; i++)
            if(users.get(i).getName().toLowerCase().contains("Unknown".toLowerCase()))
                cmp++;

        return cmp+1;
    }

    public void goToBrowseMessagesActivity(String name){
        Intent intent = new Intent(this, BrowseMessages.class);
        User u = getUserByName(name);

        if(u != null) {
            intent.putExtra("name", u.getName());
            intent.putExtra("publicKey", u.getPublicKey());
            intent.setAction("NewActivity");
            startActivity(intent);
        }
    }

    public User getUserByName(String name){
        User u = null;
        for(int i = 0 ; i < users.size() ; i++)
            if(users.get(i).getName().equals(name)){
                u = users.get(i);
                break;
            }
        return u;
    }

    public boolean userExists( String publicKey){
        boolean exist = false;
        for(int i = 0 ; i < users.size() ; i++)
            if(users.get(i).getPublicKey().equals(publicKey)) {
                exist = true;
                break;
            }
        return exist;
    }
}
