# BidMachine Android SDK

__Get more information about SDK integration and usage in our Wiki:__

__[BidMachine Android SDK Documentation](https://wiki.appodeal.com/display/BID/BidMachine+Android+SDK+Documentation)__

## Integration
[<img src="https://img.shields.io/badge/SDK%20version-1.7.0-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads/)

Add this to Module-level or App-level `build.gradle` before dependencies:

```groovy
repositories {
    // ... other project repositories
    maven {
        name 'BidMachine maven repo'
        url 'https://artifactory.bidmachine.io/bidmachine'
    }
}
```

Add next dependency to you build.gradle:

```groovy
dependencies {
    // ... other project dependencies
    implementation 'io.bidmachine:ads:1.7.0'
}
```

## Network security configuration

[Android 9.0 (API 28) blocks cleartext (non-HTTPS) traffic by default](https://developer.android.com/training/articles/security-config), which can prevent ads from serving correctly.

Add a Network Security Configuration file to your AndroidManifest.xml:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest>
    <application
        ...
        android:networkSecurityConfig="@xml/network_security_config"
        ... >
    </application>
</manifest>
```

In your `network_security_config.xml` file, add base-config that sets cleartextTrafficPermitted to true:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

## Initialization

Initialize SDK, and set your `SellerId`:

```java
BidMachine.initialize(Context, YOUR_SELLER_ID);
```

> To get your SELLER_ID, visit [our website](https://bidmachine.io/) or contact the support.

## Header-Bidding

3rd party networks which can be used for Header-Bidding can be find [here](adapters)

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.