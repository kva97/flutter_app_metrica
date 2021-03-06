# appmetrica_sdk

A Flutter plugin for [Yandex AppMetrica SDK][SITE].

## Plugin implementation status
### Implemented
- [Events](https://appmetrica.yandex.com/docs/mobile-events/concepts/events.html)
- [Profiles](https://appmetrica.yandex.com/docs/mobile-profile/concepts/profile.html)
### Not implemented yet
- [Revenue](https://appmetrica.yandex.com/docs/revenue/concepts/about.html). Coming soon
- [Crashes](https://appmetrica.yandex.com/docs/crashes/about.html)
- [Push notifications](https://appmetrica.yandex.com/docs/push/concepts/about.html)
- Multiple reporters support (with different API keys)
- Deeplinks

## Usage
To use this plugin, add `appmetrica_sdk` as a [dependency in your pubspec.yaml file](https://flutter.dev/platform-plugins/). See demonstration how to use the appmetrica_sdk plugin in example section.

## Attribute app downloads that originate from Apple Search Ads campaigns (iOS only)
Some Appmetrica SDK features, such as retrieve attribution data from user interaction with ads originating from Apple Search Ads campaigns require the [iAd Framework](https://developer.apple.com/documentation/iad) to be enabled. Without this framework Appmetrica SDK cannot collect this information.

To enable the iAd framework:
- In your Xcode project, select your project's target.
- Select the General tab for your target.
- Expand the Linked Frameworks and Libraries section.
- Click + to add a framework.
- Select iAd.framework.

## Documentation
Common documentation available on [AppMetrica official site][DOCUMENTATION].

[SITE]: https://appmetrica.yandex.com "Yandex AppMetrica site"
[DOCUMENTATION]: https://appmetrica.yandex.com/docs/quick-start/concepts/quick-start.html "Yandex AppMetrica documentation"
