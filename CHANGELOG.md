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
