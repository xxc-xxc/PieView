package com.xxc.pieview.view;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.xxc.pieview.R;
import com.xxc.pieview.bean.PieEntry;
import com.xxc.pieview.widget2.PieView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PieView mPieView;
    private com.xxc.pieview.temp.PieView mPieView2;
    private com.xxc.pieview.widget.PieView mPieView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mPieView = findViewById(R.id.pie_view);
        mPieView2 = findViewById(R.id.pie_view2);
        mPieView3 = findViewById(R.id.pie_view3);
        initPieView();
        initPieView2();
        initPieView3();
    }

    private void initPieView() {
        mPieView.setColors(createColors());
        mPieView.setData(createData());
//        mPieView.isShowHole(false);
    }

    private ArrayList<Integer> createColors() {
        ArrayList<Integer> colorLists = new ArrayList<>();
        colorLists.add(Color.parseColor("#EBBF03"));
        colorLists.add(Color.parseColor("#ff4d4d"));
        colorLists.add(Color.parseColor("#8d5ff5"));
        colorLists.add(Color.parseColor("#41D230"));
        colorLists.add(Color.parseColor("#4FAAFF"));
        return colorLists;
    }

    private ArrayList<PieEntry> createData() {
        ArrayList<PieEntry> pieLists = new ArrayList<>();
        pieLists.add(new PieEntry(20.01F, "服装"));
        pieLists.add(new PieEntry(19.99F, "数码产品"));
        pieLists.add(new PieEntry(20.00F, "保健品"));
        pieLists.add(new PieEntry(20.00F, "户外运动用品"));
        pieLists.add(new PieEntry(20.00F, "其他"));
        return pieLists;
    }

    private void initPieView2() {
        mPieView2.setColors(createSleepColors());
        mPieView2.setData(createSleepData());
    }

    private ArrayList<Integer> createSleepColors() {
        ArrayList<Integer> colorLists = new ArrayList<>();
        colorLists.add(Color.parseColor("#F291C2")); //粉色
        colorLists.add(Color.parseColor("#FFCC66")); //黄色
        colorLists.add(Color.parseColor("#00CCCC")); //浅绿
        colorLists.add(Color.parseColor("#4D88FF")); //深蓝
        return colorLists;
    }

    private ArrayList<com.xxc.pieview.temp.PieView.PieEntry1> createSleepData() {
        ArrayList<com.xxc.pieview.temp.PieView.PieEntry1> pieLists = new ArrayList<>();
        pieLists.add(new com.xxc.pieview.temp.PieView.PieEntry1(20.00f, "觉醒", "0时45分"));
        pieLists.add(new com.xxc.pieview.temp.PieView.PieEntry1(30.00f, "浅睡眠", "8时45分"));
        pieLists.add(new com.xxc.pieview.temp.PieView.PieEntry1(30.00f, "深睡眠", "3时45分"));
        pieLists.add(new com.xxc.pieview.temp.PieView.PieEntry1(20.00f, "REM分期", "10时45分"));
        return pieLists;
    }

    private void initPieView3() {
        mPieView3.setColors(createSleepColors3());
        mPieView3.setData(createSleepData3());
    }

    private ArrayList<Integer> createSleepColors3() {
        ArrayList<Integer> colorLists = new ArrayList<>();
        colorLists.add(Color.parseColor("#F291C2")); //粉色
        colorLists.add(Color.parseColor("#FFCC66")); //黄色
        colorLists.add(Color.parseColor("#00CCCC")); //浅绿
        colorLists.add(Color.parseColor("#4D88FF")); //深蓝
        return colorLists;
    }

    private ArrayList<com.xxc.pieview.widget.PieView.PieEntry> createSleepData3() {
        ArrayList<com.xxc.pieview.widget.PieView.PieEntry> pieLists = new ArrayList<>();
        pieLists.add(new com.xxc.pieview.widget.PieView.PieEntry(20.00f, "觉醒", "0时45分"));
        pieLists.add(new com.xxc.pieview.widget.PieView.PieEntry(30.00f, "浅睡眠", "8时45分"));
        pieLists.add(new com.xxc.pieview.widget.PieView.PieEntry(30.00f, "深睡眠", "3时45分"));
        pieLists.add(new com.xxc.pieview.widget.PieView.PieEntry(20.00f, "REM分期", "10时45分"));
        return pieLists;
    }

}