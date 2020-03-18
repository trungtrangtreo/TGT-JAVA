package ca.thegreattrail.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

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
                        if (!(currentFragment instanceof MapFragment)) {
                            fragment = new MapFragment();
                        }
                        tag = MapFragment.class.getSimpleName();
                        break;

                    case R.id.nav_measure:
                        if (!(currentFragment instanceof MeasureFragment)) {
                            fragment = new MeasureFragment();
                        }
                        tag = MeasureFragment.class.getSimpleName();
                        break;

                    case R.id.nav_tracker:
                        if (!(currentFragment instanceof TrackerFragment)) {
                            fragment = new TrackerFragment();
                        }
                        tag = TrackerFragment.class.getSimpleName();
                        break;

                    case R.id.nav_archive:
                        if (!(currentFragment instanceof ArchiveFragment)) {
                            fragment = new ArchiveFragment();
                        }
                        tag = ArchiveFragment.class.getSimpleName();
                        break;
                }

                if (fragment != null) {
                    replaceFragmentInActivity(fragment, R.id.main_nav_host);
                }

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
