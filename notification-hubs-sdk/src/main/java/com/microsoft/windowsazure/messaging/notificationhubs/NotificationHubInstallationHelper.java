package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NotificationHubInstallationHelper {
    private static final Pattern ENDPOINT_FORMAT_PATTERN = Pattern.compile("sb://([a-zA-Z.\\-_]+)/?");

    static String parseSbEndpoint(String endpoint){
        Matcher matcher = ENDPOINT_FORMAT_PATTERN.matcher(endpoint);
        return matcher.matches() ? matcher.group(1): "";
    }

    static String getInstallationUrl(String endpoint, String hubName, String installationId){
        StringBuilder url = new StringBuilder();
        url.append("https://").append(endpoint).append("/").append(hubName).append("/installations/")
                .append(installationId).append("?api-version=2017-04");
        return url.toString();
    }
}
