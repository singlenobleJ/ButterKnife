package com.llj.butterknife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.llj.butterknife_annotation.BindView;
import com.llj.butterknife_api.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text1)
    TextView text1;
    @BindView(R.id.text2)
    TextView text2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        text1.setText("ButterKnife Success!");
        text2.setText("Perfect!");
    }
}
