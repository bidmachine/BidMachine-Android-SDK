package com.criteo.publisher;

import com.criteo.publisher.model.s;

public class CriteoUtils {

    public static Bid createBidToken() {
        return new Bid(null, null, new s());
    }

}