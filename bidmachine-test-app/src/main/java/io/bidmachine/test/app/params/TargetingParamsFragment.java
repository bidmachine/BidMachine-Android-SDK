package io.bidmachine.test.app.params;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.bidmachine.test.app.ParamsHelper;
import io.bidmachine.utils.Gender;

public class TargetingParamsFragment extends BaseParamsFragment {

    private static final Pattern locationPattern =
            Pattern.compile("(^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?))((?:,|$)?(\\s*))([-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?))$");

    @Override
    protected void prepareView(Context context, ViewGroup parent, final ParamsHelper paramsHelper) {
        final Gender[] genders = Gender.values();
        final SelectionContainer[] displayGenders = new SelectionContainer[genders.length];
        for (int i = 0; i < genders.length; i++) {
            displayGenders[i] = new SelectionContainer(genders[i].name(), genders[i]);
        }
        final Gender currentGender = paramsHelper.getCurrentGender();

        bindParamWidget(context, parent, "Gender",
                new SpinnerParamsWidget("Gender", displayGenders,
                        currentGender != null ? new SelectionContainer(currentGender.name(), currentGender) : null,
                        new ParamWidget.ChangeTracker<SelectionContainer>() {
                            @Override
                            public void onChanged(ParamWidget widget, SelectionContainer param) {
                                paramsHelper.setGender((Gender) param.getReferenceObject());
                            }
                        }));

        StringBuilder currentKeywords = new StringBuilder();
        if (paramsHelper.getCurrentKeywords() != null) {
            for (String keyword : paramsHelper.getCurrentKeywords()) {
                if (currentKeywords.length() > 0) {
                    currentKeywords.append(",");
                }
                currentKeywords.append(keyword);
            }
        }

        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Keywords (for multiple, split by \",\")", currentKeywords.toString(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                if (param != null) {
                                    final String[] splitted = param.split(",");
                                    final List<String> outParams = new ArrayList<>();
                                    for (String variable : splitted) {
                                        if (!TextUtils.isEmpty(variable) && !",".equals(variable)) {
                                            outParams.add(variable);
                                        }
                                    }
                                    paramsHelper.setKeywords(outParams.toArray(new String[outParams.size()]));
                                } else {
                                    paramsHelper.setKeywords((String[]) null);
                                }
                            }
                        }));

        bindParamWidget(context, parent, null,
                new TextInputParamWidget("UserId", paramsHelper.getCurrentUserID(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setUserId(param);
                            }
                        }));

        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Birthday year", paramsHelper.getCurrentBirthdayYear() != null
                        ? String.valueOf(paramsHelper.getCurrentBirthdayYear()) : null,
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                if (param == null) {
                                    paramsHelper.setBirthdayYear(null);
                                } else {
                                    try {
                                        int year = Integer.parseInt(param);
                                        if (year >= 1900 && year <= Calendar.getInstance().get(Calendar.YEAR)) {
                                            paramsHelper.setBirthdayYear(year);
                                        } else {
                                            ((TextInputParamWidget) widget).setError("Format not valid");
                                            paramsHelper.setBirthdayYear(null);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        paramsHelper.setBirthdayYear(null);
                                        ((TextInputParamWidget) widget).setError("Format not valid");
                                    }
                                }
                            }
                        }));

        final ArrayList<Locale> availableLocales = ParamsHelper.obtainAvailableLocales();
        final SelectionContainer[] displayLocales = new SelectionContainer[availableLocales.size()];
        for (int i = 0; i < availableLocales.size(); i++) {
            Locale locale = availableLocales.get(i);
            displayLocales[i] = new SelectionContainer(String.format("%s, %s",
                    locale.getDisplayCountry(), locale.getCountry()), locale);
        }
        final String currentCountry = paramsHelper.getCurrentCountry();
        Locale currentCountryLocale = null;
        if (currentCountry != null) {
            for (Locale locale : availableLocales) {
                if (currentCountry.equals(locale.getCountry())) {
                    currentCountryLocale = locale;
                    break;
                }
            }
        }

        bindParamWidget(context, parent, "Country",
                new SpinnerParamsWidget("Country", displayLocales,
                        currentCountryLocale != null ? new SelectionContainer(String.format("%s, %s",
                                currentCountryLocale.getDisplayCountry(), currentCountryLocale.getCountry()),
                                currentCountryLocale) : null,
                        new ParamWidget.ChangeTracker<SelectionContainer>() {
                            @Override
                            public void onChanged(ParamWidget widget, SelectionContainer param) {
                                paramsHelper.setCountry(((Locale) param.getReferenceObject()).getCountry());
                            }
                        }));

        bindParamWidget(context, parent, null,
                new TextInputParamWidget("City", paramsHelper.getCurrentCity(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setCity(param);
                            }
                        }));

        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Zip", paramsHelper.getCurrentZip(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setZip(param);
                            }
                        }));

        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Store Url", paramsHelper.getStoreUrl(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setStoreUrl(param);
                            }
                        }));

        bindParamWidget(context, parent, null,
                new SwitchParamsWidget("Is Paid",
                        new SwitchContainer("Is Paid", paramsHelper.isPaid()),
                        new ParamWidget.ChangeTracker<Boolean>() {
                            @Override
                            public void onChanged(ParamWidget widget, Boolean param) {
                                paramsHelper.setPaid(param);
                            }
                        }));

        String currentLocation = "";
        if (paramsHelper.getCurrentDeviceLocation() != null) {
            currentLocation = paramsHelper.getCurrentDeviceLocation().getLatitude() + ", " + paramsHelper.getCurrentDeviceLocation().getLongitude();
        }
        bindParamWidget(context, parent, null,
                new TextInputParamWidget("Device Location", currentLocation,
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                if (!TextUtils.isEmpty(param)) {
                                    Matcher matcher = locationPattern.matcher(param);
                                    if (matcher.matches()) {
                                        MatchResult matchResult = matcher.toMatchResult();
                                        double latitude = Double.valueOf(matchResult.group(1));
                                        double longitude = Double.valueOf(matchResult.group(7));
                                        Location targetingLocation = new Location("userSpecified");
                                        targetingLocation.setLatitude(latitude);
                                        targetingLocation.setLongitude(longitude);
                                        targetingLocation.setTime(System.currentTimeMillis() + 1000);
                                        paramsHelper.setDeviceLocation(targetingLocation);
                                    }
                                } else {
                                    paramsHelper.setDeviceLocation(null);
                                }
                            }
                        }));

        bindParamWidget(context, parent, "Blocked Advertiser IAB Categories",
                new ListParamsWidget("Blocked Advertiser IAB Categories", "Add Category", ListParamsWidget.Mode.Value,
                        listToContainers(paramsHelper.getBlockedAdvertiserIABCategories()),
                        new ParamWidget.ChangeTracker<ListItemContainer[]>() {
                            @Override
                            public void onChanged(ParamWidget widget, ListItemContainer[] param) {
                                paramsHelper.setBlockedAdvertiserIABCategories(containersToList(param));
                            }
                        }));

        bindParamWidget(context, parent, "Blocked Advertiser Domains",
                new ListParamsWidget("Blocked Advertiser Domains", "Add Domain", ListParamsWidget.Mode.Value,
                        listToContainers(paramsHelper.getBlockedAdvertiserDomains()),
                        new ParamWidget.ChangeTracker<ListItemContainer[]>() {
                            @Override
                            public void onChanged(ParamWidget widget, ListItemContainer[] param) {
                                paramsHelper.setBlockedAdvertiserDomains(containersToList(param));
                            }
                        }));

        bindParamWidget(context, parent, "Blocked Application",
                new ListParamsWidget("Blocked Application", "Add Application", ListParamsWidget.Mode.Value,
                        listToContainers(paramsHelper.getBlockedApplications()),
                        new ParamWidget.ChangeTracker<ListItemContainer[]>() {
                            @Override
                            public void onChanged(ParamWidget widget, ListItemContainer[] param) {
                                paramsHelper.setBlockedApplications(containersToList(param));
                            }
                        }));
    }
}
