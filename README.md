
<p align="center"><a href="https://azure.microsoft.com/en-us/services/notification-hubs/"><img src="./nh-logo.svg?sanitize=true"/></a></p>

# Azure Notification Hubs - Android SDK

Azure Notification Hubs provides a multi-platform, scaled-out push infrastructure that enables you to send mobile push notifications from any backend (in the cloud or on-premises) to any mobile platform. To learn more, visit our [Developer Center](https://azure.microsoft.com/en-us/documentation/services/notification-hubs).


## Getting Started with ANH Android SDK

### Reference with Gradle

This library is published on [JFrog Bintray](https://bintray.com/microsoftazuremobile/SDK/Notification-Hubs-Android-SDK#files/com/microsoft/azure/notification-hubs-android-sdk). [Once you've completed steps 1 through 3 here to setup your app with Firebase](https://firebase.google.com/docs/android/setup#console), adding a reference to this project is as simple as editting two files in your project:

_{project-root}/build.gradle:_

``` groovy
// This is not a complete build.gradle file, it only highlights the portions you'll need to use ANH.

allprojects {
    repositories {
        // Ensure you have the following repsoitory in your "allprojects", "repositories" section.
        maven {
            url 'https://dl.bintray.com/microsoftazuremobile/SDK'
        }
    }
}
```

_{project-root}/{your-module}/build.gradle:_
``` groovy
// This is not a complete build.gradle file, it only highlights the portions you'll need to use ANH.

dependencies {
    // Ensure the following line is included in your app/library's "dependencies" section.
    implementation 'com.microsoft.azure:notification-hubs-android-sdk:v1.0.0-preview3'
}
```

## Repository Contents

### [`com.microsoft.windowsazure.messaging.notificationhubs`](./notification-hubs-sdk/src/main/java/com/microsoft/windowsazure/messaging/notificationhubs)
The code found in this namespace uses Azure Notification Hub's [Installation flow](https://docs.microsoft.com/en-us/azure/notification-hubs/notification-hubs-push-notification-registration-management#installations). It is heavily influenced by App Center Push's SDK. It was designed to do more than just act as a REST client, instead abstracting away your interactions with ANH. New projects that are looking to use Azure Notification Hub should use the constructs found in this namespace.

Some highlights of the functionality provided in this namespace:
- With one line of initialization code, this library will automatically and asynchronously call the Notification Hub backend anytime device details change.
- Easily register a callback to receive Firebase Cloud Messaging notifications.
- Highly customizable pipeline to ensure your device always has the properties
you find most useful.

### [`com.microsoft.windowsazure.messaging`](./notification-hubs-sdk/src/main/java/com/microsoft/windowsazure/messaging)

This is our SDK's legacy codebase. It facilitates interactions with our [Registration flow](https://docs.microsoft.com/en-us/azure/notification-hubs/notification-hubs-push-notification-registration-management#registrations). This namespace contains code that does little more than act as a REST client. Customers newly using Azure Notification Hub should NOT use the code found in this namespace. Expect it to be deprecated.

### [`notification-hubs-test-app-java`](./notification-hubs-test-app-java)

This is a sample application built on `com.microsoft.windowsazure.messaging.notificationhubs`. It was built to represent current best practices, and
demonstrate how to get off the ground with ANH. If you're looking to experiment
with ANH, this is a good app to get started with, to see what it can do.

### [`notification-hubs-test-app-legacy`](./notification-hubs-test-app-legacy)

This application demonstrates how to use the Registration flow using `com.microsoft.windowsazure.messaging`. If older tutorials or blogs (published before August, 2020) don't otherwise specify, this is likely the sample application they are referring to.

### [`FCMTutorialApp`](./FCMTutorialApp)

This application demostrates the final product of a person freshly completing [this tutorial](https://docs.microsoft.com/en-us/azure/notification-hubs/notification-hubs-android-push-notification-google-fcm-get-started). When there is a discrepancy between the tutorial and this application, try running the application here to see if it has the desired behavior.

### [`notification-hubs-sdk-e2etestapp`](./notification-hubs-sdk-e2etestapp)

This is an old-school test harness to execute unit tests on `com.microsoft.windowsazure.messaging` from before there was a more sophististicated environment for executing tests in an Android environment.

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
