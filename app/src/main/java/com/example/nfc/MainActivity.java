package com.example.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String STRING_KEY = "URL";
    private static final List<String> PROTOCOLS = Arrays.asList("http://", "https://");
    private static final int SELECTED_ITEM = 0;
    private String selected_url;
    private Uri created_uri;
    private String selected_protocol;
    Button button;
    TextView textView;
    Spinner spinner;
    Tag my_tag;
    Context context;

    // write to NFC tag

    // button that writes to nfc - generates NDEF record to write
    // We will need to use NfcAdapter it seems.
    // We should look at createURI method in NdefRecord class
    // https://developer.android.com/reference/android/nfc/NdefRecord.html#createUri%28android.net.Uri%29

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        spinner = findViewById(R.id.spinner);
        setSpinner();
        context = this;
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_url = textView.getText().toString();
                Toast.makeText(MainActivity.this, selected_protocol + selected_url, Toast.LENGTH_SHORT).show();
                created_uri = Uri.parse("selected_url");
                NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
                my_tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
                writeTag(created_uri, my_tag);
            }
        });
    }

    private void writeTag(Uri createdUri, Tag tag) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        selected_url = textView.getText().toString();
        outState.putString(STRING_KEY, selected_url);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STRING_KEY)) {
                selected_url = savedInstanceState.getString(STRING_KEY);
                textView.setText(selected_url);
            }
        }
    }

    private void setSpinner(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, PROTOCOLS);
        spinner.setAdapter(adapter);
    }
}