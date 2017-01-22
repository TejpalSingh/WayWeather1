package com.oakland.wayweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<LatLng> points; //added
    Polyline line;
    LatLng Start;
    LatLng Destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        points = new ArrayList<LatLng>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private String  getMapsApiDirectionsUrl(LatLng origin,LatLng dest) {
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;


        return url;

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        Bundle data= getIntent().getExtras();
        Start=(LatLng) data.get("Start");//new LatLng(42.673089, -83.218549);
        // Destination=new LatLng(42.727617, -84.481793);
        Destination=(LatLng) data.get("Destination");//new LatLng(38.590230, -80.974914);
        if (location != null)
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Start, 13));

          //  CameraPosition cameraPosition = new CameraPosition.Builder()
           //         .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
            //        .zoom(12)                   // Sets the zoom
           //         .bearing(90)                // Sets the orientation of the camera to east
            //        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
            //        .build();                   // Creates a CameraPosition from the builder
            //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


        String url = getMapsApiDirectionsUrl(Start, Destination);
        ReadTask downloadTask = new ReadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);

        // Add a marker in Sydney and move the camera
      //  LatLng sydney = new LatLng(-34, 151);
       // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    private class ReadTask extends AsyncTask<String, Void , String> {

        @Override
        protected String doInBackground(String... url) {
            // TODO Auto-generated method stub
            String data = "";
            try {
                MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);


            } catch (Exception e) {
                // TODO: handle exception
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }

    }
    private class getweatherdata extends AsyncTask<String, Void , String> {

        String lat;
        String lng;
        String time;
        @Override
        protected String doInBackground(String... url) {
            // TODO Auto-generated method stub
            lat=url[1];
            lng=url[2];
            time=url[3];
            String data = "";
            try {
                MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);


            } catch (Exception e) {
                // TODO: handle exception
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject object_to_use=new JSONObject();
            long unixTime = System.currentTimeMillis() / 1000L;
            unixTime=unixTime+Integer.parseInt(time)*60;
            try {
                JSONObject obj=new JSONObject(result);
                JSONObject obj2= (JSONObject) obj.get("hourly");
               JSONArray jarr= obj2.getJSONArray("data");
                for (int i=0;i<jarr.length();i++)
                {

                    JSONObject obj3= (JSONObject) jarr.get(i);
                  Long ltime=  obj3.getLong("time");

                    if(unixTime>ltime && unixTime<=ltime+1600)
                    {
                       object_to_use=obj3;
                    }
                   else if (unixTime>ltime+1600 && unixTime<=ltime+3200)
                    {
                        object_to_use=  (JSONObject) jarr.get(i+1);
                    }
                    Date date = new Date(ltime*1000L);
                    Date date2 = new Date(unixTime*1000L);
                  //  Log.e("API time",date.toString());
                   // Log.e("System Time",date2.toString()+"");


                }
                Log.e("Final Summary for point",object_to_use.getString("summary"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
           // Log.e("Weather data",result);
            try {
            String icon=    object_to_use.getString("icon");

                if(icon.equalsIgnoreCase("partly-cloudy-day"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.cloudy_day)));}


               else if(icon.equalsIgnoreCase("partly-cloudy-night"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.nightcloud)));}


                else if(icon.equalsIgnoreCase("clear-day"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.sunny)));}


                else if(icon.equalsIgnoreCase("clear-night"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.moon)));}


                else if(icon.equalsIgnoreCase("rain"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.umbrellas)));}


                else if(icon.equalsIgnoreCase("snow"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.snowflake)));}

                else if(icon.equalsIgnoreCase("sleet"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.sleet)));}

                else if(icon.equalsIgnoreCase("wind"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.wind)));}

                else if(icon.equalsIgnoreCase("fog"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.fog)));}

                else if(icon.equalsIgnoreCase("cloudy"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.cloudy)));}

                else if(icon.equalsIgnoreCase("thunderstorm"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.thunderstorm)));}

                else if(icon.equalsIgnoreCase("tornado"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.tornado)));}

                else if(icon.equalsIgnoreCase("hail"))
                { mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng))).title(icon).icon(BitmapDescriptorFactory.fromResource(R.drawable.hail)));}

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

    }
    public class MapHttpConnection {
        public String readUr(String mapsApiDirectionsUrl) throws IOException{
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line ="";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();


            }
            catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;

        }
    }
    private class ParserTask extends AsyncTask<String,Integer, List<List<HashMap<String , String >>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            // TODO Auto-generated method stub
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(4);
                polyLineOptions.color(Color.BLUE);
            }

            for (Waypoint points1:PathJSONParser.steps)
            {


                getweatherdata downloadTask = new getweatherdata();
                // Start downloading json data from Google Directions API
                downloadTask.execute("https://api.darksky.net/forecast/38d872cfb268fd0a1b6e269ab6f58848/"+points1.getLatitude()+","+points1.getLongitude(),points1.getLatitude()+"",points1.getLongitude()+"",points1.getTime()+"");


            }
            mMap.addPolyline(polyLineOptions);
            mMap.addMarker(new MarkerOptions().position(Start));
            mMap.addMarker(new MarkerOptions().position(Destination));

        }}


}
