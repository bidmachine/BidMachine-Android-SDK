package io.bidmachine.test.app.params;

import android.content.Context;
import android.view.ViewGroup;

import io.bidmachine.test.app.ParamsHelper;

public class UserRestrictionsParamsFragment extends BaseParamsFragment {

    @Override
    protected void prepareView(Context context, ViewGroup parent, final ParamsHelper paramsHelper) {
        bindParamWidget(context, parent, "GDPR",
                new TextInputParamWidget("GDPR consent string",
                        paramsHelper.getConsentString(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setConsentConfig(paramsHelper.getHasConsent() == Boolean.TRUE, param);
                            }
                        }),
                new SwitchParamsWidget("hasConsent",
                        new SwitchContainer("User has given consent to the processing of personal data",
                                paramsHelper.getHasConsent()),
                        new ParamWidget.ChangeTracker<Boolean>() {
                            @Override
                            public void onChanged(ParamWidget widget, Boolean param) {
                                paramsHelper.setHasConsent(param);
                            }
                        }),
                new SwitchParamsWidget("GDPR",
                        new SwitchContainer("Subject to GDPR",
                                paramsHelper.getSubjectToGDPR()),
                        new ParamWidget.ChangeTracker<Boolean>() {
                            @Override
                            public void onChanged(ParamWidget widget, Boolean param) {
                                paramsHelper.setSubjectToGDPR(param);
                            }
                        }));
        bindParamWidget(context, parent, "Coppa",
                new SwitchParamsWidget("Coppa",
                        new SwitchContainer("Children's Online Privacy Protection Act",
                                paramsHelper.hasCoppa()),
                        new ParamWidget.ChangeTracker<Boolean>() {
                            @Override
                            public void onChanged(ParamWidget widget, Boolean param) {
                                paramsHelper.setCoppa(param);
                            }
                        }));
        bindParamWidget(context, parent, "US Privacy",
                new TextInputParamWidget("US Privacy String",
                        paramsHelper.getUSPrivacyString(),
                        new ParamWidget.ChangeTracker<String>() {
                            @Override
                            public void onChanged(ParamWidget widget, String param) {
                                paramsHelper.setUSPrivacyString(param);
                            }
                        }));
    }

}
