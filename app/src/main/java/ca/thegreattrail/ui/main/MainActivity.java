package ca.thegreattrail.ui.main;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import ca.thegreattrail.R;
import ca.thegreattrail.data.model.db.TrailSegmentLight;
import ca.thegreattrail.data.model.db.TrailSegmentLightLight;
import ca.thegreattrail.data.model.db.TrailWarning;
import ca.thegreattrail.ui.archive.ArchiveFragment;
import ca.thegreattrail.ui.base.BaseTrailDrawingFragment;
import ca.thegreattrail.ui.map.MapFragment;
import ca.thegreattrail.ui.measure.MeasureFragment;
import ca.thegreattrail.ui.tracker.TrackerFragment;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnCloseNavigationViewListener {

    public static ArrayList<TrailSegmentLightLight> listSegmentsByTrailId = null;
    public static HashMap<String, ArrayList<LatLng>> listPointsByTrailId = null;
    public static ArrayList<TrailSegmentLight> listSegments = null;
    public static HashMap<Integer, ArrayList<LatLng>> listPoints = null;
    public static ArrayList<TrailWarning> trailWarnings = null;

    private NavController navController;
    private MainAdapter mainAdapter;
    private DrawerLayout mainDrawerLayout;
    private Toolbar toolbar;
    private RecyclerView recycleView;
    private BottomNavigationView bottomNavigationView;

    private AppBarConfiguration appBarConfiguration;

    public static Fragment fragment = null;

    final Fragment fragmentMap = new MapFragment();
    final Fragment fragmentMeasure = new MeasureFragment();
    final Fragment fragmentTracker = new TrackerFragment();
    final Fragment fragmentArchieve = new ArchiveFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = fragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initFragment();

    }

    private void initFragment() {
        fm.beginTransaction().add(R.id.main_nav_host, fragmentArchieve, "4").hide(fragmentArchieve).commit();
        fm.beginTransaction().add(R.id.main_nav_host, fragmentTracker, "3").hide(fragmentTracker).commit();
        fm.beginTransaction().add(R.id.main_nav_host, fragmentMeasure, "2").hide(fragmentMeasure).commit();
        fm.beginTransaction().add(R.id.main_nav_host, fragmentMap, "1").commit();
    }

    private void initView() {

        navController = Navigation.findNavController(this, R.id.main_nav_host);
        mainDrawerLayout = findViewById(R.id.main_drawer_layout);
        toolbar = findViewById(R.id.main_toolbar);
        bottomNavigationView = findViewById(R.id.main_bottom_navigation_view);
        recycleView = findViewById(R.id.recycleView);

        mainAdapter = new MainAdapter(this, initArray(), this);

        recycleView.setLayoutManager(new LinearLayoutManager(this));
        recycleView.setAdapter(mainAdapter);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map,
                R.id.nav_measure,
                R.id.nav_tracker,
                R.id.nav_archive,
                R.id.nav_about,
                R.id.nav_about_keen,
                R.id.nav_donate,
                R.id.nav_feedback,
                R.id.nav_offline_map,
                R.id.nav_faq,
                R.id.nav_setting,
                R.id.nav_exit)
                .setDrawerLayout(mainDrawerLayout) //Pass the drawer layout id from activity xml
                .build();

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        bottomNavigationView.setItemIconTintList(null);

        itemNavigationClick();
    }

    private void itemNavigationClick() {

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
                String tag = "";
                switch (menuItem.getItemId()) {
                    case R.id.nav_map:
                        fm.beginTransaction().hide(active).show(fragmentMap).commit();
                        active = fragmentMap;
                        break;

                    case R.id.nav_measure:
                        fm.beginTransaction().hide(active).show(fragmentMeasure).commit();
                        active = fragmentMeasure;
                        break;

                    case R.id.nav_tracker:
                        fm.beginTransaction().hide(active).show(fragmentTracker).commit();
                        active = fragmentTracker;
                        break;

                    case R.id.nav_archive:
                        fm.beginTransaction().hide(active).show(fragmentArchieve).commit();
                        active = fragmentArchieve;
                        break;
                }

//                Toast.makeText(MainActivity.this, fragment.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
//                if (fragment != null) {
//                    replaceFragmentInActivity(fragment, R.id.main_nav_host);
//                }

                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mainDrawerLayout) || super.onSupportNavigateUp();
    }

    @Override
    public void onCloseNavigationView() {
        if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void replaceFragmentInActivity(Fragment fragment, int container) {
        getSupportFragmentManager().beginTransaction().replace(container, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commitAllowingStateLoss();
    }

    private ArrayList<String> initArray() {
        ArrayList<String> navs = new ArrayList<>();
        navs.add(getString(R.string.menu_about));
        navs.add(getString(R.string.menu_about_keen));
        navs.add(getString(R.string.menu_donate));
        navs.add(getString(R.string.menu_feedback));
        navs.add(getString(R.string.menu_offline_map));
        navs.add(getString(R.string.menu_faq));
        navs.add(getString(R.string.menu_setting));
        navs.add(getString(R.string.menu_exit));
        return navs;
    }

    public class listSegments {
    }
}
