package com.vine.creationcaremap;

import androidx.fragment.app.FragmentActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap initialMap;
    public GoogleMap getMap() {
        return initialMap;
    }
    private LatLngBounds HONGKONG = new LatLngBounds(
//            new LatLng(22.35,114), new LatLng(22.35,114.22));
       new LatLng(22.3,114), new LatLng(22.4,114.3));

    private final static String mLogTag = "GeoJsonDemo";

    private void retrieveFileFromUrl() {
        new DownloadGeoJsonFile().execute(getString(R.string.geojson_url));
    }

    /**
     * Assigns a color based on the given shop type
     */
    private static float category(String shopType) {
        if (shopType == "Bakery") {
            return BitmapDescriptorFactory.HUE_VIOLET;
        } else if (shopType == "Butcher & Deli") {
            return BitmapDescriptorFactory.HUE_ORANGE;
        } else if (shopType == "Food & Drink (Eco-Friendly / Bulk)") {
            return BitmapDescriptorFactory.HUE_GREEN;
        } else if (shopType == "Bath & Beauty (Eco-Friendly / Bulk)") {
            return BitmapDescriptorFactory.HUE_BLUE;
        } else if (shopType == "Fashion") {
            return BitmapDescriptorFactory.HUE_YELLOW;
        } else if (shopType == "Market") {
            return BitmapDescriptorFactory.HUE_MAGENTA;
        } else {
            return BitmapDescriptorFactory.HUE_AZURE;
        }
    }

    private class DownloadGeoJsonFile extends AsyncTask<String, Void, GeoJsonLayer> {

        @Override
        protected GeoJsonLayer doInBackground(String... params) {
            try {
                // Open a stream from the URL
                InputStream stream = new URL(params[0]).openStream();

                String line;
                StringBuilder result = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    // Read and save each line of the stream
                    result.append(line);
                }

                // Close the stream
                reader.close();
                stream.close();

                return new GeoJsonLayer(getMap(), new JSONObject(result.toString()));
            } catch (IOException e) {
                Log.e(mLogTag, "GeoJSON file could not be read");
            } catch (JSONException e) {
                Log.e(mLogTag, "GeoJSON file could not be converted to a JSONObject");
            }
            return null;
        }

        @Override
        protected void onPostExecute(GeoJsonLayer layer) {
            if (layer != null) {
                addGeoJsonLayerToMap(layer);
            }
        }
    }

    /**
     * Adds a point style to all features to change the color of the marker based on its magnitude
     * property
     */
    private void addColorsToMarkers(GeoJsonLayer layer) {
        // Iterate over all the features stored in the layer
        for (GeoJsonFeature feature : layer.getFeatures()) {
            // Check if the magnitude property exists
            if (feature.getProperty("shop-type") != null && feature.hasProperty("title")) {
               String shopType = feature.getProperty("shop-type");

                // Get the icon for the feature
                BitmapDescriptor pointIcon = BitmapDescriptorFactory
                        .defaultMarker(category(shopType));

                // Create a new point style
                GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();

                // Set options for the point style
                pointStyle.setIcon(pointIcon);
                pointStyle.setTitle(feature.getProperty("title"));
                pointStyle.setSnippet(feature.getProperty("description") + feature.getProperty("place"));

                // Assign the point style to the feature
                feature.setPointStyle(pointStyle);
            }
        }
    }


    private void addGeoJsonLayerToMap(GeoJsonLayer layer) {

        addColorsToMarkers(layer);
        layer.addLayerToMap();
        // Demonstrate receiving features via GeoJsonLayer clicks.
        layer.setOnFeatureClickListener(new GeoJsonLayer.GeoJsonOnFeatureClickListener() {
            @Override
            public void onFeatureClick(Feature feature) {
                Toast.makeText(MapsActivity.this,
                        feature.getProperty("name"),
                        Toast.LENGTH_SHORT).show();
            }

        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
    @Override
    public void onMapReady(GoogleMap Map) {
        retrieveFileFromUrl();
        initialMap = Map;
        initialMap.setMapType((GoogleMap.MAP_TYPE_NORMAL));
        initialMap.getUiSettings().setZoomControlsEnabled(true);
        initialMap.getUiSettings().setAllGesturesEnabled(true);
        initialMap.setMinZoomPreference(11.0f);
        initialMap.setLatLngBoundsForCameraTarget(HONGKONG);
        LatLng HongKong = HONGKONG.getCenter();
        initialMap.moveCamera(CameraUpdateFactory.newLatLng(HongKong));
        Toast.makeText(MapsActivity.this,
                Boolean.toString(initialMap.getUiSettings().isMapToolbarEnabled()),
                Toast.LENGTH_SHORT).show();







    }

}
