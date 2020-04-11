package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ConnectionString {
    private static final Pattern PORTAL_FORMAT_PATTERN = Pattern.compile("(?<key>[^=]+)=(?<value>[^;]+);");
    private static final String SHARED_ACCESS_KEY_NAME_KEY = "SharedAccessKeyName";
    private static final String ENDPOINT_KEY = "Endpoint";
    private static final String SHARED_ACCESS_KEY = "SharedAccessKey";

    private String mEndpoint;
    private String mSharedAccessKeyName;
    private String mSharedAccessKey;

    ConnectionString(String endpoint, String sharedAccessKeyName, String sharedAccessKey) {
        mEndpoint = endpoint;
        mSharedAccessKeyName = sharedAccessKeyName;
        mSharedAccessKey = sharedAccessKey;
    }

    public static ConnectionString parse(String connectionString) {
        Matcher matcher = PORTAL_FORMAT_PATTERN.matcher(connectionString);
        return null;
    }

    public String getEndpoint() {
        return mEndpoint;
    }

    public String getSharedAccessKeyName() {
        return mSharedAccessKeyName;
    }

    public String getSharedAccessKey() {
        return mSharedAccessKey;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConnectionString)) {
            return false;
        }

        ConnectionString cast = (ConnectionString) o;

        return this.mSharedAccessKey.equals(cast.mSharedAccessKey) &&
               this.mSharedAccessKeyName.equals(cast.mSharedAccessKeyName) &&
               this.mEndpoint.equalsIgnoreCase(cast.mEndpoint);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append(ENDPOINT_KEY);
        builder.append('=');
        builder.append(mEndpoint);
        builder.append(';');

        builder.append(SHARED_ACCESS_KEY_NAME_KEY);
        builder.append('=');
        builder.append(mSharedAccessKeyName);
        builder.append(';');

        builder.append(SHARED_ACCESS_KEY);
        builder.append('=');
        builder.append(mSharedAccessKey);
        builder.append(';');

        return builder.toString();
    }
}
