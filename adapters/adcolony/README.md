## Overview

This folder contains mediation adapter used to mediate `AdColony`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.7.5-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.7.5.9-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.adcolony/1.7.5.9/)
[<img src="https://img.shields.io/badge/Network%20version-4.5.0-blue">](https://github.com/AdColony/AdColony-Android-SDK)

Add next dependency to you `build.gradle`:

```groovy
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation 'io.bidmachine:ads.networks.adcolony:1.7.5.9'
}
```

Configure `AdColony` network:

```java
BidMachine.registerNetworks(
        new AdColonyConfig("YOUR_APP_ID")
                .withMediationConfig(AdsFormat.InterstitialVideo, "YOUR_ZONE_ID")
                .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_ZONE_ID"));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.