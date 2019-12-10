package io.bidmachine.test.app.params;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.bidmachine.test.app.ParamsHelper;
import io.bidmachine.test.app.R;
import io.bidmachine.test.app.Utils;

public abstract class BaseParamsFragment extends RootParamsFragment implements ParamsHelper.OnClearedListener {

    private LinearLayout contentParent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        if (context == null) throw new IllegalArgumentException("Context is null");
        final ScrollView parent = new ScrollView(context);
        parent.setBackgroundColor(Color.WHITE);
        parent.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        contentParent = new LinearLayout(context);
        contentParent.setOrientation(LinearLayout.VERTICAL);
        contentParent.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        final int parentPadding = Utils.dp2px(context, 16f);
        contentParent.setPadding(parentPadding, parentPadding, parentPadding, parentPadding);

        prepareView(context, contentParent, ParamsHelper.getInstance(getContext(), obtainAdsType()));

        parent.addView(contentParent);
        return parent;
    }

    @Override
    public void onParamsCleared() {
        recreateView();
    }

    protected void recreateView() {
        contentParent.removeAllViews();
        prepareView(getContext(), contentParent, ParamsHelper.getInstance(getContext(), obtainAdsType()));
    }

    protected abstract void prepareView(Context context, ViewGroup parent, ParamsHelper paramsHelper);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ParamsHelper.addOnClearListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ParamsHelper.removeOnClearListener(this);
        ParamsHelper.getInstance(getContext(), obtainAdsType()).syncGlobalParams();
    }

    protected View bindParamWidget(Context context, ViewGroup parent, String title, ParamWidget... widgets) {
        final ViewGroup widgetParent = createWidgetParent(context, parent, title);
        parent.addView(widgetParent);
        for (ParamWidget widget : widgets) {
            final View widgetView = createParamView(context, widgetParent, widget);
            parent.addView(widgetView);
        }
        if (parent.getChildCount() > 1 && widgetParent.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) widgetParent.getLayoutParams())
                    .topMargin = Utils.dp2px(context, 8);
        }
        return widgetParent;
    }

    private ViewGroup createWidgetParent(Context context, ViewGroup parent, String title) {
        final LinearLayout widgetParent = new LinearLayout(context);
        widgetParent.setOrientation(LinearLayout.VERTICAL);
        if (!TextUtils.isEmpty(title)) {
            final TextView textView = new TextView(context);
            textView.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);
            textView.setText(title);
            widgetParent.addView(textView);
        }
        return widgetParent;
    }

    private View createParamView(Context context, ViewGroup widgetParent, ParamWidget widget) {
        return widget.createView(context, widgetParent);
    }

    ListItemContainer[] listToContainers(List<String> source) {
        if (source == null || source.size() == 0) return new ListItemContainer[0];
        ListItemContainer[] out = new ListItemContainer[source.size()];
        for (int i = 0; i < source.size(); i++) {
            out[i] = new ListItemContainer(null, source.get(i));
        }
        return out;
    }

    List<String> containersToList(ListItemContainer[] containers) {
        if (containers == null || containers.length == 0) return new ArrayList<>();
        final ArrayList<String> out = new ArrayList<>();
        for (ListItemContainer container : containers) {
            out.add(container.getValue());
        }
        return out;
    }

    /*
    Params widgets
     */

    static abstract class ParamWidget<ParamType, ChangeParamType> {

        @NonNull
        private String key;
        @NonNull
        private ParamType params;
        @NonNull
        private ChangeTracker<ChangeParamType> changeTracker;

        ParamWidget(@NonNull String key, @NonNull ParamType params,
                    @NonNull ChangeTracker<ChangeParamType> changeTracker) {
            this.key = key;
            this.params = params;
            this.changeTracker = changeTracker;
        }

        @NonNull
        String getKey() {
            return key;
        }

        @NonNull
        ParamType getParams() {
            return params;
        }

        abstract View createView(Context context, ViewGroup parent);

        void notifyChange(ChangeParamType param) {
            changeTracker.onChanged(this, param);
        }

        public interface ChangeTracker<ParamType> {
            void onChanged(ParamWidget widget, ParamType param);
        }

    }

    static class TextInputParamWidget extends ParamWidget<String, String> {

        private EditText editText;

        TextInputParamWidget(@NonNull String key, @NonNull String params,
                             @NonNull ChangeTracker<String> changeTracker) {
            super(key, params, changeTracker);
        }

        @Override
        View createView(Context context, ViewGroup parent) {
            editText = new EditText(context);
            editText.setHint(getKey());
            editText.setText(getParams());
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    final String value = s.length() == 0 ? null : s.toString().trim();
                    notifyChange(value);
                }
            });
            return editText;
        }

        public void setError(String message) {
            editText.setError(message);
        }
    }

    static class SelectionContainer {

        private String title;
        private Object referenceObject;

        SelectionContainer(String title, Object referenceObject) {
            this.title = title;
            this.referenceObject = referenceObject;
        }

        public String getTitle() {
            return title;
        }

        Object getReferenceObject() {
            return referenceObject;
        }

    }

    static class SpinnerParamsWidget extends ParamWidget<SelectionContainer[], SelectionContainer> {

        private SelectionContainer defaultSelection;

        SpinnerParamsWidget(@NonNull String key, SelectionContainer[] params,
                            @Nullable SelectionContainer defaultSelection,
                            @NonNull ChangeTracker<SelectionContainer> changeTracker) {
            super(key, params, changeTracker);
            this.defaultSelection = defaultSelection;
        }

        @Override
        View createView(Context context, ViewGroup parent) {
            final Spinner spinner = new Spinner(context);
            final String[] displayTitles = new String[getParams().length];
            for (int i = 0; i < getParams().length; i++) {
                displayTitles[i] = getParams()[i].title;
            }
            spinner.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                    displayTitles) {

                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    view.setPadding(0, view.getPaddingTop(), 0, view.getPaddingBottom());
                    return view;
                }
            });
            if (defaultSelection != null) {
                for (int i = 0; i < getParams().length; i++) {
                    if (defaultSelection.getReferenceObject() == getParams()[i].getReferenceObject()) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    notifyChange(getParams()[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            return spinner;
        }
    }

    static class SwitchContainer {
        private String title;
        private Boolean isChecked;

        SwitchContainer(String title, Boolean isChecked) {
            this.title = title;
            this.isChecked = isChecked;
        }

        public String getTitle() {
            return title;
        }

        public Boolean isChecked() {
            return isChecked;
        }
    }

    static class SwitchParamsWidget extends ParamWidget<SwitchContainer, Boolean> {

        SwitchParamsWidget(@NonNull String key, @NonNull SwitchContainer params,
                           @NonNull ChangeTracker<Boolean> changeTracker) {
            super(key, params, changeTracker);
        }

        @Override
        View createView(Context context, ViewGroup parent) {
            SwitchCompat switchCompat = new SwitchCompat(context);
            switchCompat.setMinHeight(Utils.dp2px(context, 48));
            switchCompat.setText(getParams().getTitle());
            switchCompat.setChecked(getParams().isChecked() != null && getParams().isChecked());
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    notifyChange(isChecked);
                }
            });
            return switchCompat;
        }
    }

    static class ListItemContainer {
        private String key;
        private String value;

        public ListItemContainer(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    static class ListParamsWidget extends ParamWidget<ListItemContainer[], ListItemContainer[]> {

        enum Mode {
            KeyValue(R.layout.dialog_add_item_key_value), Value(R.layout.dialog_add_item_value);

            int layoutResource;

            Mode(int layoutResource) {
                this.layoutResource = layoutResource;
            }
        }

        private String addText;
        private ArrayList<ListItemContainer> currentItems = new ArrayList<>();

        private Mode mode = Mode.KeyValue;

        ListParamsWidget(@NonNull String key,
                         @NonNull String addText,
                         @NonNull Mode mode,
                         @NonNull ListItemContainer[] params,
                         @NonNull ChangeTracker<ListItemContainer[]> changeTracker) {
            super(key, params, changeTracker);
            this.addText = addText;
            this.mode = mode;
            currentItems.addAll(Arrays.asList(params));
        }

        @Override
        View createView(Context context, ViewGroup parent) {
            LinearLayout contentParent = new LinearLayout(context);
            contentParent.setOrientation(LinearLayout.VERTICAL);
            fillItems(context, contentParent);
            return contentParent;
        }

        private void fillItems(final Context context, final ViewGroup parent) {
            parent.removeAllViews();
            final View itemView = LayoutInflater.from(context).inflate(
                    android.R.layout.simple_list_item_1, parent, true);
            itemView.setMinimumHeight(Utils.dp2px(context, 48f));
            final TextView txtTitle = itemView.findViewById(android.R.id.text1);
            txtTitle.setText(addText);
            txtTitle.setPadding(0, 0, 0, 0);
            txtTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add, 0, 0, 0);
            txtTitle.setCompoundDrawablePadding(Utils.dp2px(context, 16f));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddDialog(context, parent);
                }
            });
            for (ListItemContainer item : currentItems) {
                Log.e("TEST", "addItem: " + item.getValue());
                addItem(context, parent, item);
            }
        }

        private void showAddDialog(final Context context, final ViewGroup parent) {
            final View contentView = LayoutInflater.from(context).inflate(
                    mode.layoutResource, null, false);
            final EditText edtKey = contentView.findViewById(R.id.editKey);
            final EditText edtValue = contentView.findViewById(R.id.editValue);
            if (edtKey != null) {
                onPrepareKeyEditText(edtKey);
            }
            if (edtValue != null) {
                onPrepareValueEditText(edtValue);
            }
            new AlertDialog.Builder(context)
                    .setTitle("Add item")
                    .setView(contentView)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (mode) {
                                case KeyValue: {
                                    final String key = edtKey.getText().toString().trim();
                                    final String value = edtValue.getText().toString().trim();
                                    if (isKeyValid(key) && isValueValid(value)) {
                                        for (ListItemContainer container : currentItems) {
                                            if (container.getKey().equals(key)) {
                                                Toast.makeText(context, "Item with key (" + key + ") already exists",
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                        currentItems.add(new ListItemContainer(key, value));
                                        updateItems(context, parent);
                                    }
                                    break;
                                }
                                case Value: {
                                    final String value = edtValue.getText().toString().trim();
                                    if (isValueValid(value)) {
                                        for (ListItemContainer container : currentItems) {
                                            if (container.getValue().equals(value)) {
                                                Toast.makeText(context, "Item with value (" + value + ") already exists",
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                        currentItems.add(new ListItemContainer(null, value));
                                        updateItems(context, parent);
                                    }
                                    break;
                                }
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //ignore
                        }
                    }).show();
        }

        protected void onPrepareKeyEditText(EditText editText) {
        }

        protected boolean isKeyValid(String key) {
            return !TextUtils.isEmpty(key);
        }

        protected void onPrepareValueEditText(EditText editText) {
        }

        protected boolean isValueValid(String value) {
            return !TextUtils.isEmpty(value);
        }

        private void addItem(final Context context, final ViewGroup parent, final ListItemContainer item) {
            final View itemView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            final TextView txtTitle = itemView.findViewById(android.R.id.text1);
            final TextView txtSubTitle = itemView.findViewById(android.R.id.text2);
            switch (mode) {
                case Value: {
                    txtTitle.setText(item.value);
                    txtSubTitle.setVisibility(View.GONE);
                    break;
                }
                case KeyValue: {
                    txtTitle.setText(item.key);
                    txtSubTitle.setText(item.value);
                    break;
                }
            }
            itemView.findViewById(R.id.btnRemove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentItems.remove(item);
                    updateItems(context, parent);
                }
            });
            parent.addView(itemView);
        }

        private void updateItems(Context context, ViewGroup parent) {
            fillItems(context, parent);
            notifyChange(currentItems.toArray(new ListItemContainer[currentItems.size()]));
        }

    }

}
