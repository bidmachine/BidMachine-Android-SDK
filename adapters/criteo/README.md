## Overview

This folder contains mediation adapter used to mediate `Criteo`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.7.1-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.7.1.8-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.criteo/1.7.1.8/)
[<img src="https://img.shields.io/badge/Network%20version-4.2.1-blue">](https://publisherdocs.criteotilt.com/app/android/get-started/)

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.criteo:1.7.1.8'
}
```

Configure `Criteo` network:

```java
BidMachine.registerNetworks(
        new CriteoConfig("YOUR_PUBLISHER_ID")
                .withMediationConfig(AdsFormat.Banner_320x50, "YOUR_AD_UNIT_ID")
                .withMediationConfig(AdsFormat.Banner_300x250, "YOUR_AD_UNIT_ID")
                .withMediationConfig(AdsFormat.Banner_728x90, "YOUR_AD_UNIT_ID")
                .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_AD_UNIT_ID")
                .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_AD_UNIT_ID", Orientation.Portrait)
                .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_AD_UNIT_ID", Orientation.Landscape));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.