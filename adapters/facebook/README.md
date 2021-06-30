## Overview

This folder contains mediation adapter used to mediate `Facebook`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.7.5-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.7.5.7-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.facebook/1.7.5.7/)
[<img src="https://img.shields.io/badge/Network%20version-6.5.0-blue">](https://developers.facebook.com/docs/android/)

Add next dependency to you `build.gradle`:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.bidmachine:ads.networks.facebook:1.7.5.7'
}
```

Configure `Facebook` network:

```java
BidMachine.registerNetworks(
        new FacebookConfig("YOUR_APP_ID")
                .withMediationConfig(AdsFormat.Banner, "YOUR_PLACEMENT_ID")
                .withMediationConfig(AdsFormat.Banner_300x250, "YOUR_PLACEMENT_ID")
                .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_PLACEMENT_ID")
                .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_PLACEMENT_ID"));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.