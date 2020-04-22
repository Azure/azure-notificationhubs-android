package com.microsoft.windowsazure.messaging.notificationhubs;

class NotificationHubInstallationHelper {

    static String parseSbEndpoint(String endpoint){
        return endpoint.replace("sb://", "").replace("/", "");
    }

    static String getInstallationUrl(String endpoint, String hubName, String installationId){
        StringBuilder url = new StringBuilder();
        url.append("https://").append(endpoint).append("/").append(hubName).append("/installations/")
                .append(installationId).append("?api-version=2017-04");
        return url.toString();
    }
}
