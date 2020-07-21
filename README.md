
<center><svg width="300" aria-hidden="true" role="presentation" data-slug-id="notification-hubs" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 18 18">
  <defs>
    <linearGradient id="notification-hubs:bd9b1662-9bee-4ea8-9cee-787e4ccd4f18-58c4495c" x1="9" y1="0.81" x2="9" y2="21.28" gradientUnits="userSpaceOnUse">
      <stop offset="0" stop-color="#ffd70f"></stop>
      <stop offset="0.34" stop-color="#eeb309"></stop>
      <stop offset="0.77" stop-color="#dc8c03"></stop>
      <stop offset="1" stop-color="#d57d01"></stop>
    </linearGradient>
  </defs>
      <path d="M17.5,2.5V13.33a.58.58,0,0,1-.59.58H12.39a.14.14,0,0,0-.14.14V15.8a.28.28,0,0,1-.45.22L9.08,13.94l-.09,0H1.09a.58.58,0,0,1-.59-.58V2.5a.58.58,0,0,1,.59-.58H16.91A.58.58,0,0,1,17.5,2.5Z" fill="url(#notification-hubs:bd9b1662-9bee-4ea8-9cee-787e4ccd4f18-58c4495c)"></path>
      <path d="M2.05,9l3,2.52a.29.29,0,0,0,.47-.22v-1.1H17.48V8.5H2.23A.29.29,0,0,0,2.05,9Z" fill="#ffe452"></path>
      <path d="M16.24,6.46,13.13,3.94a.29.29,0,0,0-.47.22v1.1H.5V7H16.06A.29.29,0,0,0,16.24,6.46Z" fill="#fff"></path>
    <rect width="18" height="18" fill="none"></rect>
</svg></center>

# Microsoft Azure Notification Hubs - Android SDK

Microsoft Azure Notification Hubs provide a multi-platform, scaled-out push infrastructure that enables you to send mobile push notifications from any backend (in the cloud or on-premises) to any mobile platform. To learn more, visit our [Developer Center](https://azure.microsoft.com/en-us/documentation/services/notification-hubs).


## Getting Started with Android SDK

### Reference with Gradle

This library is published on [JFrog Bintray](https://bintray.com/microsoftazuremobile/SDK/Notification-Hubs-Android-SDK#files/com/microsoft/azure/notification-hubs-android-sdk). Adding a reference to this project is as simple as downloading your 'google-services.json' file from the [Firebase Console](https://console.firebase.google.com) and editting two files in your project:

_{project-root}/build.gradle:_

``` groovy
// This is not a complete build.gradle file, it only highlights the portions you'll need to use ANH.

buildscript {
    repositories {
        // Make sure Google's repository is available for build-time dependency resolution.
        google()
    }
    dependencies {
        // Make sure the google-services plugin is available for your project.
        classpath 'com.google.gms:google-services:4.3.3'
    }
}

allprojects {
    repositories {
        // Ensure you have the following two repsoitories in your "allprojects", "repositories" section.
        google()
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

// Ensure your google-services.json is loaded at build time with the following line.
apply plugin: 'com.google.gms.google-services'
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
