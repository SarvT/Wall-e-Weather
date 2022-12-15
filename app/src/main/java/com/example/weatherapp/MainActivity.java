package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeLayout;
    private ProgressBar loadingBar;
    private TextView tempTxt, locationTxt, titleTxt;
    private ImageView searchIcon, weatherIcon, bgIcon;
    private TextInputEditText locationInp;
    private RecyclerView recyclerView;
    private ArrayList<WeatherModel> weatherModelArrayList;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private int PERMIT_CODE = 1;
    private String cityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //used for fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(LinearLayoutManager);
        homeLayout = findViewById(R.id.homeLayout);
        loadingBar = findViewById(R.id.progressBar);
        tempTxt = findViewById(R.id.tempTxt);
        locationTxt = findViewById(R.id.locationTxt);
        titleTxt = findViewById(R.id.titleTW);
        locationInp = findViewById(R.id.idTIETCity);
        bgIcon = findViewById(R.id.idIVBlack);
        weatherIcon = findViewById(R.id.idVIcon);
        searchIcon = findViewById(R.id.searchIcon);
        weatherModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModelArrayList);
        recyclerView.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMIT_CODE);
        }
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
//        cityName = getCityName(location.getLongitude(), location.getLatitude());
//
//        getWeatherInfo(cityName);

        if (location != null){cityName = getCityName(location.getLongitude(),location.getLatitude());
            getWeatherInfo(cityName);
        } else {
            cityName = "London";
            getWeatherInfo(cityName);
        }

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = locationInp.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter the city name please!", Toast.LENGTH_LONG).show();
                }else{
                    locationTxt.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMIT_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_LONG).show();
                finish(); //closing the application.
            }
        }
    }

    private String getCityName(double longitude, double lattitude){
        String cityName = "Not found!";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lattitude, longitude, 10);

            for (Address address: addresses){
                if (address!=null){
                    String city = address.getLocality();
                    if (city!=null && city!="")
                        cityName = city;
                    else {
                        Log.d("TAG", "City Not Found!");
                        Toast.makeText(this, "Entered city is not found!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=bb6303fd66aa4191b73152317223005&q=Mumbai&days=1&aqi=no&alerts=no";
        locationTxt.setText(cityName);
        RequestQueue requestQueue =  Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingBar.setVisibility(View.GONE);
                homeLayout.setVisibility(View.VISIBLE);
                weatherModelArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    tempTxt.setText(temperature+"°c");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Glide.with(MainActivity.this).load("http:".concat(conditionIcon)).into(weatherIcon);
                    titleTxt.setText(condition);
                    if (isDay==1){
                        Glide.with(MainActivity.this).load("https://unsplash.com/photos/VeGfXKMrDZk").into(bgIcon);
                    }else{
                        Glide.with(MainActivity.this).load("https://unsplash.com/photos/VeGfXKMrDZk").into(bgIcon);
                    }
                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecast0 = response.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArr = forecast0.getJSONArray("hour");

                    for (int i=0; i<hourArr.length(); i++){
                        JSONObject hourObj = hourArr.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherModelArrayList.add(new WeatherModel(time, temp, img, wind));
                    }
                    weatherAdapter.notifyDataSetChanged();

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Try again!", Toast.LENGTH_LONG).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}