package com.example.newsplug;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsplug.ui.common.HeadlinesList;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static Context context;
    private JsonObjectRequest request;
    private RequestQueue requestQueue;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_sections, R.id.navigation_trends, R.id.navigation_bookmarks)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        SharedPreferences pref = MainActivity.getContext().getSharedPreferences("news", 0);
        SharedPreferences.Editor editor = pref.edit();
        String Name = "bookmarks";
        List<HeadlinesList> bookmarks = new ArrayList<>();
        Gson gson = new Gson();
        if (!pref.contains(Name)) {
            String bookmarked_json = gson.toJson(bookmarks);
            editor.putString("bookmarks", bookmarked_json);
            editor.commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        MenuItem searchItem = menu.findItem(R.id.search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final ComponentName componentName = new ComponentName(this, SearchableActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

//         Get SearchView autocomplete object.
        final SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setDropDownAnchor(R.id.search);
        searchAutoComplete.setThreshold(3);

        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String query = (String) adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText("" + query);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
            }
        });

        // Below event is triggered when submit search query.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() < 2){
                    ArrayList searchItems = new ArrayList();
                    searchItems.add("");
                    ArrayAdapter newsAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, searchItems);
                    searchAutoComplete.setAdapter(newsAdapter);
                } else {
                    String URL = "https://api.cognitive.microsoft.com/bing/v7.0/suggestions?q=" + newText;
                    request = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject res) {
                            JSONObject jsonObject;
                            try {
                                JSONArray response = res.getJSONArray("suggestionGroups").getJSONObject(0).getJSONArray("searchSuggestions");

                                ArrayList searchItems = new ArrayList();
                                for (int i = 0; i < response.length() && i < 5; i++) {
                                    jsonObject = response.getJSONObject(i);
                                    searchItems.add(jsonObject.getString("displayText"));
                                }
                                ArrayAdapter newsAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, searchItems);
                                searchAutoComplete.setAdapter(newsAdapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error);
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Ocp-Apim-Subscription-Key", "90ec36b3c34c49d6993e17b0deb1b799");
                            return params;
                        }
                    };
                    request.setRetryPolicy(new DefaultRetryPolicy(
                            50000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue = Volley.newRequestQueue(MainActivity.this);
                    requestQueue.add(request);
                }
                return false;
            }
        });
        return true;
    }

}
