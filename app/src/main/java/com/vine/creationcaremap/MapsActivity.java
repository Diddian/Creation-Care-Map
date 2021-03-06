package com.vine.creationcaremap;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
    private int markerBackground;

    public GoogleMap getMap() {
        return initialMap;
    }
    private LatLngBounds HONGKONG = new LatLngBounds(
//            new LatLng(22.35,114), new LatLng(22.35,114.22));
       new LatLng(22.18,113.9), new LatLng(22.51,114.3));

    private final static String mLogTag = "GeoJsonDemo";

    private void retrieveFileFromUrl() {
        new DownloadGeoJsonFile().execute(getString(R.string.geojson_url));
    }

    /**
     * Assigns a color marker based on the given shop type
     */
    private static @DrawableRes int shopMarkerColour(String shopType) {
        if (shopType.contentEquals("Bakery")) {
            return R.drawable.ic_eph_map_brown;
        } else if (shopType.contentEquals("Butcher & Deli")) {
            return R.drawable.ic_eph_map_yellow;
        } else if (shopType.contentEquals("Food & Drink (Eco-Friendly / Bulk)")) {
            return R.drawable.ic_eph_map_leaf;
        } else if (shopType.contentEquals("Bath & Beauty (Eco-Friendly / Bulk)")) {
            return R.drawable.ic_eph_map_purple;
        } else if (shopType.contentEquals("Fashion")) {
            return R.drawable.ic_eph_map_red;
        } else if (shopType.contentEquals("Market")) {
            return R.drawable.ic_eph_map_darkgreen;
        } else {
            return R.drawable.ic_eph_map_grey;
        }
    }
    /**
     * Assigns an icon based on the given shop type
     */
    private static @DrawableRes int shopIcon(String shopType) {
        if (shopType.contentEquals("Bakery")) {
            return R.drawable.ic_eph_icon_wheat;
        } else if (shopType.contentEquals("Butcher & Deli")) {
            return R.drawable.ic_eph_icon_cow;
        } else if (shopType.contentEquals("Food & Drink (Eco-Friendly / Bulk)")) {
            return R.drawable.ic_eph_icon_restaurant;
        } else if (shopType.contentEquals("Bath & Beauty (Eco-Friendly / Bulk)")) {
            return R.drawable.ic_eph_icon_makeup;
        } else if (shopType.contentEquals("Fashion")) {
            return R.drawable.ic_eph_icon_dress;
        } else if (shopType.contentEquals("Market")) {
            return R.drawable.ic_eph_icon_store;
        } else {
            return R.drawable.ic_eph_icon_empty;
        }
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {


        Drawable background = ContextCompat.getDrawable(context, markerBackground);

        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(38, 27, vectorDrawable.getIntrinsicWidth() + 35, vectorDrawable.getIntrinsicHeight() + 16);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
            if (feature.getProperty("name") != null && feature.hasProperty("shop-type")) {
               String shopTypeName = feature.getProperty("shop-type");

                // Get the icon for the feature
//                BitmapDescriptor pointIcon = BitmapDescriptorFactory.defaultMarker(shopColour(shopTypeName));
                markerBackground = shopMarkerColour(shopTypeName);
                BitmapDescriptor pointIcon = bitmapDescriptorFromVector(this, shopIcon(shopTypeName));

                // Create a new point style
                GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();

                // Set options for the point style
                pointStyle.setIcon(pointIcon);
                pointStyle.setTitle(feature.getProperty("name"));
                pointStyle.setSnippet(feature.getProperty("description"));

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
                        feature.getProperty("shop-type"),
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
        initialMap.getUiSettings().setZoomControlsEnabled(false);
        initialMap.getUiSettings().setAllGesturesEnabled(true);
        initialMap.setMinZoomPreference(11.5f);
        initialMap.setLatLngBoundsForCameraTarget(HONGKONG);
        LatLng HongKong = HONGKONG.getCenter();
        initialMap.moveCamera(CameraUpdateFactory.newLatLng(HongKong));

//        Toast.makeText(MapsActivity.this,
//                Boolean.toString(initialMap.getUiSettings().isMapToolbarEnabled()),
//                Toast.LENGTH_SHORT).show();







    }

}
