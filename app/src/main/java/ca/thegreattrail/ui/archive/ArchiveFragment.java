package ca.thegreattrail.ui.archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;

import ca.thegreattrail.R;
import ca.thegreattrail.ui.base.BaseFragment;
import ca.thegreattrail.ui.base.HomeTabMapFragment;

public class ArchiveFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        return view;
    }
}
