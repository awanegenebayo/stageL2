package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class BrowseMessages extends Activity {

    ListView lv;
    SQLiteHelper sqLiteHelper;

    String userName;
    String userPk;
    String itemToDelete;

    String myPublicKey;

    EditText mEdit;

    private DiscussArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_messages);

        Intent intent = getIntent();

        userName = intent.getStringExtra("name");
        userPk = intent.getStringExtra("publicKey");

        TextView tv = (TextView)findViewById(R.id.contactName);
        tv.setText(userName);

        lv = (ListView)findViewById(R.id.listView);
        sqLiteHelper = new SQLiteHelper(this);
        mEdit = (EditText)findViewById(R.id.editText);


        myPublicKey = KeysHelper.getMyPublicKey();

        updateView();

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               itemToDelete = (String) lv.getItemAtPosition(position);

                new AlertDialog.Builder(BrowseMessages.this)
                        .setTitle("Delete message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            List<Message> messagesToDelete = sqLiteHelper.getMessagesChatFromContentAndSender(itemToDelete, userName );
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i = 0 ; i < messagesToDelete.size() ; i++)
                                    sqLiteHelper.deleteMessageChat(messagesToDelete.get(i));

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
                return false;
            }
        });


    }

    public void updateView(){
        final List<Message> messages = sqLiteHelper.getMessagesChatConcerningUser(userPk);

        sortMessagesByDate(messages);

        adapter = new DiscussArrayAdapter(getApplicationContext(), R.layout.listitem_discuss);
        lv.setAdapter(adapter);

        if(messages != null) {
            for (int i = 0; i < messages.size(); i++) {
                //MESSAGE FOR ME
                if (messages.get(i).getPublicKeyDest().equals(myPublicKey)){
                    adapter.add(new OneComment(true, new String(messages.get(i).getContent())));
                }
                //MESSAGE BY ME
                if (messages.get(i).getPublicKeySource().equals(myPublicKey)) {
                    adapter.add(new OneComment(false, new String(messages.get(i).getContent())));
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    public byte[] getEncryptedMessage(String message){
        try {
            return CryptoHelper.RSAEncrypt(message, userPk);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendButton(View view) {
        byte[] text = getEncryptedMessage(mEdit.getText().toString());
        Message messageEncr = new Message(text, myPublicKey, userPk);
        Message messageCl = new Message(mEdit.getText().toString().getBytes(), myPublicKey, userPk);


        sqLiteHelper.addMessage(messageEncr);
        sqLiteHelper.addMessageToChat(messageCl);
        Toast.makeText(BrowseMessages.this, "Message sent", Toast.LENGTH_SHORT).show();
        updateView();
        mEdit.setText("");
        lv.setSelection(adapter.getCount() -1);
    }

    public void sortMessagesByDate(List<Message> m){
        if(m.size()>0)
            quickSort(0, m.size() -1, m);
        for(int i = 0 ; i < m.size() ; i++)
            Log.i("tamere", "date : "+m.get(i).getDate());
    }

    public void quickSort(int lowerIndex, int higherIndex, List<Message> m){

        int i = lowerIndex;
        int j = higherIndex;

        double pivot = m.get(lowerIndex+(higherIndex-lowerIndex)/2).getDate();

        while (i <= j) {
            while (m.get(i).getDate() < pivot) {
                i++;
            }
            while (m.get(j).getDate() > pivot) {
                j--;
            }
            if (i <= j) {
                exchangeMessages(i, j, m);
                i++;
                j--;
            }
        }
        if (lowerIndex < j)
            quickSort(lowerIndex, j, m);
        if (i < higherIndex)
            quickSort(i, higherIndex, m);
    }
    private void exchangeMessages(int i, int j, List<Message> m) {
        double temp = m.get(i).getDate();
        m.get(i).setDate(m.get(j).getDate());
        m.get(j).setDate(temp);
    }
}
