package com.oakland.wayweather;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by waheguru on 21/01/17.
 */

public class PathJSONParser {

    static List<Waypoint> steps;

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        int time=0;
        steps=new ArrayList<Waypoint>();

        try {
            jRoutes = jObject.getJSONArray("routes");
            for (int i=0 ; i < jRoutes.length() ; i ++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<>();
                for(int j = 0 ; j < jLegs.length() ; j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    for(int k = 0 ; k < jSteps.length() ; k ++) {
                        String polyline = "";
                        Waypoint point=new Waypoint();
                        JSONObject jobj=(JSONObject) jSteps.get(k);
                        JSONObject jobj2=(JSONObject) jobj.get("duration");
                        JSONObject jobj3=(JSONObject) jobj.get("end_location");
                        String timestr= jobj2.getString("text");
                        point.setLatitude(jobj3.getDouble("lat"));
                        point.setLongitude(jobj3.getDouble("lng"));
                        jobj3.getDouble("lng");
                        time=time+Integer.parseInt(timestr.split(" ")[0]);

                        point.setTime(time);
                        Log.e("time",time+" ");
                        steps.add(point);
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);
                        for(int l = 0 ; l < list.size() ; l ++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat",
                                    Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng",Double.toString(((LatLng) list.get(l)).longitude));

                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;

    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }}