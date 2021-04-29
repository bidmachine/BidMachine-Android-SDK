package io.bidmachine;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/test/AndroidManifest.xml", sdk = 21)
public class NetworkAssetManagerTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
    }

    @Test
    public void getNetworkAssetParams_assetsWithValidContent_returnNetworkAssetParams() throws Exception {
        NetworkAssetParams networkAssetParams = NetworkAssetManager
                .getNetworkAssetParams(context, "test_network");

        assertNotNull(networkAssetParams);
        assertEquals("io.bidmachine.TestNetworkConfig", networkAssetParams.getClasspath());
        assertEquals("test_network", networkAssetParams.getName());
        assertEquals("1.1.1.1", networkAssetParams.getVersion());
    }

    @Test
    public void getNetworkAssetParams_assetsWithEmptyContent_returnNull() throws Exception {
        NetworkAssetParams networkAssetParams = NetworkAssetManager
                .getNetworkAssetParams(context, "test_network_empty");

        assertNull(networkAssetParams);
    }

    @Test
    public void getNetworkAssetParams_assetsWithWrongContent_returnNull() throws Exception {
        NetworkAssetParams networkAssetParams = NetworkAssetManager
                .getNetworkAssetParams(context, "test_network_wrong_content");

        assertNull(networkAssetParams);
    }

    @Test
    public void getNetworkAssetParams_assetsWithWrongFieldsContent_returnNull() throws Exception {
        NetworkAssetParams networkAssetParams = NetworkAssetManager
                .getNetworkAssetParams(context, "test_network_empty");

        assertNull(networkAssetParams);
    }

}