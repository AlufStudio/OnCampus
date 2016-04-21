package com.devon_dickson.apps.oncampus;

/**
 * Created by ddickson1 on 3/23/2016.
*/

        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.app.FragmentPagerAdapter;

public class TabPagerAdapter extends FragmentPagerAdapter {

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int arg0) {
        switch (arg0) {
            case 0:
                return new UpcomingTab();
            case 1:
                //return new SecondTab();
            case 2:
                //return new ThirdTab();
            default:
                break;
        }
        return new Fragment();
    }

    @Override
    public int getCount() {
        return 3;
    }
}