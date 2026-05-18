package com.example.heronhealth;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

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

public class NutritionActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager2 pager2;

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
        tabLayout = findViewById(R.id.tab_layout);
        pager2 = findViewById(R.id.view_pager);

        viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(this, tabLayout.getTabCount());
        pager2.setAdapter(viewPagerFragmentAdapter);
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
                tab.getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().clearColorFilter();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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