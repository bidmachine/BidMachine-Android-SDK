## Overview

This folder contains mediation adapter used to mediate `Amazon`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.6.1-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.6.1.4-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.amazon/1.6.1.4/)
[<img src="https://img.shields.io/badge/Network%20version-8.4.1-blue">](https://ams.amazon.com/webpublisher/uam/docs/mobile-integration-documentation/other-ad-server-integration.html)

Add next configuration to your app level `build.gradle`:

```groovy
android {
    //... other configuration options
    
    compileOptions {
        sourceCompatibility 1.8
        targetCompatibility 1.8
    }
}
```

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.amazon:1.6.1.4'
}
```

Configure `Amazon` network:

```java
BidMachine.registerNetworks(
        new AmazonConfig("YOUR_APP_KEY")
               .withMediationConfig(AdsFormat.Banner_320x50, "YOUR_SLOT_UUID")
               .withMediationConfig(AdsFormat.Banner_300x250, "YOUR_SLOT_UUID")
               .withMediationConfig(AdsFormat.Banner_728x90, "YOUR_SLOT_UUID")
               .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_SLOT_UUID")
               .withMediationConfig(AdsFormat.InterstitialVideo, "YOUR_SLOT_UUID"));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.