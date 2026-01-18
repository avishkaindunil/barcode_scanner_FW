package com.example.barcode_scanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnBarcode).setOnClickListener(v ->
                openScanner("BARCODE"));

        findViewById(R.id.btnQR).setOnClickListener(v ->
                openScanner("QR"));
    }

    private void openScanner(String type) {
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra("SCAN_TYPE", type);
        startActivity(intent);
    }
}
