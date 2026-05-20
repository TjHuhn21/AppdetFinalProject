package com.example.heronhealth;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * ViewPager2 adapter for the three nutrition tabs:
 *   0 → MacrosFragment         (Carbs / Fat / Protein)
 *   1 → ExtendedMacrosFragment (all 7 macros, Total/Goal/Left)
 *   2 → CaloriesFragment       (calories by meal)
 */
public class NutritionPagerAdapter extends FragmentStateAdapter {

    private String date;

    private MacrosFragment    macrosFragment;
    private NutrientsFragment extendedFragment;
    private CaloriesFragment  caloriesFragment;

    public NutritionPagerAdapter(@NonNull FragmentManager fm,
                                 @NonNull Lifecycle lifecycle,
                                 String date) {
        super(fm, lifecycle);
        this.date = date;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                macrosFragment = MacrosFragment.newInstance(date);
                return macrosFragment;
            case 1:
                extendedFragment = NutrientsFragment.newInstance(date);
                return extendedFragment;
            case 2:
                caloriesFragment = CaloriesFragment.newInstance(date);
                return caloriesFragment;
            default:
                macrosFragment = MacrosFragment.newInstance(date);
                return macrosFragment;
        }
    }

    @Override
    public int getItemCount() { return 3; }

    public void updateDate(String newDate) {
        this.date = newDate;
        if (macrosFragment   != null) macrosFragment.refreshData(newDate);
        if (extendedFragment != null) extendedFragment.refreshData(newDate);
        if (caloriesFragment != null) caloriesFragment.refreshData(newDate);
    }
}