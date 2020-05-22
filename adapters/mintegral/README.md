## Overview

This folder contains mediation adapter used to mediate `Mintegral`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.4.4-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.4.4.2-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.mintegral/1.4.4.2/)
[<img src="https://img.shields.io/badge/Network%20version-9.13.5-blue">](http://cdn-adn.rayjump.com/cdn-adn/v2/markdown_v2/index.html?file=sdk-m_sdk-android&lang=en)

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.mintegral:1.4.4.2'
}
```

Configure `Mintegral` network:

```java
BidMachine.registerNetworks(
        new MintegralConfig()
               .withMediationConfig(AdsFormat.InterstitialVideo, "YOUR_UNIT_ID")
               .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_UNIT_ID", "YOUR_REWARD_ID");
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.