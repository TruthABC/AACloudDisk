package hk.hku.cs.aaclouddisk;

import android.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import hk.hku.cs.aaclouddisk.main.TabPagerAdapter;

public class MainActivity extends FragmentActivity {

    // When requested, this adapter returns a specified Fragment(all in package "main.tab"),
    TabPagerAdapter mTabPagerAdapter;
    ViewPager mTabPager;

    //tab layout
    private LinearLayout mTabRecent;
    private LinearLayout mTabFiles;
//    private LinearLayout mTabMusic;
    private LinearLayout mTabMe;

    //tab icon
    private ImageButton mRecentImg;
    private ImageButton mFilesImg;
//    private ImageButton mMusicImg;
    private ImageButton mMeImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();
        initActionBar();

    }

    private void initActionBar() {
        // Get actionBar from activity
        final ActionBar actionBar = getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                // When the tab is selected, switch to the corresponding page in the ViewPager.
                mTabPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }
            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }
        };

        // Add tabs, specifying the tab's text and TabListener
        for (int i = 0; i < TabPagerAdapter.TITLES.length; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(TabPagerAdapter.TITLES[i])
                            .setTabListener(tabListener));
        }
    }

    private void initViews() {
        // ViewPager and its adapters use support library fragments, so use getSupportFragmentManager.
        mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mTabPager = (ViewPager) findViewById(R.id.pager);
        mTabPager.setAdapter(mTabPagerAdapter);
    }

    private void initEvents() {
        mTabPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });
    }

}
