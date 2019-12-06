package com.londonappbrewery.climapm;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangeCityController extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_city_layout);

        ImageButton backButton = findViewById(R.id.backButton);
        EditText editCity = findViewById(R.id.queryET);

        backButton.setOnClickListener(v -> {
            finish();
        });

        editCity.setOnEditorActionListener((textView, i, keyEvent) -> {
            String city = editCity.getText().toString();
            Intent intent = new Intent(ChangeCityController.this, WeatherController.class);

            intent.putExtra("city", city);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        });
    }
}
