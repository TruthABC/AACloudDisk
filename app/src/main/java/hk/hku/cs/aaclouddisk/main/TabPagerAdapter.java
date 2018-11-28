package hk.hku.cs.aaclouddisk.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import hk.hku.cs.aaclouddisk.main.tab.MP3Fragment;
import hk.hku.cs.aaclouddisk.main.tab.FilesFragment;
import hk.hku.cs.aaclouddisk.main.tab.MeFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {

    public static final String[] TITLES = {"Files","Cloud MP3","Me"};
    Fragment[] fragments;

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new Fragment[TITLES.length];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        if (fragments[position] == null) {
            switch (position) {
                case 0: fragments[position] = new FilesFragment(); break;
                case 1: fragments[position] = new MP3Fragment(); break;
                case 2: fragments[position] = new MeFragment(); break;
            }
        }
        return fragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

}
