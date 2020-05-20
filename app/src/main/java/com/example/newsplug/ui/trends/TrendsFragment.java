package com.example.newsplug.ui.trends;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsplug.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrendsFragment extends Fragment {

    LineDataSet dataSet;
    LineData lineData;
    LineChart chart;
    private JsonObjectRequest request;
    private RequestQueue requestQueue;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trends, container, false);
        chart = root.findViewById(R.id.chart_trends);
        getData(null);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final EditText trendQuery = view.findViewById(R.id.search_trends);
        trendQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    getData(trendQuery.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    public void getData(String query) {
        if (query == null || query.isEmpty()) {
            query = "Coronavirus";
        }
        String API = "http://ec2-54-197-200-149.compute-1.amazonaws.com:7000/api/google/trends?q=" + query;
        final String finalQuery = query;
        request = new JsonObjectRequest(Request.Method.GET, API, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject res) {
                try {
                    JSONArray main = res.getJSONArray("main");
                    List<Entry> entries = new ArrayList<>();
                    for (int i = 0; i < main.length(); i++) {
                        entries.add(new Entry(i, main.getInt(i)));
                    }
                    dataSet = new LineDataSet(entries, "Trending Chart for " + finalQuery);
                    dataSet.setColor(Color.parseColor("#6200EE"));
                    dataSet.setValueTextColor(Color.parseColor("#6200EE"));
                    dataSet.setCircleColor(Color.parseColor("#6200EE"));
                    dataSet.setCircleHoleColor(Color.parseColor("#6200EE"));
                    dataSet.setValueTextSize(8);
                    setData(dataSet);
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
        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(request);
    }

    private void setData(LineDataSet dataSet) {
        lineData = new LineData(dataSet);
        chart.setTouchEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.setData(lineData);
        Legend legend = chart.getLegend();
        legend.setTextSize(15);
        legend.setFormSize(15);
        chart.invalidate();
    }
}
