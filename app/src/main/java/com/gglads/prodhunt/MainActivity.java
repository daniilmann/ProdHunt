package com.gglads.prodhunt;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.gglads.prodhunt.Entities.Product;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public Prefs prefs = Prefs.getInstance();
    private MainActivity main = this;
    private boolean isRefreshing = false;
    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.ERROR_MSG:
                    Toast.makeText(getApplicationContext(), ((Throwable) msg.obj).getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                case Constants.UPDATE_PRODS_MSG:
                    PHAPIHelper.updateProducts(getApplicationContext());
                    break;
                case Constants.PRODUCT_MSG:
                    updateProduct((Product) msg.obj);
                    break;
                case Constants.STOP_REFRESH:
                    if (swipeView.isRefreshing())
                        setRefreshing(false);
                    break;
            }
        }
    };
    private BroadcastReceiver receiver = null;

    private RecyclerView prodListView = null;
    private RecyclerView.LayoutManager prodListManager = null;
    private ProductAdapter prodAdapter = null;

    private SwipeRefreshLayout swipeView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs.init(getApplicationContext());
        TextView tv = (TextView) findViewById(R.id.cat_name_tv);
        try {
            tv.setText(prefs.getCurrentCat());
        }
        catch (Exception e) {
            tv.setText("tech");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.UPDATE_PRODUCT_ACTION);
        filter.addAction(Constants.STOP_REFRESH_ACTION);
        filter.addAction(Constants.TIME_NOTIFY_ACTION);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Constants.UPDATE_PRODUCT_ACTION:
                        updateProduct((Product) intent.getExtras().get("PRODUCT"));
                        break;
                    case Constants.STOP_REFRESH_ACTION:
                        setRefreshing(false);
                        break;
                    case Constants.ERROR_ACTION:
                        Toast.makeText(getApplicationContext(), ((Throwable) intent.getExtras().get("ERROR")).getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                    case Constants.TIME_NOTIFY_ACTION:
                        int n = intent.getIntExtra("PCOUNT", 0);
                        if (n > 1) {
                            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent resultPendingIntent =  PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            builder.setContentIntent(resultPendingIntent);
                            if (n == 1) {
                                String pname = intent.getStringExtra("PNAME");
                                String pdesc = intent.getStringExtra("PDESC");
                                String pvote = intent.getStringExtra("PVOTES");
                                Bitmap pthumb = (Bitmap) intent.getExtras().get("PTHUMB");
                                builder.setContentTitle(pname);
                                builder.setContentText(pdesc + "Votes: " + pvote);
                                builder.setLargeIcon(pthumb);
                            }
                            else {
                                builder.setContentTitle("New Products");
                                builder.setContentText("There are " + n + " new products");
                            }
                            builder.setSmallIcon(R.drawable.bell_ring);
                            builder = builder.setPriority(Notification.PRIORITY_MAX);
                            nm.notify(001, builder.build());
                        }
                }
            }
        };
        registerReceiver(receiver, filter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe_view);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setRefreshing(true);
             }
        });

        prodListView = (RecyclerView) findViewById(R.id.prod_list_rv);
        prodListView.setHasFixedSize(true);
        prodListManager = new LinearLayoutManager(this);
        ((LinearLayoutManager)prodListManager).setOrientation(LinearLayoutManager.VERTICAL);
        prodListView.setLayoutManager(prodListManager);
        prodAdapter = new ProductAdapter();
        prodListView.setAdapter(prodAdapter);

        setRefreshing(true);

        Intent notifyIntent = new Intent(this, NotificationReceiver.class);
        notifyIntent.setAction(Constants.TIME_UPDATE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), Constants.TIME_UPDATE, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis(),
                1000 * Prefs.getInstance().getUpdatePeriod(), pendingIntent);
    }

    public void setRefreshing(final boolean state) {
        swipeView.post(new Runnable() {
            @Override
            public void run() {
                if (state) {
                    if (!isRefreshing) {
                        isRefreshing = true;
                        swipeView.setRefreshing(state);
                        PHAPIHelper.updateProducts(getApplicationContext());
                    }
                }
                else {
                    isRefreshing = false;
                    swipeView.setRefreshing(state);
                }
            }
        });
    }

    public void updateProduct(Product product) {
        if (product != null)
            prodAdapter.addProduct(product);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_settings:
                Toast.makeText(getApplicationContext(), "Some settings could be here.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_linkedin:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://linkedin.com/in/daniilmann/")));
                break;
            case R.id.nav_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/daniilmann")));
                break;
            case R.id.nav_send:
                startActivity(new Intent(Intent.ACTION_SEND, Uri.parse("mailto:daniilmann@gmail.com")));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
