package ca.thegreattrail.ui.base;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import com.google.android.gms.maps.model.CameraPosition;
import ca.thegreattrail.R;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.ui.search.SearchListFragment;

/**
 * Created by Islam Salah on 10/30/17.
 * <p>
 * https://github.com/IslamSalah
 * islamsalah007@gmail.com
 */

public abstract class HomeTabMapFragment extends LazyLoadFragment implements SearchView.OnQueryTextListener {

    public String searchText;

    protected SearchListFragment searchListFragment;
    protected MainActivity activity;

    protected SearchListFragment createSearchListFragment() {
        if (myMap == null)
            return new SearchListFragment();

        CameraPosition cameraPosition = myMap.getCameraPosition();
        return SearchListFragment.newInstance(cameraPosition);
    }

    @Override
    protected void loadUi() {
        if (!isVisible || !isViewCreated || isLoaded)
            return;

        isLoaded = true;
        setupMapView();
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {

        try {
            if (searchListFragment.NeedToSave(newText))
                searchListFragment.SaveRecentSearch(newText);

            searchListFragment.refreshData(activity, newText);
        } catch (Exception e) {
            Log.i("onQueryTextSubmit", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchListFragment.refreshData(activity, newText);
        searchText = newText;
        return false;
    }

    // TODO : check if it's useless
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);
    }

}
