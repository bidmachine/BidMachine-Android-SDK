## Overview

This folder contains mediation adapter used to mediate `Pangle`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.7.5-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.7.5.1-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.pangle/1.7.5.1/)
[<img src="https://img.shields.io/badge/Network%20version-3.5.1.0-blue">](https://www.pangleglobal.com/support/doc/6034a663511c57004360ff0f)

Add next dependency to you `build.gradle`:

```groovy
repositories {
    maven {
        url 'https://artifact.bytedance.com/repository/pangle'
    }
}

dependencies {
    implementation 'io.bidmachine:ads.networks.pangle:1.7.5.1'
}
```

Configure `Pangle` network:

```java
BidMachine.registerNetworks(
        new PangleConfig("YOUR_APP_ID")
                .withMediationConfig(AdsFormat.Banner, "YOUR_SLOT_ID")
                .withMediationConfig(AdsFormat.Interstitial, "YOUR_SLOT_ID")
                .withMediationConfig(AdsFormat.Rewarded, "YOUR_SLOT_ID"));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.