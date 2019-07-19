# sample-mobile-app-android

* **category**: Samples
* **copyright**: 2019 MIRACL Technologies LTD
* **link**: https://github.com/miracl/sample-mobile-app-android


## Contents
This repository contains simple demonstration mobile apps which make use of our mobile Android SDK.

* **Mobile App login** - located in folder [MobileAppLoginSample](MobileAppLoginSample/README.md).
This flow is used to login into the mobile app itself.

* **Website login** - located in folder [WebsiteLoginSample](WebsiteLoginSample/README.md).
This flow is used to log into another app using the mobile app (the oidc flow).

* **DVS** - located in folder [DvsSample](DvsSample/README.md).
This flow is used to configure a 'Designated Verifier Signature' app whereby the MIRACL Trust authentication server can issue secret signing keys to users and allow them to verify their transactions with multi-factor signatures.

* **Bootstrap** - located in folder [BootstrapSample](BootstrapSample/README.md).

    Bootstrap codes are used to skip the customer verification when the user have already registered identity on another device. There are two flows:
    * Bootstrap Code Registration - use an already generated bootstrap code from another device to transfer an identity to the device skipping the registration verification process
    * Bootstrap Code Generation - generate a bootstrap code for a registered identity that could be used to transfer it to another device

All samples use [mpinsdk.aar](https://github.com/miracl/mfa-client-sdk-android) which is located in the mpinsdk directory of the checked out repository.

Instructions on how to build and run each samples can be found in the README located in the folder for each sample.