package com.example.android.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Eugen on 25-Jan-18.
 */


public class BarcodeActivity extends AppCompatActivity {

    private ZXingScannerView scannerView;
    private int BARCODE_RESULT_CODE = 1;
    private int BARCODE_RESULT_FAIL = -1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        scannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
            @Override
            public void handleResult(Result result) {
                String code = result.getText();
                Toast.makeText(getApplicationContext(), "Barcode result: " + code, Toast.LENGTH_LONG).show();
                scannerView.stopCamera();
                Intent data = new Intent();
                data.putExtra("CODE", code);
                setResult(BARCODE_RESULT_CODE, data);
                finish();
            }
        });
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void onBackPressed() {
        scannerView.stopCamera();
        setResult(BARCODE_RESULT_FAIL,new Intent());
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is the event fired when up button is pressed.
                setResult(BARCODE_RESULT_FAIL,new Intent());
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

