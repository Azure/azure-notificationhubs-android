# com.microsoft.windowsazure.messaging.notificationhubs

## Overview

The code in this namespace facilitates interactions with Azure Notification Hubs using [Installations](https://docs.microsoft.com/en-us/azure/notification-hubs/notification-hubs-push-notification-registration-management#installations). It is the more modern way to use Azure Notification Hubs, and should be preferred for new projects using this SDK.

## Add to Your Project

See the [front-page README](../../../../../../../../../README.md) for instructions on how to add this package to your project.

## Scenarios

### Connect to Your Notification Hub

To get started with the simplest scenario, you only need a single line of code:
``` Java
// From your MainActivity
NotificationHub.start(this.getApplication(), "{your-hub-name}", "{your-listenonly-access-policy}");
```

With only the line above, your app will receive notification messages from Firebase Cloud Messaging in the System Tray when your app is in the background.

To have your code alerted to data messages anytime, or noitification messages when your app is in the foreground, you'll just need a second clause:

``` Java
NotificationHub.setListener(new NotificationListener(){
    @Override
    public void onPushNotificationReceived(Context context, NotificationMessage message) {
        // Render the notification, fire an Intent, update device settings, whatever your application needs.
    }
});
```

To better understand when Android informs apps of notifications, read more [here](https://firebase.google.com/docs/cloud-messaging/android/receive#handling_messages). Notably, this SDK has implemented `FirebaseMessagingService` for you, and that implementation will automatically be merged into your `AndroidManifest.xml`.

### Tag Management

[Tags](https://docs.microsoft.com/en-us/azure/notification-hubs/notification-hubs-tags-segment-push-message#tags) are a versatile and powerful tool, which allow for the targeting of a subset of your users. Using our backend SDKs or REST APIs, one can target all Installations that have a particular tag applied, or use a Tag Expression to further pare down the targeted users.

Adding and removing tags is simple and easy. Consider the following code, which would update which teams an ice-hockey fan wants to receive notifications for:

``` Java
NotificationHub.addTag("seattleKraken");
NotificationHub.removeTag("anaheimDucks");
```

### Template Mangement

[Templates](https://docs.microsoft.com/en-us/azure/notification-hubs/notification-hubs-templates-cross-platform-push-messages) reverses control of the Notification body from the backend sending notifiations, instead giving that control to the client. This can be used to localize notifications based on user prefrences, or even to abstract away platform details.

To use templates, add them to your Installation associated with a particular name:

``` Java
String krakenScore = "{
    \"notification\":{
        \"title\": \"Seattle Kraken Goal!\",
        \"body\": \"The Kraken scored on the $(time) minute of the $(period) period!\"
    }
}";
NotificationHub.setTemplate("kraken-score", krakenScore);
```

Simultaneously, on a separate device, you could register the same notification but in Spanish. Register it with the same name, `"kraken-score"`. When the backend is alerted, it need only send one notification, to all people interested in the Seattle Kraken, instead of needing to send separate notifications for Spanish speaking users and English speaking users. Do the same with Android and iOS users. Using templates will dramatically reduce the matrix of concerns felt by your backend.

### Listening for Installation Update Events

The NotificationHub global single instance is always waiting in the background, ready to update the backend when device details change. By default, a log entry will be written any time one of these events occurs. If there's alternate behavior you would like to incorporate, just let the NotificationHub global single instance know:

``` Java
NotificationHub.setInstallationSavedListener(new InstallationAdapter.Listener(){
    @Override
    public void onInstallationSaved(Installation installation){
        // Log or visualize the updated installation as you please.
    }
})

NotificationHub.setInstallationSaveFailureListener(new InstallationAdapter.ErrorListener(){
    @Override
    public void onInstallationSaveError(Exception e) {
        // Log or visualize the error saving the installation. Did you try to add an invalid tag?
    }
})
```

### Saving Installations to a Custom Backend

Most customers will want to use Azure Notification Hubs to store the records of all of the devices using their application. However, if a developer is tracking all of their devices in their own database, and relaying notifications using [direct-send](https://docs.microsoft.com/en-us/rest/api/notificationhubs/direct-send), the SDK can be configured to call whichever backend is desired.

The contract is simple, just implement the `InstallationAdapter` interface. The only method in the the interface is `saveInstallation`:

``` Java
// From your MainActivity:

NotificationHub.start(this.getApplication(), new InstallationAdapter(){
    @Override
    public void saveInstallation(final Installation installation, final Listener onSuccess, final ErrorListener onFailure) {
        try {
            // Call your backend with `installation`'s details
            onSuccess.onInstallationSaved(installation);
        } catch (Exception e) {
            onFailure.onInstallationSaveError(e);
        }
    }
});
```

### Enriching Installations

As your application starts, when the network becomes available, or anytime the SDK detects a change, this SDK will build up a new instance of Installation and send it to the backend. As the installation is built up, all of the most recent data stored by the NotificationHub global single instance will be applied to it. This includes: tags, templates, the device's unique push address, etc. You can add to the set of transforms that will be applied to the Installation by providing implementations of `InstallationVisitor`.

The following would add a tag to each Installation before it is sent to the backend, indicating which platform the app is built upon.

``` Java
NotificationHub.useVisitor(new InstallationVisitor(){
    @Override
    public void visitInstallation(Installation installation) {
        installation.addTag("platform_Android");
    }
})
```

This approach allows you to avoid cluttering the tags that are stored by the `NotificationHub` global single instance. Another perk is giving you the ability to inspect the current device state periodically as the Installation is updated.
