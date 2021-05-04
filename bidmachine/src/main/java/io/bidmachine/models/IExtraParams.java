package io.bidmachine.models;

public interface IExtraParams<SelfType> {

    @SuppressWarnings("UnusedReturnValue")
    SelfType addExtra(String key, String value);

}