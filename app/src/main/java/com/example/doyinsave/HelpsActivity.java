package com.example.doyinsave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.doyinsave.Fragment.Cach1Framgnet;
import com.example.doyinsave.Fragment.Cach2Fragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HelpsActivity extends AppCompatActivity {
    private TabLayout tabLayout1;
    private ViewPager2 viewPager1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);
        tabLayout1 = findViewById(R.id.tab_layout);
        viewPager1 = findViewById(R.id.view_page);
        setStatusBarGradiant(this);
       CustomFragmentAdapter adapter = new CustomFragmentAdapter(this);
        viewPager1.setAdapter(adapter);
        new TabLayoutMediator(tabLayout1, viewPager1, new TabLayoutMediator.TabConfigurationStrategy( ) {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0 :{
                        tab.setText(" Cách 1 ").setIcon(R.drawable.icon_download);
                        break;
                    }
                    case 1 :{
                        tab.setText(" Cách 2 ").setIcon(R.drawable.icon_download);
                        break;
                    }
                }
            }
        }).attach();
        findViewById(R.id.linearLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{ContextCompat.getColor(this, R.color.color1),
                        ContextCompat.getColor(this, R.color.color2),
                        ContextCompat.getColor(this, R.color.color3)});
        tabLayout1.setSelectedTabIndicatorColor(Color.parseColor("#0066FF"));
        tabLayout1.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));
        tabLayout1.setTabTextColors(Color.parseColor("#727272"), Color.parseColor("#ffffff"));
    }
    public class CustomFragmentAdapter extends FragmentStateAdapter {
        public CustomFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:{
                    return new Cach1Framgnet();
                }
                case 1:{
                    return new Cach2Fragment();
                }
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.color.white);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
//            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }
}