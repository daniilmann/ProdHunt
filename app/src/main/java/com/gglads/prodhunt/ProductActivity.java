package com.gglads.prodhunt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

public class ProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_activity);

        ImageView iv = (ImageView) findViewById(R.id.screen_iv);
        Ion.with(iv).load(getIntent().getStringExtra("IMG"));
        TextView name = (TextView) findViewById(R.id.pname_tv);
        name.setText(getIntent().getStringExtra("NAME"));
        TextView desc = (TextView) findViewById(R.id.pdesc_tv);
        desc.setText(getIntent().getStringExtra("DESC"));
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
