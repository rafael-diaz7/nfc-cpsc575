package com.example.nfc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String STRING_KEY = "URL";
    private static final List<String> PROTOCOLS = Arrays.asList("https://", "http://");
    private static final int SELECTED_ITEM = 0;
    private String selected_url;
    private Uri created_uri;
    private String selected_protocol;
    private boolean write;
    Button button;
    TextView textView;
    Spinner spinner;
    Activity act;
    PendingIntent pendingIntent;
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        spinner = findViewById(R.id.spinner);
        setSpinner();
        act = this;
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this,
                0,
                new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
                if (parent.getChildAt(SELECTED_ITEM) != null) {
                    selected_protocol = PROTOCOLS.get(pos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            selected_url = textView.getText().toString();
            Toast.makeText(MainActivity.this, selected_protocol + selected_url, Toast.LENGTH_SHORT).show();
            created_uri = Uri.parse(selected_protocol + selected_url);
            nfcAdapter.enableForegroundDispatch(act, pendingIntent, null, null);
        });


    }

    @Override
    protected void onPause(){
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void writeTag(Uri createdUri, Tag tag) {
        Ndef ndef = Ndef.get(tag);
        NdefRecord recordNFC = NdefRecord.createUri(createdUri);
        NdefMessage message = new NdefMessage(recordNFC);
        try {
            ndef.connect();
            ndef.writeNdefMessage(message);
            ndef.close();
            Toast.makeText(this, "Write to tag successful!", Toast.LENGTH_SHORT).show();
        }
        catch(IOException | FormatException e){
            Toast.makeText(this, "Write Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        Switch switch1 = findViewById(R.id.switch1);
        write = switch1.isChecked();
        // write tag
        if (write) {
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                    NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
                    NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                assert tag != null;
                writeTag(created_uri, tag);
            }
        }
        // read tag
        else {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){
                Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if(rawMessages != null) {

                    NdefMessage[] messages = new NdefMessage[rawMessages.length];
                    for(int i = 0; i < rawMessages.length; i++) {

                        messages[i] = (NdefMessage) rawMessages[i];

                    }
                    for(NdefMessage message : messages){
                        NdefRecord[] records = message.getRecords();
                        for (NdefRecord record:records){

                            // the following commented code will parse the tag IF you know what the type is beforehand
                            // byte[] header = record.getType();
                        	//short tnf = record.getTnf();
                        	//if(tnf==1){
                            //	Uri read_uri = record.toUri();
                            //	int i = 4;
                        	//}

                            byte[] payload = record.getPayload();
                            String scheme = "";
                            if(payload[0]==4){
                                scheme = "https://";
                            }
                            String utfMSG = new String(Arrays.copyOfRange(payload, 1, payload.length));
                            Toast.makeText(this, scheme+utfMSG, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        selected_url = textView.getText().toString();
        outState.putString(STRING_KEY, selected_url);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(STRING_KEY)) {
            selected_url = savedInstanceState.getString(STRING_KEY);
            textView.setText(selected_url);
        }
    }

    private void setSpinner(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_layout_simple, PROTOCOLS);
        spinner.setAdapter(adapter);
    }
}