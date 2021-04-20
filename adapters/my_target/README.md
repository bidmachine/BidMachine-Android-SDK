## Overview

This folder contains mediation adapter used to mediate `myTarget`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.7.2-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.7.2.6-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.my_target/1.7.2.6/)
[<img src="https://img.shields.io/badge/Network%20version-5.12.2-blue">](https://github.com/myTargetSDK/mytarget-android)

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.my_target:1.7.2.6'
}
```

Configure `myTarget` network:

```java
BidMachine.registerNetworks(
        new MyTargetConfig()
               .withMediationConfig(AdsFormat.Banner, "YOUR_SLOT_ID")
               .withMediationConfig(AdsFormat.Banner_320x50, "YOUR_SLOT_ID")
               .withMediationConfig(AdsFormat.Banner_300x250, "YOUR_SLOT_ID")
               .withMediationConfig(AdsFormat.Banner_728x90, "YOUR_SLOT_ID")
               .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_SLOT_ID")
               .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_SLOT_ID");
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.