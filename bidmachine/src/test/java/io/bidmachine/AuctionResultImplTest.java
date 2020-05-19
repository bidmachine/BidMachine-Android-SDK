package io.bidmachine;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import io.bidmachine.models.AuctionResult;
import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.protobuf.AppExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AuctionResultImplTest {

    @Test
    public void createCustomParams() {
        AdExtension adExtension1 = AdExtension.newBuilder()
                .putCustomParams("custom_key_1", "custom_value_1")
                .putCustomParams("custom_key_2", "custom_value_2")
                .putCustomParams("custom_key_3", "custom_value_3")
                .build();
        AdExtension adExtension2 = AdExtension.newBuilder()
                .putCustomParams("custom_key_4", "custom_value_4")
                .putCustomParams("custom_key_5", "custom_value_5")
                .putCustomParams("custom_key_6", "custom_value_6")
                .build();
        AdExtension adExtension3 = AdExtension.newBuilder()
                .putCustomParams("custom_key_2", "new_custom_value_2")
                .putCustomParams("custom_key_4", "new_custom_value_4")
                .build();
        AppExtension notAdExtension = AppExtension.newBuilder().build();
        Ad ad = Ad.newBuilder()
                .addExt(Any.pack(adExtension1))
                .addExt(Any.pack(notAdExtension))
                .addExt(Any.pack(adExtension2))
                .addExt(Any.pack(adExtension3))
                .addExt(Any.pack(adExtension3))
                .build();
        AuctionResult auctionResult = new AuctionResultImpl(
                Response.Seatbid.newBuilder().build(),
                Response.Seatbid.Bid.newBuilder().build(),
                ad,
                mock(NetworkConfig.class));
        Map<String, String> customParams = auctionResult.getCustomParams();
        assertEquals(6, customParams.size());
        assertEquals("custom_value_1", customParams.get("custom_key_1"));
        assertEquals("new_custom_value_2", customParams.get("custom_key_2"));
        assertEquals("custom_value_3", customParams.get("custom_key_3"));
        assertEquals("new_custom_value_4", customParams.get("custom_key_4"));
        assertEquals("custom_value_5", customParams.get("custom_key_5"));
        assertEquals("custom_value_6", customParams.get("custom_key_6"));
    }

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