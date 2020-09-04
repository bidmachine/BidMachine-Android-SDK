package io.bidmachine.test.app.params;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.test.app.ParamsHelper;

public class PublisherParamsFragment extends BaseParamsFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ScrollView scrollView = new ScrollView(getContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        scrollView.setBackgroundColor(Color.WHITE);

        final View origin = super.onCreateView(inflater, container, savedInstanceState);
        origin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        final LinearLayout parent = new LinearLayout(getContext());
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        parent.addView(origin);

        scrollView.addView(parent);
        return scrollView;
    }

    @Override
    protected void prepareView(Context context, ViewGroup parent, final ParamsHelper paramsHelper) {
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Id", paramsHelper.getPublisherId(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setPublisherId(param);
                            }
                        }));
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Name", paramsHelper.getPublisherName(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setPublisherName(param);
                            }
                        }));
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Domain", paramsHelper.getPublisherDomain(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setPublisherDomain(param);
                            }
                        }));
        bindParamWidget(context, parent, "Categories",
                        new ListParamsWidget("Categories", "Add Category", ListParamsWidget.Mode.Value,
                                             listToContainers(paramsHelper.getPublisherCategories()),
                                             new ParamWidget.ChangeTracker<ListItemContainer[]>() {
                                                 @Override
                                                 public void onChanged(ParamWidget widget, ListItemContainer[] param) {
                                                     paramsHelper.setPublisherCategories(containersToList(param));
                                                 }
                                             }));
    }

}
