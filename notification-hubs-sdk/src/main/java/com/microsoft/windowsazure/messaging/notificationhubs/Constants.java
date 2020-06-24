package com.microsoft.windowsazure.messaging.notificationhubs;

enum AzureEnvironment {
    INT7("2020-06", "int7.windows-int.net"),
    PROD("2020-06", "windows.net"),
    FFPROD("2017-04", "cloudapi.de"),
    BFPROD("2017-04", "usgovcloudapi.net"),
    CHPROD("2017-04", "chinacloudapi.cn");

    private String apiVersion;
    private String domain;
 
    AzureEnvironment(String apiVersion, String domain) {
        this.apiVersion = apiVersion;
        this.domain = domain;
    }
 
    public String getApiVersion() {
        return this.apiVersion;
    }

    public String getDomain() {
        return this.domain;
    }
}
