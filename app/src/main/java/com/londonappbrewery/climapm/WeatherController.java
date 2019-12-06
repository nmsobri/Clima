package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {
    private final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    private final String APP_ID = "219baf711885cbecc9b6aa9333c99d24";
    private final long MIN_TIME = 5000;
    private final float MIN_DISTANCE = 1000;
    private final String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private final int REQUEST_CODE = 123;
    private final int CITY_CODE = 456;

    private TextView cityLabel;
    private ImageView weatherImage;
    private TextView temperatureLbel;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean useLocation = true;
    private String city = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        cityLabel = findViewById(R.id.locationTV);
        weatherImage = findViewById(R.id.weatherSymbolIV);
        temperatureLbel = findViewById(R.id.tempTV);
        ImageButton changeCityButton = findViewById(R.id.changeCityButton);

        checkLocationPermission();

        changeCityButton.setOnClickListener(v -> {
            Intent intent = new Intent(WeatherController.this, ChangeCityController.class);
            startActivityForResult(intent, CITY_CODE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "onResume() called");

        if (this.useLocation) {
            getWeatherForCurrentLocation();
            Log.d("Clima", "Getting weather for current location");
        } else {
            getWeatherForNewCity(this.city);
            Log.d("Clima", "Getting weather new city");
        }

    }

    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }

    private void getWeatherForCurrentLocation() {
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        this.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() callback triggered");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d("Clima", "Longitude: " + longitude + " , Latitude: " + latitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("Clima", "onProviderDisabled() callback triggered");
                Toast.makeText(WeatherController.this, "You need to enable gps on your setting", Toast.LENGTH_SHORT).show();
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, this.locationListener);
        }
    }

    private void checkLocationPermission() {
        ActivityCompat.requestPermissions(WeatherController.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Clima", "onRequestPermissionResult() : permission granted");
                getWeatherForCurrentLocation();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(WeatherController.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("Clima", "Permission denied");
                this.reconfirmPermissionDialog();
            } else {
                new AlertDialog.Builder(WeatherController.this)
                        .setCancelable(false)
                        .setTitle("Permission denied")
                        .setMessage("This app need an access to your location")
                        .setNegativeButton("close", (dialog, which) -> finish())
                        .create().show();

                Toast.makeText(WeatherController.this, "Permission denied! This app need access to your location!", Toast.LENGTH_SHORT).show();
                Log.d("Clima", "Permission denied");
            }
        }
    }

    private void reconfirmPermissionDialog() {
        new AlertDialog.Builder(WeatherController.this)
                .setTitle("Permission required")
                .setMessage("This app need to access your location")
                .setPositiveButton("Ok", (dialog, which) -> {
                    ActivityCompat.requestPermissions(WeatherController.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(WeatherController.this, "Permission denied! This app need access to your location!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .create().show();
    }

    private void letsDoSomeNetworking(RequestParams params) {
        AsyncHttpClient http = new AsyncHttpClient();
        http.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Clima", "Request successful: " + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("Clima", "Fail " + errorResponse.toString());
                Log.e("Clima", "Code " + statusCode);
                Toast.makeText(WeatherController.this, "Request failed ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(WeatherDataModel weatherData) {
        int imageResouce = getResources().getIdentifier(weatherData.getIconName(), "drawable", getPackageName());

        weatherImage.setImageResource(imageResouce);
        cityLabel.setText(weatherData.getCity());
        temperatureLbel.setText(weatherData.getTemperate());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.locationManager != null) {
            this.locationManager.removeUpdates(this.locationListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("Clima", "onActivityResult() called");

        if (requestCode == CITY_CODE) {
            if (resultCode == RESULT_OK) {
                this.city = data.getStringExtra("city");
                Log.d("Clima", "City: " + this.city);
                this.locationManager.removeUpdates(this.locationListener);
                this.useLocation = false;
                this.getWeatherForNewCity(this.city);
            }
        }

    }
}
