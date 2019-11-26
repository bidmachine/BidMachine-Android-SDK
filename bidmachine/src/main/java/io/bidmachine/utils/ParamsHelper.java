package io.bidmachine.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.bidmachine.PriceFloorParams;
import io.bidmachine.R;
import io.bidmachine.TargetingParams;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.models.RequestBuilder;

public class ParamsHelper {

    private static final ThreadLocal<ArrayList<String>> tmpStringList =
            new ThreadLocal<ArrayList<String>>() {
                @Override
                protected ArrayList<String> initialValue() {
                    return new ArrayList<>();
                }
            };

    private static final Pattern locationPattern =
            Pattern.compile("(^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?))((?:,|$)?(\\s*))([-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?))$");

    public static void parseRequestParams(@NonNull RequestBuilder requestBuilder,
                                          @NonNull Context context,
                                          @Nullable AttributeSet attrs,
                                          int defStyleAttr) {
        requestBuilder.setPriceFloorParams(ParamsHelper.getPriceFloorParams(context, attrs, defStyleAttr));
        requestBuilder.setTargetingParams(ParamsHelper.getTargetingParams(context, attrs, defStyleAttr));
    }

    public static PriceFloorParams getPriceFloorParams(@NonNull Context context,
                                                       @Nullable AttributeSet attrs,
                                                       int defStyleAttr) {
        final TypedArray params = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.PriceFloorParams, defStyleAttr, 0);
        try {
            PriceFloorParams priceFloorParams = new PriceFloorParams();
            String stringPriceFloors = params.getString(R.styleable.PriceFloorParams_priceFloors);
            if (stringPriceFloors != null) {
                String[] priceFloors = stringPriceFloors.split(",");
                for (String variable : priceFloors) {
                    variable = variable.trim();
                    if (!TextUtils.isEmpty(variable) && !",".equals(variable)) {
                        String[] onePriceFloor = variable.split("-");
                        if (onePriceFloor.length == 2
                                && !TextUtils.isEmpty(onePriceFloor[0])
                                && !TextUtils.isEmpty(onePriceFloor[1])
                                && !"-".equals(onePriceFloor[0])
                                && !"-".equals(onePriceFloor[1])) {
                            priceFloorParams.addPriceFloor(onePriceFloor[0], Double.parseDouble(onePriceFloor[1]));
                        } else if (onePriceFloor.length == 1
                                && !TextUtils.isEmpty(onePriceFloor[0])) {
                            priceFloorParams.addPriceFloor(Double.parseDouble(onePriceFloor[0]));
                        }
                    }
                }
            }
            return priceFloorParams;
        } finally {
            params.recycle();
        }
    }

    public static TargetingParams getTargetingParams(@NonNull Context context,
                                                     @Nullable AttributeSet attrs,
                                                     int defStyleAttr) {
        final TypedArray params = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.TargetParams, defStyleAttr, 0);
        try {
            TargetingParams targetingParams = new TargetingParams();
            if (params.hasValue(R.styleable.TargetParams_targetUserId))
                targetingParams.setUserId(params.getString(R.styleable.TargetParams_targetUserId));
            if (params.hasValue(R.styleable.TargetParams_targetGender))
                targetingParams.setGender(Gender.fromInt(params.getInt(R.styleable.TargetParams_targetGender, 0)));
            if (params.hasValue(R.styleable.TargetParams_targetBirthdayYear)) {
                final int birthdayYear = params.getInteger(R.styleable.TargetParams_targetBirthdayYear, -1);
                if (Utils.isYearValid(birthdayYear)) {
                    targetingParams.setBirthdayYear(birthdayYear);
                } else {
                    Logger.log("Wrong Birthday Year: should be 4-digit integer, more or equal 1900 and less or equal than current year");
                }
            }
            if (params.hasValue(R.styleable.TargetParams_targetCountry))
                targetingParams.setCountry(params.getString(R.styleable.TargetParams_targetCountry));
            if (params.hasValue(R.styleable.TargetParams_targetCity))
                targetingParams.setCity(params.getString(R.styleable.TargetParams_targetCity));
            if (params.hasValue(R.styleable.TargetParams_targetZip))
                targetingParams.setZip(params.getString(R.styleable.TargetParams_targetZip));
            if (params.hasValue(R.styleable.TargetParams_targetKeywords)) {
                String targetKeywords = params.getString(R.styleable.TargetParams_targetKeywords);
                if (targetKeywords != null) {
                    ArrayList<String> outList = tmpStringList.get();
                    outList.clear();
                    String[] keywords = targetKeywords.split(",");
                    for (String variable : keywords) {
                        variable = variable.trim();
                        if (!TextUtils.isEmpty(variable) && !",".equals(variable)) {
                            outList.add(variable);
                        }
                    }
                    targetingParams.setKeywords(outList.toArray(new String[0]));
                }
            }
            //TODO: not tested
            if (params.hasValue(R.styleable.TargetParams_deviceLocation)) {
                final String targetLocation = params.getString(R.styleable.TargetParams_deviceLocation);
                if (!TextUtils.isEmpty(targetLocation)) {
                    Matcher matcher = locationPattern.matcher(targetLocation);
                    if (matcher.matches()) {
                        MatchResult matchResult = matcher.toMatchResult();
                        double latitude = Double.valueOf(matchResult.group(1));
                        double longitude = Double.valueOf(matchResult.group(7));
                        Location targetingLocation = new Location("userSpecified");
                        targetingLocation.setLatitude(latitude);
                        targetingLocation.setLongitude(longitude);
                        targetingLocation.setTime(System.currentTimeMillis() + 1000);
                        targetingParams.setDeviceLocation(targetingLocation);
                    }
                }
            }
            if (params.hasValue(R.styleable.TargetParams_targetStoreUrl))
                targetingParams.setStoreUrl(params.getString(R.styleable.TargetParams_targetStoreUrl));
            if (params.hasValue(R.styleable.TargetParams_targetPaid))
                targetingParams.setPaid(params.getBoolean(R.styleable.TargetParams_targetPaid, false));
            return targetingParams;
        } finally {
            params.recycle();
        }
    }

}