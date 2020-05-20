package com.example.newsplug.ui.sections;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TabFragment extends Fragment {
    int position;
    private RecyclerView recyclerView;
    private List<HeadlinesList> seclist;

    private JsonObjectRequest request;
    private RequestQueue requestQueue;
    private RecyclerAdapter myAdapter;
    private RelativeLayout spinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        TabFragment tabFragment = new TabFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_headlines_tabs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinner = view.findViewById(R.id.progressBar_sec);
        spinner.setVisibility(View.VISIBLE);

        recyclerView = view.findViewById(R.id.sectional_recycler_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(60);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        seclist = new ArrayList<>();
        getData();

        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items_sec);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                seclist = new ArrayList<>();
                getData();
            }
        });

    }

    private void getData() {
        String URL = "http://ec2-54-197-200-149.compute-1.amazonaws.com:7000/api/guardian/section?section=";
        switch (position) {
            case 0:
                URL += "world";
                break;
            case 1:
                URL += "business";
                break;
            case 2:
                URL += "politics";
                break;
            case 3:
                URL += "sport";
                break;
            case 4:
                URL += "technology";
                break;
            case 5:
                URL += "science";
                break;
        }
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
                        seclist.add(listData);
                    }
                    if(mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setupData(seclist);

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
        myAdapter = new RecyclerAdapter(list_data, getContext());
        recyclerView.setAdapter(myAdapter);
        spinner.setVisibility(View.GONE);
    }
}