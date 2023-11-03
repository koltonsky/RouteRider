package com.example.routerider;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.routerider.fragments.RoutesFragment;
import com.example.routerider.fragments.ProfileFragment;
import com.example.routerider.fragments.ScheduleFragment;

// NO CHATGPT
public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ScheduleFragment();
        } else if(position == 1) {
            return new RoutesFragment();
        }
        return new ProfileFragment();
    }

    @Override
    public int getItemCount() {
        // Hardcoded, use lists
        return 3;
    }
}