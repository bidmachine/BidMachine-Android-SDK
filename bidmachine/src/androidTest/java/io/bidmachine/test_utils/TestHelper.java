package io.bidmachine.test_utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.lang.reflect.Field;
import java.util.Map;

public class TestHelper {

    public static Response.Seatbid getDefaultSeatBid(Ad.Display display) {
        Ad ad = Ad.newBuilder()
                .setId("test_id_1")
                .setDisplay(display)
                .build();
        Response.Seatbid.Bid bid = Response.Seatbid.Bid.newBuilder()
                .setId("test_bid_id_1")
                .setPrice(2.34D)
                .setMedia(Any.pack(ad))
                .build();
        return Response.Seatbid
                .newBuilder()
                .addBid(bid)
                .build();
    }

    public static Response.Seatbid getDefaultSeatBid(Ad.Video video) {
        Ad ad = Ad.newBuilder()
                .setId("test_id_1")
                .setVideo(video)
                .build();
        Response.Seatbid.Bid bid = Response.Seatbid.Bid.newBuilder()
                .setId("test_bid_id_1")
                .setPrice(2.34D)
                .setMedia(Any.pack(ad))
                .build();
        return Response.Seatbid
                .newBuilder()
                .addBid(bid)
                .build();
    }

    public static String getTestMraidAdm() {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='utf-8'><meta name='viewport' content='width=device-width, user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1'><title>orientated banner</title><style>html,body {margin: 0;padding: 0;width: 100%;height: 100%;} a .banner {position: absolute;top: 0;left: 0;width: 100%; height: 100%; background-size: cover; background-repeat: no-repeat; background-position: center;} @media screen and (orientation:landscape) { a .banner {background-image: url('http://s3.amazonaws.com/appodeal-campaign-images/development/campaign_images/images/000/001/479/original/10_1024x768.jpg?1474475545');}} @media screen and (orientation:portrait) { a .banner {background-image: url('http://image.cdn-max.appodeal.com/appodeal-campaigns-images/staging/campaign_images/images/000/001/639/original/11_768x1024.jpg?1477398090');}}</style></head><body><a href='appodeal://'><div class='banner'></div></a></body></html>\"";
    }

    public static String getTestVastAdm() {
        return "<VAST version='2.0'><Ad id='preroll-1'><InLine><AdSystem>2.0</AdSystem><AdTitle>5748406</AdTitle><Impression id='blah'></Impression><Creatives><Creative><Linear><Duration>00:00:55</Duration><TrackingEvents></TrackingEvents><VideoClicks><ClickThrough id='scanscout'><![CDATA[ http://www.appodeal.com ]]></ClickThrough></VideoClicks><MediaFiles><MediaFile height='720' width='1280' bitrate='680' type='video/mp4' delivery='progressive'><![CDATA[ http://image.cdn-max.appodeal.com/appodeal-campaign-images/test_banners/video.mp4 ]]></MediaFile></MediaFiles></Linear></Creative><Creative AdID='Companion'><CompanionAds><Companion width='600' height='400'><HTMLResource><![CDATA[<html> <head> <style> body { margin: 0; padding: 0; }  *:not(input) { -webkit-touch-callout: none; -webkit-user-select: none; -webkit-text-size-adjust: none; } </style> </head> <body> <div align='center'> <a id='appodeal_a' href='appodeal://'></a> <script type='application/javascript'> var density = window.devicePixelRatio; var screenWidth = Math.max(screen.width,screen.height) - 48; var screenHeight = Math.min(screen.width,screen.height) - 48; var metaTag=document.createElement('meta'); metaTag.name = 'viewport'; metaTag.content = 'width=device-width, target-densityDpi=device-dpi'; document.getElementsByTagName('head')[0].appendChild(metaTag); var a = document.getElementById('appodeal_a'); var img = new Image(); img.src = 'http://image.cdn-max.appodeal.com/appodeal-images/appodea-image.png'; img.onload = function() { var imageRatio = this.width/this.height; var screenRatio = screenWidth/screenHeight; if (imageRatio <= screenRatio) { newHeight = screenHeight; newWidth = newHeight * imageRatio; } else { newWidth = screenWidth; newHeight = newWidth / imageRatio; } var appodealInner = document.getElementsByClassName('appodeal-inner'); if (newWidth>this.width*density && newHeight>this.height*density) { newWidth = this.width*density; newHeight = this.height*density; } if (appodealInner.length > 0) { appodealInner[0].style.width = newWidth; appodealInner[0].style.height = newHeight; } img.style.maxWidth = newWidth; img.style.height = newHeight; img.style.maxHeight = newHeight; a.appendChild(img); }; </script> </div> </body> </html>]]></HTMLResource><CompanionClickThrough><![CDATA[appodeal://]]></CompanionClickThrough></Companion></CompanionAds></Creative></Creatives></InLine></Ad></VAST>";
    }

    public static String getAdFoxTestVastAdm() {
        return "<?xml version='1.0' encoding='UTF-8'?><VAST version='2.0'><Ad id='189486'> <InLine><Error></Error><Creatives><Creative AdID='189486'><Linear><Duration>00:00:28</Duration><VideoClicks><ClickThrough><![CDATA[ https://share.virool.com/share/gotosite/campaign/189486/368960?site_key=8ea5a17&amp;uuid=b89de0aa-165f-417a-9e30-b70ac599e846&amp;meta=virtual_site%3Aforbes.com&amp;]]></ClickThrough><ClickTracking><![CDATA[ http://track.virool.com/click?img=1&amp;track_token=b89de0aa-165f-417a-9e30-b70ac599e846]]></ClickTracking></VideoClicks> <MediaFiles> <MediaFile width='640' height='360' type='video/mp4' delivery='progressive'><![CDATA[http://content.adfox.ru/150305/adfox/205544/801069_1.mp4]]></MediaFile></MediaFiles></Linear></Creative></Creatives></InLine></Ad></VAST>";
    }

    public static ViewGroup findCurrentActivityContainer() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            if (activityThread == null) {
                return null;
            }

            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<?, ?> activities = (Map<?, ?>) activitiesField.get(activityThread);
            if (activities == null || activities.isEmpty()) {
                return null;
            }

            for (Object activityRecord : activities.values()) {
                try {
                    Class activityRecordClass = activityRecord.getClass();

                    Field activityInfoField = activityRecordClass.getDeclaredField("activityInfo");
                    activityInfoField.setAccessible(true);

                    ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(activityRecord);
                    if (activityInfo == null) {
                        continue;
                    }

                    Field pausedField = activityRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (!pausedField.getBoolean(activityRecord)) {
                        Field activityField = activityRecordClass.getDeclaredField("activity");
                        activityField.setAccessible(true);

                        final Activity activity = (Activity) activityField.get(activityRecord);
                        return activity.findViewById(android.R.id.content);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static View findViewByCondition(View view, Condition condition) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View findView = findViewByCondition(viewGroup.getChildAt(i), condition);
                    if (findView != null) {
                        return findView;
                    }
                }
            } else if (condition.check(view)) {
                return view;
            }
        }

        return null;
    }

    public static Rect getRect(View view) {
        Rect rect = new Rect();
        if (view != null) {
            view.getGlobalVisibleRect(rect);
        }
        return rect;
    }

    public static Rect getRect(Activity activity) {
        Rect rect = new Rect();
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
            }
        }
        return rect;
    }

}
