# BboxApi-Bluemix Notification bridge

[![Build Status](https://travis-ci.org/BboxLab/bboxapi-bluemix-starterkit.svg)](https://travis-ci.org/BboxLab/bboxapi-bluemix-starterkit)
[![License](http://img.shields.io/:license-mit-blue.svg)](LICENSE.md)

Android Service running a MQTT notification client between BboxApi and Bluemix platform

![architecture](img/architecture.png)

Following BboxApi notifications are published through MQTT client directly to Bluemix IoT platform : 


| notification types | MQTT Topic |
|--------------------|-------------------|
| APPLICATION         |  "Application" |
| MEDIA     |     "Media"       |
| MESSAGE |     Message         |
| USER_INPUT   |   "UserInterface/RemoteController"      |
| IOT        |    "Iot"    |

## Download source

```
git clone bboxapi-bluemix-starterkit
```

## Setup Bluemix

The following will describe how to create a working IoT Bluemix project step by step from https://console.eu-gb.bluemix.net :

<hr/>

<b>1) Go to "catalog" and choose "Internet of Things Foundation Starter" :</b>

![boilerplate](img/boilerplate.png)
<hr/>

<b>2) Name your "Internet of Things Foundation Starter" application and "create" :</b>

![create_iot_starter_kit](img/create_iot_starter_kit.png)
<hr/>

<b>3) Return to the "catalog" and choose "Internet of Things Foundation" : </b>

![iot_foundation](img/iot_foundation.png)
<hr/>

<b>4) Name your "Internet of Things Foundation" service : </b>

![create_iot_foundation](img/create_iot_foundation.png)
<hr/>

<b>5) Click on "Restage" to restage your app :</b>

![restage](img/restage.png)
<hr/>

<b>6) Go to your project overview :</b>

![overview](img/overview.png)
<hr/>

<b>7) Click on your "Internet of Things Foundation" service :</b>

![click_iot_foundation](img/click_iot_foundation.png)
<hr/>

<b>8) Click on "Launch dashboard" : </b>

![iot_dashboard](img/iot_dashboard.png)
<hr/>

<b>9) On your "Internet of Things Foundation" dashboard, click on "add a device" : </b>

![add_a_terminal](img/add_a_terminal.png)
<hr/>

<b>10) You cant create a device if you havent created device type yet, click on "create a device type" :</b>

![create_terminal_type](img/create_terminal_type.png)
<hr/>

<b>11) Create a type with name "Android" :</b>

![define_android_type](img/define_android_type.png)
<hr/>

<b>12) Then create a device with type "Android" : </b>

![create_terminal](img/create_terminal.png)
<hr/>

<b>13) Name your new device :</b>

![name_device](img/name_device.png)
<hr/>

<b>14) Copy paste somewhere identification data for your device :</b>

![end_add_device](img/end_add_device.png)
<hr/>

## Configure authentication variables

In your `./bbox-bluemix-bridge/build.gradle` you have a few variables to set :

| environnement variable | description |
|--------------------|-------------------|
| BBOXAPI_APP_ID         | application ID relative to your Miami Box  |
| BBOXAPI_APP_SECRET     | application Secret relative to your Miami Box             |
| BLUEMIX_IOT_AUTH_TOKEN | Bluemix Internet of Things auth token (see last step in Setup)              |
| BLUEMIX_IOT_DEVICEID   | Bluemix Internet of Things deviceId you have set              |
| BLUEMIX_IOT_ORG        | Bluemix Internet of Things organization id (at the top in dashboard page)        |

You can either set these variable as environnement variable or you can replace them with their values directly

* `BBOXAPI_APP_ID` and `BBOXAPI_APP_SECRET` are given by Bouygues Télécom. If you dont have these, here is following contact https://dev.bouyguestelecom.fr/dev/?page_id=51

![end_add_device](img/gradle_info.png)

## Build Android app

Build project with Android Studio or your favorite IDE

## Install Service

Note down your Bbox Miami <IP> address.
Connect via adb and install apk :  

```
adb connect <IP>
adb install -r ./bbox-bluemix-bridge/build/outputs/apk/bbox-bluemix-bridge-debug.apk
```

## Launch Service

```
adb shell am startservice  "fr.bouyguestelecom.tv.bridge.bluemix/.BluemixBridgeService" --user 0
```

## Output screenshot

* Client output, publishing message with BBoxApi Notification topic to Bluemix platform

![client_publish_notif](img/client_publish_notif.png)
<hr/>

* Bluemix Node Red Dashboard receiving notifications

![dashboard_log](img/dashboard_log.png)

<i>You can access Node Red dashboard from your <service_url>/red. For instance : http://yourservice.mybluemix.net/red/</i>

<hr/>

## Modify source code dispatching notification

Android application is a service defined in `fr.bouyguestelecom.tv.bridge.bluemix.BluemixBridgeService.java` :

![dashboard_log](img/dispatch_notification.png)

## External Libraries

* <a href="https://github.com/ibm-messaging/iot-starter-for-android">IBM IoT Starter for Android</a>
* <a href="https://github.com/BboxLab/bbox-2ndscreen-android">bbox-2ndscreen library</a>
* <a href="http://www.eclipse.org/paho/">Eclipse Paho MQTT open source client</a>

## License

The MIT License (MIT) Copyright (c) 2015 InnovationLab BboxLab
