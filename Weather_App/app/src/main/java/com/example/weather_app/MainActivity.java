package com.example.weather_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcel;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    //Variables
    Animation topAnim, bottomAnim, bottomAnimSlow, text_Anim;
    TextView logo, temp, weather_description, location;
    ImageView footer_left, footer_right, weather, degrees;
    EditText user_input_city;
    ImageButton imageButton;
    LocationRequest locationRequest;

    double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        bottomAnimSlow = AnimationUtils.loadAnimation(this, R.anim.bottom_animation_slow);
        text_Anim = AnimationUtils.loadAnimation(this, R.anim.text_animation);


        //Weather description
        weather = findViewById(R.id.weather);
        temp = findViewById(R.id.temp);
        weather_description = findViewById(R.id.description);
        location = findViewById(R.id.location);
        degrees = findViewById(R.id.degrees);


        //Setting the animation
        logo = findViewById(R.id.logo);
        logo.setAnimation(topAnim);

        footer_left = findViewById(R.id.footer_left);
        footer_left.setAnimation(bottomAnim);
        footer_right = findViewById(R.id.footer_right);
        footer_right.setAnimation(bottomAnimSlow);

        weather.setAnimation(text_Anim);
        temp.setAnimation(text_Anim);
        weather_description.setAnimation(text_Anim);
        location.setAnimation(text_Anim);
        degrees.setAnimation(text_Anim);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(isGPSEnabled()){
                    LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                    .removeLocationUpdates(this);

                            if (locationResult != null && locationResult.getLocations().size() > 0){
                                int index = locationResult.getLocations().size();
                                latitude = locationResult.getLocations().get(index).getLatitude();
                                longitude = locationResult.getLocations().get(index).getLongitude();
                            }
                        }
                    }, Looper.getMainLooper());
                }
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        String key = "79602c917a9856ef8684073b4cfe2b79";
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + key + "&units=metric";
        new GetURLData().execute(url);
        location.setText("Current location");

        user_input_city = findViewById(R.id.user_input_city);
        imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_input_city.getText().toString().trim().equals("")){
                    //Toast.makeText(MainActivity.this, R.string.no_user_input, Toast.LENGTH_LONG).show();
                }else{
                    String city = user_input_city.getText().toString();
                    String Url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric";
                    location.setText(city);
                    new GetURLData().execute(Url);
                }
            }
        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if(locationManager == null){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }

    private class GetURLData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpsURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                }

                return buffer.toString();
            } catch (MalformedURLException e) {
                Toast.makeText(MainActivity.this, "Seems that city doesn't exist", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();

                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                temp.setText("" + jsonObject.getJSONObject("main").getDouble("temp"));

                JSONArray mass = jsonObject.getJSONArray("weather");
                String current_weather = null;
                for (int i = 0; i < mass.length(); i++) {
                    JSONObject inst = mass.getJSONObject(i);
                    current_weather = inst.getString("description").trim();
                }
                String desc;
                desc = "" + current_weather.substring(0, 1).toUpperCase(Locale.ROOT) + current_weather.substring(1).toLowerCase(Locale.ROOT);
                weather_description.setText(desc);
                if (current_weather.equals("clear sky")) weather.setImageResource(R.drawable.clear_sky);
                if (current_weather.equals("few clouds")) weather.setImageResource(R.drawable.few_clouds);
                if (current_weather.equals("scattered clouds") |
                        (current_weather.equals("broken clouds")) |
                        (current_weather.equals("few clouds")) |
                        (current_weather.equals("overcast clouds"))) weather.setImageResource(R.drawable.scattered_clouds);
                if (current_weather.equals("shower rain") |
                        current_weather.equals("light intensity drizzle") |
                        current_weather.equals("drizzle") |
                        current_weather.equals("heavy intensity drizzle") |
                        current_weather.equals("light intensity drizzle rain") |
                        current_weather.equals("drizzle rain") |
                        current_weather.equals("heavy intensity drizzle rain") |
                        current_weather.equals("shower rain and drizzle") |
                        current_weather.equals("heavy shower rain and drizzle") |
                        current_weather.equals("shower drizzle")) weather.setImageResource(R.drawable.drizzle);
                if (current_weather.equals("rain") |
                        current_weather.equals("light rain") |
                        current_weather.equals("moderate rain") |
                        current_weather.equals("heavy intensity rain") |
                        current_weather.equals("very heavy rain") |
                        current_weather.equals("extreme rain") |
                        current_weather.equals("freezing rain") |
                        current_weather.equals("light intensity shower rain") |
                        current_weather.equals("heavy intensity shower rain") |
                        current_weather.equals("ragged shower rain")) weather.setImageResource(R.drawable.drizzle_alt_sun);
                if (current_weather.equals("thunderstorm") |
                        current_weather.equals("thunderstorm with light rain") |
                        current_weather.equals("thunderstorm with rain") |
                        current_weather.equals("thunderstorm with heavy rain") |
                        current_weather.equals("light thunderstorm") |
                        current_weather.equals("heavy thunderstorm") |
                        current_weather.equals("ragged thunderstorm") |
                        current_weather.equals("thunderstorm with light drizzle") |
                        current_weather.equals("thunderstorm with drizzle") |
                        current_weather.equals("thunderstorm with heavy drizzle")) weather.setImageResource(R.drawable.lightning_rain);
                if (current_weather.equals("snow") |
                        current_weather.equals("light snow") |
                        current_weather.equals("Heavy snow") |
                        current_weather.equals("Sleet") |
                        current_weather.equals("Light shower sleet") |
                        current_weather.equals("Shower sleet") |
                        current_weather.equals("Light rain and snow") |
                        current_weather.equals("Rain and snow") |
                        current_weather.equals("Light shower snow") |
                        current_weather.equals("Shower snow") |
                        current_weather.equals("Heavy shower snow")) weather.setImageResource(R.drawable.snow_alt);
                if (current_weather.equals("mist") |
                        current_weather.equals("Smoke") |
                        current_weather.equals("Haze") |
                        current_weather.equals("sand/ dust whirls") |
                        current_weather.equals("fog") |
                        current_weather.equals("sand") |
                        current_weather.equals("dust") |
                        current_weather.equals("volcanic ash") |
                        current_weather.equals("squalls") |
                        current_weather.equals("tornado")) weather.setImageResource(R.drawable.fog_sun);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}