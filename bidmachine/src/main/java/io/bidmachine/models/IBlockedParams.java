package io.bidmachine.models;

public interface IBlockedParams<SelfType> {

    /**
     * Adds category of content you want to block using IAB categories
     *
     * @param category Block list category ID of content
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType addBlockedAdvertiserIABCategory(String category);

    /**
     * Adds advertiser domain (e.g., “example.com”) you want to block
     *
     * @param domain Advertiser domain (e.g., “example.com”) that will be blocked
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType addBlockedAdvertiserDomain(String domain);

    /**
     * Adds advertised app you want to block
     *
     * @param bundleOrPackage App bundle or package you want to block
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType addBlockedApplication(String bundleOrPackage);

}
