package com.devon_dickson.apps.oncampus;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener{
    private ViewPager viewPager;
    private ActionBar actionBar;
    private String[] tabNames = { "Upcoming", "Saved", "Attended" };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Default");
        Log.d("Theme", themeName);
        if (themeName.equals("Night")) {
            setTheme(R.style.NightAppTheme);



        }else if (themeName.equals("FIM")) {
            //Toast.makeText(this, "set theme", Toast.LENGTH_SHORT).show();

            setTheme(R.style.FimAppTheme);

        }else if (themeName.equals("Default")) {

            setTheme(R.style.AppTheme);

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabPagerAdapter);
        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        for (int i = 0; i < 3; i++) {
            actionBar.addTab(actionBar.newTab().setText(tabNames[i])
                    .setTabListener(this));
        }

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int postion) {
                actionBar.setSelectedNavigationItem(postion);
            }
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
     public void onResume() {

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Default");
        Log.d("Theme", themeName);
        if (themeName.equals("Night")) {
            setTheme(R.style.NightAppTheme);



        }else if (themeName.equals("FIM")) {
            //Toast.makeText(this, "set theme", Toast.LENGTH_SHORT).show();

            setTheme(R.style.FimAppTheme);

        }else if (themeName.equals("Default")) {

            setTheme(R.style.AppTheme);

        }
        super.onResume();
    }

    @Override
    public void onRestart() {

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Default");
        Log.d("Theme", themeName);
        if (themeName.equals("Night")) {
            setTheme(R.style.NightAppTheme);



        }else if (themeName.equals("FIM")) {
            //Toast.makeText(this, "set theme", Toast.LENGTH_SHORT).show();

            setTheme(R.style.FimAppTheme);

        }else if (themeName.equals("Default")) {

            setTheme(R.style.AppTheme);

        }
        super.onRestart();
        recreate();
    }
}