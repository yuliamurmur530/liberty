# Liberty

[Русский](README.md) · [Privacy](PRIVACY.md) · [Security](SECURITY.md)

> **Status: public alpha.** Liberty is under open testing. Public source improves auditability, but it is not a security certification and cannot guarantee that the software is free of defects.

Liberty helps separate selected apps from the apps and data in the personal profile. It starts Android's system flow for creating a protected space — a managed work profile. Root is not required because Android enforces the isolation.

Do not add VPN apps to the protected space. Add apps that should be separated from the personal profile, including services where VPN use is undesirable. They cannot obtain the personal profile's app list and use separate local data and accounts.

After creating the space, install the required apps through the Google Play icon carrying the briefcase badge. Liberty does not disable VPN or control routing. On the device used for Liberty's initial testing, protected-profile apps connected directly even while a tunnel was active in the personal profile.

Android application ID: `net.crinoidea.liberty`. Project website: `https://liberty.crinoidea.net`.

## Download and verify the APK

Installable files are published in [GitHub Releases](https://github.com/yuliamurmur530/liberty/releases). Before installing, you can check:

- repository: `yuliamurmur530/liberty`;
- application ID: `net.crinoidea.liberty`;
- APK signing certificate SHA-256: `3c10f7cc83e0868226cef3699ebca9e982730dd298dfcd8f81155ad8f66a96c7`.

A build signed with a different certificate was not released from this repository. GPL-3.0 permits studying and modifying the source under its terms, but it does not grant rights to the Liberty name or logo. See [TRADEMARKS.md](TRADEMARKS.md).

## Current capabilities

- Android-managed work-profile provisioning without root;
- separate app data, accounts, storage, and profile credential;
- installation through the protected-profile Google Play instance;
- cross-profile clipboard, contact search, caller-ID, and notification-listener restrictions;
- local VPN and HTTP-proxy signal check before launching an app through Liberty;
- explicit verification of critical isolation policies before showing a green protection state;
- immediate protected-profile lock and confirmed profile removal;
- Russian and English interface;
- no ads, analytics, telemetry, third-party SDKs, or `INTERNET` permission;
- no `QUERY_ALL_PACKAGES`; Liberty sees only launchable apps visible under Android package-visibility rules;
- backups and cleartext traffic disabled.

## Important limits

Apps inside the same work profile are separated by Android app sandboxes, but may be able to discover one another when Android and their manifests allow it. One ordinary APK cannot create a separate OS profile for every app.

Liberty does not install, stop, hide, or control personal-profile VPN software. Android and device vendors may route or expose VPN state differently. A reliable tunnel bypass requires support from Android or the VPN itself. Liberty locally checks the active protected-profile network before launching an app through its own screen; this is not a universal network firewall.

The system work-profile folder belongs to the device launcher. Liberty cannot pin its layout or intercept every launch from that folder. Install protected apps through the Google Play icon carrying the Android briefcase badge.

Android does not let an ordinary app make screen recordings black while still allowing normal screenshots. Liberty currently blocks neither screenshots nor recordings.

## Developer build

Requirements: JDK 17+ and Android SDK 36.

```bash
./gradlew :app:assembleDebug
./gradlew :app:lintRelease
```

The Gradle distribution is pinned by its SHA-256 checksum, and CI validates the wrapper JAR. Production signing data is read only from environment variables and is never stored in this repository. See [SIGNING.md](SIGNING.md).

The debug APK is intended only for local development and source-code testing. Users and external testers should download the signed `Liberty-<version>-release.apk` from [GitHub Releases](https://github.com/yuliamurmur530/liberty/releases). Releases use a production keystore stored outside this repository; debug certificates are never used for distribution.

## License and contributions

The source code is distributed under GNU GPL v3.0. Forks and modifications are allowed under the license, but must not impersonate the Liberty project. Use Issues for testing feedback and feature proposals. Report vulnerabilities privately as described in [SECURITY.md](SECURITY.md).
