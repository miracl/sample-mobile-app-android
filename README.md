# sample-mobile-app-android

* **category**: Samples
* **copyright**: 2019 MIRACL UK LTD
* **link**: https://github.com/miracl/sample-mobile-app-android


## Contents
This repository contains simple demonstration mobile apps which make use of our mobile Android SDK.

* **In App login** - located in folder [InAppLoginSample](InAppLoginSample/README.md).
This flow is used to login into the mobile app itself.

* **Webside login** - located in folder [WebsiteLoginSample](WebsiteLoginSample/README.md).
This flow is used to log into another app using the mobile app(the oidc flow).

* **DVS** - located in folder [DvsSample](DvsSample/README.md).
This flow is used to configure a 'Designated Verifier Signature' app whereby the MIRACL Trust authentication server can issue secret signing keys to users and allow them to verify their transactions with multi-factor signatures.

All samples use [mpinsdk.aar](https://github.com/miracl/mfa-client-sdk-android) which is located in the mpinsdk directory of the checked out repository.

Instructions on how to build and run each samples can be found in the README located in the folder for each sample.