package io.bidmachine;

import com.explorestack.protobuf.adcom.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class PublisherTest {

    private Context.App.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = Context.App.newBuilder();
    }

    @Test
    public void build1() {
        Publisher publisher = new Publisher.Builder()
                .setId(null)
                .setName(null)
                .setDomain(null)
                .addCategory(null)
                .addCategories(null)
                .build();
        publisher.build(builder);

        Context.App.Publisher protoPublisher = builder.getPub();
        assertNotNull(protoPublisher);
        assertEquals("", protoPublisher.getId());
        assertEquals("", protoPublisher.getName());
        assertEquals("", protoPublisher.getDomain());
        assertNotNull(protoPublisher.getCatList());
        assertTrue(protoPublisher.getCatList().isEmpty());
    }

    @Test
    public void build2() {
        List<String> categoryList = new ArrayList<>();
        categoryList.add("test_category_1");
        categoryList.add(null);
        categoryList.add("test_category_2");
        categoryList.add("");
        Publisher publisher = new Publisher.Builder()
                .setId("test_id")
                .setName("test_name")
                .setDomain("test_domain")
                .addCategory("test_category")
                .addCategories(categoryList)
                .build();
        publisher.build(builder);

        Context.App.Publisher protoPublisher = builder.getPub();
        assertNotNull(protoPublisher);
        assertEquals("test_id", protoPublisher.getId());
        assertEquals("test_name", protoPublisher.getName());
        assertEquals("test_domain", protoPublisher.getDomain());
        assertNotNull(protoPublisher.getCatList());
        assertEquals(3, protoPublisher.getCatList().size());
        assertEquals("test_category", protoPublisher.getCatList().get(0));
        assertEquals("test_category_1", protoPublisher.getCatList().get(1));
        assertEquals("test_category_2", protoPublisher.getCatList().get(2));
    }

}