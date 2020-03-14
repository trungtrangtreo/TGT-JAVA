package ca.thegreattrail.ui.howtouse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.util.ArrayList;
import java.util.List;
import ca.thegreattrail.R;
import ca.thegreattrail.data.model.other.Tutorial;
import ca.thegreattrail.ui.main.MainActivity;

public class PhotosPagerAdapter extends PagerAdapter {

    private Context context;
    private AppCompatActivity appCompatActivity;
    private List<Tutorial> photos = new ArrayList<>();

    public PhotosPagerAdapter(Context context) {
        this.context = context;
        appCompatActivity = (AppCompatActivity) context;
        initList();
    }


    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.item_photo_2, container, false);

        ImageView ivPage = layout.findViewById(R.id.ivPage);
        TextView tvTitleHowTo = layout.findViewById(R.id.tvTitleHowTo);
        TextView tvDescriptionHowTo = layout.findViewById(R.id.tvDescriptionHowTo);
        Button btnGetStarted = layout.findViewById(R.id.btnGetStarted);
        RelativeLayout rlHowUseOne = layout.findViewById(R.id.rlHowUseOne);
        final ProgressBar progressBar = layout.findViewById(R.id.progressBar);

        if (position == 0) {
            rlHowUseOne.setVisibility(View.VISIBLE);
            ivPage.setVisibility(View.GONE);
        } else {
            rlHowUseOne.setVisibility(View.GONE);
            ivPage.setVisibility(View.VISIBLE);
        }

        ivPage.setImageResource(photos.get(position).getImage());
        tvTitleHowTo.setText(photos.get(position).getTitle());
        tvDescriptionHowTo.setText(photos.get(position).getDescription());

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                checkAccessLocationPermission();
            }
        });

        container.addView(layout);
        return layout;
    }

    private void checkAccessLocationPermission() {

        Dexter.withActivity(appCompatActivity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        context.startActivity(new Intent(context, MainActivity.class));
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void initList() {

        Tutorial howToOne = new Tutorial(
                R.drawable.ic_how_to_01,
                context.getString(R.string.title_how_to_01),
                context.getString(R.string.message_description_how_to_01));
        photos.add(howToOne);

        Tutorial howToTwo = new Tutorial(
                R.drawable.ic_how_to_02,
                context.getString(R.string.title_how_to_02),
                context.getString(R.string.message_description_how_to_02));
        photos.add(howToTwo);

        Tutorial howToThree = new Tutorial(
                R.drawable.ic_how_to_03,
                context.getString(R.string.title_how_to_03),
                context.getString(R.string.message_description_how_to_03));
        photos.add(howToThree);

        Tutorial howToFour = new Tutorial(
                R.drawable.ic_how_to_04,
                context.getString(R.string.title_how_to_04),
                context.getString(R.string.message_description_how_to_04));
        photos.add(howToFour);

        Tutorial howToFive = new Tutorial(
                R.drawable.ic_how_to_05,
                context.getString(R.string.title_how_to_05),
                context.getString(R.string.message_description_how_to_05));
        photos.add(howToFive);
    }
}
