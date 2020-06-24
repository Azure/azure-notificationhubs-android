package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NotificationHubInstallationHelper {
    private static final Pattern ENDPOINT_FORMAT_PATTERN = Pattern.compile("sb://([a-zA-Z.\\-_]+)/?");

    static String parseSbEndpoint(String endpoint){
        Matcher matcher = ENDPOINT_FORMAT_PATTERN.matcher(endpoint);

        if (!matcher.matches()){
            throw new IllegalArgumentException("Wrong endpoint format");
        }

        return matcher.group(1);
    }

    static String getInstallationUrl(String endpoint, String hubName, String installationId, String apiVersion) {
        StringBuilder url = new StringBuilder();
        url.append("https://").append(endpoint).append("/").append(hubName).append("/installations/")
                .append(installationId).append("?api-version=").append(apiVersion);
        return url.toString();
    }

    static String getApiVersion(String endpoint) {
        if(endpoint.contains(AzureEnvironment.INT7.getDomain())) {
            return AzureEnvironment.INT7.getApiVersion();
        } else if(endpoint.endsWith(AzureEnvironment.BFPROD.getDomain())) {
            return AzureEnvironment.BFPROD.getApiVersion();
        } else if(endpoint.endsWith(AzureEnvironment.FFPROD.getDomain())) {
            return AzureEnvironment.FFPROD.getApiVersion();
        } else if(endpoint.endsWith(AzureEnvironment.CHPROD.getDomain())) {
            return AzureEnvironment.CHPROD.getApiVersion();
        } else {
            return AzureEnvironment.PROD.getApiVersion();
        }
    }
}
