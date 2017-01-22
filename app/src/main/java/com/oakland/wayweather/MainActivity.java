package com.oakland.wayweather;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button b1;
    EditText et1,et2;
    private static final String LOG_TAG = "Google Places Autocomplete";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";

    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";

    private static final String OUT_JSON = "/json";



    private static final String API_KEY = "AIzaSyB6Vh-asFrNd-koDEWTH1dlMuAhhlP_piI";

    LatLng start;
    LatLng Destination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1=(Button)findViewById(R.id.button);


        final AutoCompleteTextView autoCompView1 = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        final AutoCompleteTextView autoCompView2 = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);



        autoCompView1.setAdapter(new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_list_item_1));
        autoCompView2.setAdapter(new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_list_item_1));

        autoCompView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

               start= getLocationFromAddress(autoCompView1.getText().toString());

            }
        });

        autoCompView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

              Destination=  getLocationFromAddress(autoCompView2.getText().toString());


            }
        });




        b1.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent in=new Intent(MainActivity.this,MapsActivity.class);
        in.putExtra("Start",start);
        in.putExtra("Destination",Destination);
        startActivity(in);
    }
});
    }

    public static ArrayList autocomplete(String input) {

        ArrayList resultList = null;


        HttpURLConnection conn = null;

        StringBuilder jsonResults = new StringBuilder();

        try {

            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);

            sb.append("?key=" + API_KEY);

            sb.append("&components=country:us");

            sb.append("&input=" + URLEncoder.encode(input, "utf8"));


            URL url = new URL(sb.toString());

            conn = (HttpURLConnection) url.openConnection();

            InputStreamReader in = new InputStreamReader(conn.getInputStream());


            // Load the results into a StringBuilder

            int read;

            char[] buff = new char[1024];

            while ((read = in.read(buff)) != -1) {

                jsonResults.append(buff, 0, read);

            }

        } catch (MalformedURLException e) {

            Log.e("", "Error processing Places API URL", e);

            return resultList;

        } catch (IOException e) {

            Log.e("", "Error connecting to Places API", e);

            return resultList;

        } finally {

            if (conn != null) {

                conn.disconnect();

            }

        }
        try {

            // Create a JSON object hierarchy from the results

            JSONObject jsonObj = new JSONObject(jsonResults.toString());

            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");


            // Extract the Place descriptions from the results

            resultList = new ArrayList(predsJsonArray.length());

            for (int i = 0; i < predsJsonArray.length(); i++) {

                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));

                System.out.println("============================================================");

                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));

            }

        } catch (JSONException e) {

            Log.e("", "Cannot process JSON results", e);

        }
        return resultList;

    }


    public LatLng getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();
            p1=new LatLng(location.getLatitude(),location.getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return p1;

    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {

            private ArrayList<String> resultList;


            public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {

                super(context, textViewResourceId);

            }


            @Override

            public int getCount() {

                return resultList.size();

            }


            @Override

            public String getItem(int index) {

                return resultList.get(index);

            }


            @Override

            public Filter getFilter() {

                Filter filter = new Filter() {

                    @Override

                    protected FilterResults performFiltering(CharSequence constraint) {

                        FilterResults filterResults = new FilterResults();

                        if (constraint != null) {

                            // Retrieve the autocomplete results.

                            resultList = autocomplete(constraint.toString());


                            // Assign the data to the FilterResults

                            filterResults.values = resultList;

                            filterResults.count = resultList.size();

                        }

                        return filterResults;

                    }


                    @Override

                    protected void publishResults(CharSequence constraint, FilterResults results) {

                        if (results != null && results.count > 0) {

                            notifyDataSetChanged();

                        } else {

                            notifyDataSetInvalidated();

                        }

                    }

                };

                return filter;

            }

        }

    }
