package com.example.routerider;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.routerider.fragments.MapFragment;
import com.example.routerider.fragments.ProfileFragment;
import com.example.routerider.fragments.ScheduleFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private List<Fragment> fragmentList;

    public ViewPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);

        // Initialize the fragment list
        fragmentList = new ArrayList<>();

        fragmentList.add(new ScheduleFragment());
        fragmentList.add(new MapFragment());
        fragmentList.add(new ProfileFragment());
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }
}
