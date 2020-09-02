package io.bidmachine;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.adcom.Context;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class ProtobufTest {

    @Test
    public void protoTest() throws IOException {
        Context.App.Content extContent = Context.App.Content.newBuilder()
                .setId("TestId123")
                .setAlbum("TestAlbum123")
                .setArtist("TestArtist123").build();
        Context.App.Publisher publisher = Context.App.Publisher.newBuilder()
                .addExtProto(Any.pack(extContent)).build();
        Context.App app = Context.App.newBuilder().setPub(publisher).build();
        Context context = Context.newBuilder().setApp(app).build();

        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        context.writeTo(outstr);
        Context context2 = Context.parseFrom(outstr.toByteArray());
        if (context2.getApp().getPub().getExtProto(0).is(Context.App.Content.class)) {
            Context.App.Content newExtContent = context2.getApp().getPub().getExtProto(0).unpack(Context.App.Content.class);
            Assert.assertNotSame(extContent, newExtContent);
        } else {
            fail("Wrong class instance");
        }
    }

}
