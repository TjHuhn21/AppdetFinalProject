package com.example.heronhealth;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NutritionActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager2 pager2;
    NutritionPagerAdapter nutritionPagerAdapter;

    TextView tvSelectedDate;
    ImageButton btnPrevDay, btnNextDay;

    Calendar calendar;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat displaySdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    ViewPagerFragmentAdapter viewPagerFragmentAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nutrition);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", null);

        calendar = Calendar.getInstance(); // starts on today
        String todayDate = sdf.format(calendar.getTime());

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnPrevDay     = findViewById(R.id.btnPrevDay);
        btnNextDay     = findViewById(R.id.btnNextDay);

        tvSelectedDate.setText(displaySdf.format(calendar.getTime()));

        btnPrevDay.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDate();
        });

        btnNextDay.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDate();
        });

        tabLayout = findViewById(R.id.tab_layout);
        pager2    = findViewById(R.id.view_pager);

        nutritionPagerAdapter = new NutritionPagerAdapter(
                getSupportFragmentManager(), getLifecycle(), todayDate);
        pager2.setAdapter(nutritionPagerAdapter);
        pager2.setOffscreenPageLimit(3);

        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateDate() {
        String newDate = sdf.format(calendar.getTime());
        tvSelectedDate.setText(displaySdf.format(calendar.getTime()));
        nutritionPagerAdapter.updateDate(newDate); // pushes new date to all fragments
    }

    public static class ViewPagerFragmentAdapter extends FragmentStateAdapter{
        int size;

        public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity, int size){
            super(fragmentActivity);
            this.size = size;
        }
        @NonNull
        @Override
        public Fragment createFragment(int position){
            switch (position){
                case 0:
                    return  new CaloriesFragment();
                case 1:
                    return  new NutrientsFragment();
                case 2:
                    return  new MacrosFragment();
            }
            return  new CaloriesFragment();
        }

        @Override
        public int getItemCount(){
            return size;
        }
    }
}