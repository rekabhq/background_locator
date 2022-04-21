THIS FORK IS MEANT FOR PERSONAL USE ONLY, I DO NOT PLAN TO MAINTAIN IT IN THE FUTURE
I'm just trying to make it work for me with fixed reboot and fixed callbacks...

For now, the example seems broken, I saw a pull request on the main repo about it, I don't know if I'll take time to make it work.

In order to use it in your project, paste this in your pubspec.yaml in dependencies :
```
  background_locator:
    git:
      url: https://github.com/Yukams/background_locator_fixed
      ref: master
```


# background_locator ![](https://github.com/rekab-app/background_locator/workflows/build/badge.svg) [![pub package](https://img.shields.io/pub/v/background_locator.svg)](https://pub.dartlang.org/packages/background_locator) ![](https://img.shields.io/github/contributors/rekab-app/background_locator) ![](https://img.shields.io/github/license/rekab-app/background_locator)

A Flutter plugin for getting location updates even when the app is killed.

![demo](https://raw.githubusercontent.com/RomanJos/background_locator/master/demo.gif)

Refer to [wiki](https://github.com/rekab-app/background_locator/wiki) page for install and setup instruction or jump to specific subject with below links:

* [Installation](https://github.com/rekab-app/background_locator/wiki/Installation)
* [Setup](https://github.com/rekab-app/background_locator/wiki/Setup)
* [How to use](https://github.com/rekab-app/background_locator/wiki/How-to-use)
* [Use other plugins in callback](https://github.com/rekab-app/background_locator/wiki/Use-other-plugins-in-callback)
* [Stop on app terminate](https://github.com/rekab-app/background_locator/wiki/Stop-on-app-terminate)
* [LocationSettings options](https://github.com/rekab-app/background_locator/wiki/LocationSettings-options)
* [Restart service on device reboot (Android only)](https://github.com/rekab-app/background_locator/wiki/Restart-service-on-device-reboot)

##  License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Contributor
Thanks to all who contributed on this plugin to fix bugs and adding new feature, including:
* [Gerardo Ibarra](https://github.com/gpibarra)
* [RomanJos](https://github.com/RomanJos)
* [Marcelo Henrique Neppel](https://github.com/marceloneppel)
