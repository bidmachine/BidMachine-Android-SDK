package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.bidmachine.models.IPriceFloorParams;
import io.bidmachine.models.RequestParams;

public final class PriceFloorParams extends RequestParams implements IPriceFloorParams<PriceFloorParams> {

    private Map<String, Double> priceFloorsMap;

    @Nullable
    Map<String, Double> getPriceFloors() {
        return priceFloorsMap;
    }

    @Override
    public PriceFloorParams addPriceFloor(double price) {
        addPriceFloor(UUID.randomUUID().toString(), price);
        return this;
    }

    @Override
    public PriceFloorParams addPriceFloor(String id, double price) {
        if (priceFloorsMap == null) {
            priceFloorsMap = new HashMap<>();
        }
        priceFloorsMap.put(id, price);
        return this;
    }

    @Override
    public void merge(@NonNull RequestParams instance) {
        //ignore
    }
}
