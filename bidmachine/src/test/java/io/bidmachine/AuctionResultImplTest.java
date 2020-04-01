package io.bidmachine;

import com.explorestack.protobuf.adcom.Ad;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AuctionResultImplTest {

    @Test
    public void identifyCreativeFormat_withEmptyAd_creativeFormatIsNull() {
        Ad ad = Ad.newBuilder()
                .build();
        CreativeFormat creativeFormat = AuctionResultImpl.identifyCreativeFormat(ad);
        assertNull(creativeFormat);
    }

    @Test
    public void identifyCreativeFormat_containsEmptyDisplay_creativeFormatIsNull() {
        Ad.Display display = Ad.Display.newBuilder()
                .build();
        Ad ad = Ad.newBuilder()
                .setDisplay(display)
                .build();
        CreativeFormat creativeFormat = AuctionResultImpl.identifyCreativeFormat(ad);
        assertNull(creativeFormat);
    }

    @Test
    public void identifyCreativeFormat_containsDisplayWithAdm_creativeFormatIsBanner() {
        Ad.Display display = Ad.Display.newBuilder()
                .setAdm("test_adm")
                .build();
        Ad ad = Ad.newBuilder()
                .setDisplay(display)
                .build();
        CreativeFormat creativeFormat = AuctionResultImpl.identifyCreativeFormat(ad);
        assertEquals(CreativeFormat.Banner, creativeFormat);
    }

    @Test
    public void identifyCreativeFormat_containsDisplayWithBanner_creativeFormatIsBanner() {
        Ad.Display.Banner banner = Ad.Display.Banner.newBuilder()
                .build();
        Ad.Display display = Ad.Display.newBuilder()
                .setBanner(banner)
                .build();
        Ad ad = Ad.newBuilder()
                .setDisplay(display)
                .build();
        CreativeFormat creativeFormat = AuctionResultImpl.identifyCreativeFormat(ad);
        assertEquals(CreativeFormat.Banner, creativeFormat);
    }

    @Test
    public void identifyCreativeFormat_containsDisplayWithNative_creativeFormatIsNative() {
        Ad.Display.Native nativeAd = Ad.Display.Native.newBuilder()
                .build();
        Ad.Display display = Ad.Display.newBuilder()
                .setNative(nativeAd)
                .build();
        Ad ad = Ad.newBuilder()
                .setDisplay(display)
                .build();
        CreativeFormat creativeFormat = AuctionResultImpl.identifyCreativeFormat(ad);
        assertEquals(CreativeFormat.Native, creativeFormat);
    }

    @Test
    public void identifyCreativeFormat_containsVideo_creativeFormatIsVideo() {
        Ad.Video video = Ad.Video.newBuilder()
                .build();
        Ad ad = Ad.newBuilder()
                .setVideo(video)
                .build();
        CreativeFormat creativeFormat = AuctionResultImpl.identifyCreativeFormat(ad);
        assertEquals(CreativeFormat.Video, creativeFormat);
    }

}