## Version 1.7.2 (April 20, 2021)
**Features**:
* SDK improvement
* Added API method ```AdRequest.Builder#setBidPayload``` to set BidPayload
* Added support for OMSDK version 1.3.15

## Version 1.7.1 (March 30, 2021)
**Features**:
* SDK improvement
* Added API method ```AdRequest#destroy``` to destroy AdRequest
* Deprecated client-side rounding, use server side rounding
* Deprecated 3rd party helpers, use adapter helpers

## Version 1.6.4 (February 12, 2021)
**Features**:
* Improved visibility tracker

## Version 1.6.3 (January 18, 2021)
**Features**:
* Banner improvement

## Version 1.6.2 (December 17, 2020)
**Features**:
* Added support TCF 2.0 - ```IABTCF_TCString``` and ```IABTCF_gdprApplies```

## Version 1.6.1 (November 3, 2020)
**Features**:
* SDK improvement

## Version 1.6.0 (September 11, 2020)
**Features**:
* SDK improvement
* Added API method ```BidMachine.setUSPrivacyString``` to set US privacy string
* Added API method ```AdRequest.Builder#setNetworks``` to set certain networks for AdRequest
* Added API method ```AdRequest.Builder#setSessionAdParams``` to set additional session parameters for AdRequest
* Updated AdColony adapter version to 1.6.0.5. More info [here](adapters/adcolony/CHANGELOG.md)
* Updated Amazon adapter version to 1.6.0.3. More info [here](adapters/amazon/CHANGELOG.md)
* Updated Facebook adapter version to 1.6.0.4. More info [here](adapters/facebook/CHANGELOG.md)
* Updated MyTarget adapter version to 1.6.0.3. More info [here](adapters/my_target/CHANGELOG.md)
* Updated Tapjoy adapter version to 1.6.0.4. More info [here](adapters/tapjoy/CHANGELOG.md)

## Version 1.5.2 (August 3, 2020)
**Features**:
* SDK improvement
* Updated AdColony adapter version to 1.5.2.4. More info [here](https://github.com/bidmachine/BidMachine-Android-SDK/blob/master/adapters/adcolony/CHANGELOG.md)
* Updated Criteo adapter version to 1.5.2.4. More info [here](https://github.com/bidmachine/BidMachine-Android-SDK/blob/master/adapters/criteo/CHANGELOG.md)

## Version 1.5.1 (July 15, 2020)
**Features**:
* Native ad improvement

## Version 1.5.0 (July 10, 2020)
**Features**:
* SDK improvement
* Combining BidMachineFetcher and BidMachineHelper. Use BidMachineFetcher instead BidMachineHelper
* Added API method ```AdRequest.Builder#setLoadingTimeOut``` to set loading timeout for AdRequest
* Added API method ```AdRequest#notifyMediationWin``` and ```AdRequest#notifyMediationLoss``` to notify BidMachine of win/loss if you use BidMachine like in-house mediation

## Version 1.4.4 (May 22, 2020)
**Features**:
* Updated Amazon version to 8.3.0
* Updated Criteo version to 3.5.0

## Version 1.4.3 (April 9, 2020)
**Features**:
* Added support CCPA based on IAB. More info [here](https://github.com/InteractiveAdvertisingBureau/USPrivacy/blob/master/CCPA/Version%201.0/USP%20API.md)
* Updated AdColony version to 4.1.0
* Updated Facebook version to 5.7.1
* Updated Tapjoy version to 12.4.2

## Version 1.4.1 (January 21, 2020)
**Features**:
* Added API method ```BidMachine.setPublisher``` to set publisher information
* Added API methods ```Builder#disableHeaderBidding``` and ```Builder#enableHeaderBidding``` to AdRequest to disable/enable header bidding on server side

## Version 1.4.0 (November 28, 2019)
**Features**:
* Added Native Ad type

## Version 1.3.3 (September 20, 2019)
**Features**:
* Added Amazon network adapter for Header-Bidding
* Added Criteo network adapter for Header-Bidding

## Version 1.3.2 (September 12, 2019)
**Features**:
* Update compatibility with Google's Protobuf libraries

## Version 1.3.1 (September 4, 2019)
**Features**:
* Added Mintegral network adapter for Header-Bidding
* Update proguard rules

## Version 1.3.0 (August 14, 2019)
**Features**:
* Support of 3d party Ad networks adapters for Header-Bidding that work with BidMachine via SDK
* Possibility to change endpoint
* Support of GDPR settings exchange with SharedPreferences (you can found more info [here](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/Mobile%20In-App%20Consent%20APIs%20v1.0%20Final.md#how-do-third-party-sdks-vendors-access-the-consent-information-))
