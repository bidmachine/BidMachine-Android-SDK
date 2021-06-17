## Overview

This folder contains mediation adapter used to mediate `Tapjoy`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.7.4-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.7.4.7-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.tapjoy/1.7.4.7/)
[<img src="https://img.shields.io/badge/Network%20version-12.8.1-blue">](https://dev.tapjoy.com/sdk-integration/android/)

Add next dependency to you `build.gradle`:

```groovy
repositories {
    google()
    maven {
        name "Tapjoy's maven repo"
        url "https://sdk.tapjoy.com/"
    }
}

dependencies {
    implementation 'io.bidmachine:ads.networks.tapjoy:1.7.4.7'
}
```

Configure `Tapjoy` network:

```java
BidMachine.registerNetworks(
        new TapjoyConfig("YOUR_SDK_KEY")
               .withMediationConfig(AdsFormat.InterstitialVideo, "YOUR_PLACEMENT_NAME")
               .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_PLACEMENT_NAME"));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.