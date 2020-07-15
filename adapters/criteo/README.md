## Overview

This folder contains mediation adapter used to mediate `Criteo`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.5.1-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.5.1.3-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.criteo/1.5.1.3/)
[<img src="https://img.shields.io/badge/Network%20version-3.5.0-blue">](https://publisherdocs.criteotilt.com/app/android/get-started/)

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.criteo:1.5.1.3'
}
```

Configure `Criteo` network:

```java
BidMachine.registerNetworks(
        new CriteoConfig("YOUR_PUBLISHER_ID")
               .withMediationConfig(AdsFormat.Banner_320x50, "YOUR_AD_UNIT_ID")
               .withMediationConfig(AdsFormat.Banner_300x250, "YOUR_AD_UNIT_ID")
               .withMediationConfig(AdsFormat.Banner_728x90, "YOUR_AD_UNIT_ID")
               .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_AD_UNIT_ID"));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.