package io.bidmachine.test.app.params;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.test.app.ParamsHelper;
import io.bidmachine.test.app.R;

public class AdsParamsFragment extends RootParamsFragment {

    private static final String ARG_AdsType = "AdsType";

    public static AdsParamsFragment create(ParamsHelper.AdsType adsType) {
        return bindType(new AdsParamsFragment(), adsType);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ads_params, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            ParamsHelper.AdsType adsType = getArguments() != null
                    ? (ParamsHelper.AdsType) getArguments().getSerializable(ARG_AdsType) : null;
            getChildFragmentManager().beginTransaction()
                    .add(R.id.ads_fragment_content_parent, bindType(new TargetingParamsFragment(), adsType),
                            "AdsParamsFragment_Targeting")
//                    .add(R.id.ads_fragment_content_parent, bindType(new ExtraParamsFragment(), adsType),
//                            "AdsParamsFragment_Extra")
                    .add(R.id.ads_fragment_content_parent, bindType(new PriceFloorParamsFragment(), adsType),
                            "AdsParamsFragment_PriceFloors")
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
