package io.bidmachine.test.app.params;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.bidmachine.test.app.ParamsHelper;
import io.bidmachine.test.app.Utils;

public class PriceFloorParamsFragment extends BaseParamsFragment {

    @Override
    protected void prepareView(Context context, ViewGroup parent, final ParamsHelper paramsHelper) {
        final Map<String, Double> currentParams = paramsHelper.getPriceFloorParamsMap();
        final ArrayList<ListItemContainer> items = new ArrayList<>();
        for (Map.Entry<String, Double> entry : currentParams.entrySet()) {
            items.add(new ListItemContainer(entry.getKey(), entry.getValue().toString()));
        }
        bindParamWidget(context, parent, "PriceFloors",
                new ListParamsWidget("PriceFloors", "Add PriceFloor", ListParamsWidget.Mode.KeyValue,
                        items.toArray(new ListItemContainer[0]),
                        new ParamWidget.ChangeTracker<ListItemContainer[]>() {
                            @Override
                            public void onChanged(ParamWidget widget, ListItemContainer[] param) {
                                final HashMap<String, Double> outMap = new HashMap<>();
                                for (ListItemContainer container : param) {
                                    try {
                                        outMap.put(container.getKey(), Double.parseDouble(container.getValue()));
                                    } catch (Exception ignored) {
                                    }
                                }
                                paramsHelper.setPriceFloorParams(outMap);
                            }
                        }) {
                    @Override
                    protected void onPrepareValueEditText(EditText editText) {
                        super.onPrepareValueEditText(editText);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    }

                    @Override
                    protected boolean isValueValid(String value) {
                        try {
                            Double.parseDouble(value);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.showToast(getContext(), "Value not valid");
                            return false;
                        }
                        return super.isValueValid(value);
                    }
                });
    }

}
