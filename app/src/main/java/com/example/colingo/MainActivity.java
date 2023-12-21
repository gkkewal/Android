package com.example.colingo;

import com.example.colingo.Display;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.mlkit.common.model.DownloadConditions;
//import com.google.mlkit.nl.translate.TranslateLanguage;
//import com.google.mlkit.nl.translate.Translation;
//import com.google.mlkit.nl.translate.Translator;
//import com.google.mlkit.nl.translate.TranslatorOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import com.google.firebase.FirebaseApp;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private ArrayList<String> translationResults = new ArrayList<>();
    private ArrayList<String> telephoneResults = new ArrayList<>();
    Translator englishGujaratiTranslator;
    private Object objectdownload;
    private BufferedReader reader;
    //private static Uri uri;
    Button loadButton, translate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.GUJARATI).build();
        englishGujaratiTranslator = Translation.getClient(options);

        Button loadButton = findViewById(R.id.loadButton);
        Button transButton = findViewById(R.id.translate);
        Button download = findViewById(R.id.download);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open file picker
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*"); // VCF MIME type
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        transButton.setOnClickListener(view -> {
            downloadmodule();
            if (objectdownload == null) {
                try {
                    BufferedReader readertext = reader;
                    String line;
                    while ((line = readertext.readLine()) != null) {
                        //Log.d("VCF_DATA", line);
                        if (line.startsWith("FN")) {
                            Log.d("FN", line.substring(3));
                            translator(line.substring(3), new TranslationCallback() {
                                @Override
                                public void onTranslationComplete(Object result) {
                                    Log.d("Transl", result.toString());
                                }
                            });
                        } else if (line.startsWith("TEL")) {
                            Log.d("TEL", line.substring(9));
                            //Log.d("TEL",line);
                            telephoneResults.add(line);
//                            if (!telephoneResults.contains(line)) {
//                                telephoneResults.add(line);
//                            }
                        } else {
                            Log.d("line", line);
                        }
                        // Display each line in Logcat
                    }
                    readertext.close();
                    reader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        download.setOnClickListener(view -> {
            //String state = Environment.getExternalStorageState();
//            if (!Environment.MEDIA_MOUNTED.equals(state)) {
//                return; // External storage not available
//            }
            for (Object result : translationResults) {
                Log.d("Translation", "Translated Result: " + result.toString());
            }
            for (Object result : telephoneResults) {
                Log.d("Telephone", "Telephone Result: " + result.toString());
            }

            try {
                File vcfFile = new File(getFilesDir(), "GujaratiContacts.vcf");
//                File vcfFile = new File(this.getFilesDir(), "GujaratiContacts.txt");
                FileWriter writer = new FileWriter(vcfFile);
                for (int i = 0; i < translationResults.size(); i++) {
                    // Write contact information in VCF format
                    writer.write("BEGIN:VCARD\n");
                    writer.write("VERSION:2.1\n");
                    writer.write("FN:" + translationResults.get(i) + "\n");
                    writer.write(telephoneResults.get(i) + "\n");
                    writer.write("END:VCARD\n");
                    //Toast.makeText(this, telephoneResults.get(i), Toast.LENGTH_SHORT).show();
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("TotalName : ", "" + translationResults.size());
            Log.d("Total Number : ", "" + telephoneResults.size());

        });
    }

    public void downloadmodule() {
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        englishGujaratiTranslator.downloadModelIfNeeded(conditions).addOnSuccessListener(
                new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        objectdownload = o;
                        Toast.makeText(MainActivity.this, "Model is downloaded " + o, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Downloading fail", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public interface TranslationCallback {
        void onTranslationComplete(Object result);
    }

    public void translator(String input, final TranslationCallback callback) {
        englishGujaratiTranslator.translate(input).addOnSuccessListener(
                new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        if (o != null) {
                            //Log.d("Translate", o.toString());
//                                    if (!translationResults.contains(o)){
//                                        translationResults.add(o);
//                                    }
                            translationResults.add(o.toString());

                            callback.onTranslationComplete(o);
                            //Toast.makeText(MainActivity.this, "Translation is " + o.toString(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Translation result is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error in translation", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                // Read the content of the VCF file and display it in Logcat
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    //displayVcfData(uri);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

//    private void displayVcfData(Uri uri) {
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(uri);
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                //Log.d("VCF_DATA", line);
//                if(line.startsWith("FN")){
//                    Log.d("FN",line.substring(3));
//
//                } else if(line.startsWith("TEL")) {
//                    Log.d("TEL",line.substring(9));
//                } else{
//                    Log.d( "line",line);
//                }
//                // Display each line in Logcat
//            }
//            reader.close();

//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
