package ca.thegreattrail.ui.base;

import android.util.Log;

/**
 * Created by Islam Salah on 8/8/17.
 */

public abstract class LazyLoadFragment extends BaseTrailDrawingFragment {
    protected boolean isVisible;
    protected boolean isViewCreated;
    protected boolean isLoaded;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser){
            Log.e("trung", "setUserVisibleHint: ");
            this.isVisible = true;
            loadUi();
        }
    }

    abstract protected void loadUi();
}
