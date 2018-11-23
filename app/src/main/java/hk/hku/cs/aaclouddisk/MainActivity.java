package hk.hku.cs.aaclouddisk;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import hk.hku.cs.aaclouddisk.main.TabPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initToolBar();
        initFinal();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.pager);
    }

    private void initToolBar() {
        // When requested, this adapter returns a specified Fragment(all in package "main.tab"),
        // ViewPager and its adapters use support library fragments, so use getSupportFragmentManager.
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.bold7);
        mTabLayout.getTabAt(1).setIcon(R.drawable.folder99);
        mTabLayout.getTabAt(2).setIcon(R.drawable.user88);

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mToolbar.setTitle(TabPagerAdapter.TITLES[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initFinal() {
        mToolbar.setTitle(TabPagerAdapter.TITLES[0]);
    }

}
