package com.example.routerider;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.routerider.fragments.MapFragment;
import com.example.routerider.fragments.ProfileFragment;
import com.example.routerider.fragments.ScheduleFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Hardcoded in this order, you'll want to use lists and make sure the titles match
        if (position == 0) {
            return new ScheduleFragment();
        } else if(position == 1) {
            return new MapFragment();
        }
        return new ProfileFragment();
    }

    @Override
    public int getItemCount() {
        // Hardcoded, use lists
        return 3;
    }
}