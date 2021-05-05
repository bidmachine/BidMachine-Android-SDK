package io.bidmachine.models;

public interface IPriceFloorParams<SelfType> {

    @SuppressWarnings("UnusedReturnValue")
    SelfType addPriceFloor(double value);

    @SuppressWarnings("UnusedReturnValue")
    SelfType addPriceFloor(String id, double value);

}