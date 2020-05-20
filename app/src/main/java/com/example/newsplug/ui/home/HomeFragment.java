package com.example.newsplug.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsplug.R;
import com.example.newsplug.ui.common.HeadlinesList;
import com.example.newsplug.ui.search.SearchableAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String URL = "http://ec2-54-197-200-149.compute-1.amazonaws.com:7000/api/guardian/home";
    String provider;
    LocationManager locationManager;
    private RecyclerView recyclerView;
    private List<HeadlinesList> hdlist;
    private SearchableAdapter myAdapter;
    private JsonObjectRequest request, requestLocation;
    private RequestQueue requestQueue;
    private String current_city, current_state;
    View view;
    private RelativeLayout spinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Criteria criteria;

    public HomeFragment() {
    }

    public boolean getLocationPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            onLocationChanged(location);
            return true;
        }
    }

    public void onLocationChanged(Location location) {
        Geocoder geocoder;
        List<Address> user = null;
        double lat;
        double lng;
        if (location == null){
            Toast.makeText(getContext(),"Location Not found",Toast.LENGTH_LONG).show();
        }else{
            geocoder = new Geocoder(getContext());
            try {
                user = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                lat=(double)user.get(0).getLatitude();
                lng=(double)user.get(0).getLongitude();
                System.out.println(" DDD lat: " +lat+",  longitude: "+lng);
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(lat, lng, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                current_city = addresses.get(0).getLocality();
                current_state = addresses.get(0).getAdminArea();
                getLocationdata();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        recyclerView = view.findViewById(R.id.my_recycler_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        hdlist = new ArrayList<>();
        getData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                        criteria = new Criteria();
                        provider = locationManager.getBestProvider(criteria, false);
                        Location location = locationManager.getLastKnownLocation(provider);
                        onLocationChanged(location);

                    }

                } else {
                }
                return;
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        spinner = view.findViewById(R.id.progressBar_home);
        spinner.setVisibility(View.VISIBLE);
        getLocationPermissions();

        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items_home);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLocationPermissions();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    private void getLocationdata() {
        String API = "https://api.openweathermap.org/data/2.5/weather?q=" + current_city + "&units=metric&appid=696b67907527288cb24e1bbd81b8cb82";
        requestLocation = new JsonObjectRequest(Request.Method.GET, API, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject res) {
                try {
                    JSONObject main = res.getJSONObject("main");
                    JSONObject weather = (JSONObject) res.getJSONArray("weather").get(0);
                    int bgImage;
                    switch (weather.getString("main")) {
                        case "Clouds":
                            bgImage = R.drawable.cloudy_weather;
                            break;
                        case "Clear":
                            bgImage = R.drawable.clear_weather;
                            break;
                        case "Snow":
                            bgImage = R.drawable.snowy_weather;
                            break;
                        case "Rain":
                        case "Drizzle":
                            bgImage = R.drawable.rainy_weather;
                            break;
                        case "Thunderstorm":
                            bgImage = R.drawable.thunder_weather;
                            break;
                        default:
                            bgImage = R.drawable.sunny_weather;
                    }

                    LinearLayout img;
                    TextView city, temp, state, summary;
                    img = view.findViewById(R.id.weather_card);
                    img.setBackgroundResource(bgImage);

                    city = view.findViewById(R.id.city);
                    city.setText(current_city);
                    city.setTextColor(Color.WHITE);

                    temp = view.findViewById(R.id.temperature);
                    temp.setText(Math.round(Float.parseFloat(main.getString("temp"))) + " \u2103");
                    temp.setTextColor(Color.WHITE);

                    state = view.findViewById(R.id.state);
                    state.setText(current_state);
                    state.setTextColor(Color.WHITE);

                    summary = view.findViewById(R.id.type);
                    summary.setText(weather.getString("main"));
                    summary.setTextColor(Color.WHITE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        requestLocation.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(requestLocation);
    }

    private void getData() {
        request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject res) {
                JSONObject jsonObject;
                try {
                    JSONArray response = res.getJSONArray("response");
                    for (int i = 0; i < response.length(); i++) {
                        jsonObject = response.getJSONObject(i);

                        HeadlinesList listData = new HeadlinesList(jsonObject.getString("ID"),
                                jsonObject.getString("Image"),
                                jsonObject.getString("Title"),
                                jsonObject.getString("Date"),
                                jsonObject.getString("Section_Name"),
                                jsonObject.getString("Link"));
                        hdlist.add(listData);
                    }
                    if(mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setupData(hdlist);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (myAdapter != null) {
            myAdapter.notifyDataSetChanged();
        }
    }

    private void setupData(List<HeadlinesList> list_data) {
        myAdapter = new SearchableAdapter(list_data, getContext());
        recyclerView.setAdapter(myAdapter);
        spinner.setVisibility(View.GONE);
    }
}


