package com.bupt.pulltozoom.pulltozoomrefresh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    PullToZoomRefreshView pullToZoomRefreshView;
    ArrayAdapter<String> adapter;
    ListView listView;
    String[] items = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullToZoomRefreshView = (PullToZoomRefreshView) findViewById(R.id.pull_to_zoom_head);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);

        pullToZoomRefreshView.setOnRefreshListener(new PullToZoomRefreshView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pullToZoomRefreshView.finishRefreshing();
            }
        },0);
    }
}
