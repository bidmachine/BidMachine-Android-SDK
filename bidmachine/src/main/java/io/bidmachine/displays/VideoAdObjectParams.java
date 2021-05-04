package io.bidmachine.displays;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

final class VideoAdObjectParams extends IabAdObjectParams {

    VideoAdObjectParams(Response.Seatbid seatbid, Response.Seatbid.Bid bid, Ad ad) {
        super(seatbid, bid, ad);
    }

}