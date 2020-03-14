package ca.thegreattrail.ui.howtouse;

import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import ca.thegreattrail.R;
import ca.thegreattrail.ui.base.BaseActivity;

public class HowToUseActivity extends BaseActivity {

    private ViewPager viewpager;
    private TabLayout tabLayout;
    private PhotosPagerAdapter photosPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_use);

        initView();
        initAdapter();
    }

    private void initView() {
        viewpager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void initAdapter() {
        photosPagerAdapter = new PhotosPagerAdapter(this);
        viewpager.setAdapter(photosPagerAdapter);

        tabLayout.setupWithViewPager(viewpager, true);
    }
}
