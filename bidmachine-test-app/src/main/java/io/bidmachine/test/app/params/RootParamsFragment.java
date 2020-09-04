package io.bidmachine.test.app.params;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import io.bidmachine.test.app.ParamsHelper;
import io.bidmachine.test.app.R;

public class RootParamsFragment extends DialogFragment {

    public static final String ARG_AdsType = "AdsType";

    public static <T extends Fragment> T bindType(T fragment, ParamsHelper.AdsType adsType) {
        if (adsType != null) {
            Bundle args = fragment.getArguments() != null ? fragment.getArguments() : new Bundle();
            args.putSerializable(ARG_AdsType, adsType);
            fragment.setArguments(args);
        }
        return fragment;
    }

    private ParamsHelper.AdsType adsType;

    protected ParamsHelper.AdsType obtainAdsType() {
        if (adsType == null) {
            adsType = getArguments() != null
                    ? (ParamsHelper.AdsType) getArguments().getSerializable(ARG_AdsType) : null;
        }
        return adsType;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(getParentFragment() == null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem saveParamsItem = menu.add(0, R.id.menu_item_save_params, 0, "Save Params");
        saveParamsItem.setIcon(R.drawable.ic_save);
        saveParamsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        MenuItem deleteParamsItem = menu.add(0, R.id.menu_item_delete_params, 0, "Save Params");
        deleteParamsItem.setIcon(R.drawable.ic_delete);
        deleteParamsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save_params: {
                ParamsHelper.storeParams(getContext());
                break;
            }
            case R.id.menu_item_delete_params: {
                ParamsHelper.clearParams(getContext());
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
