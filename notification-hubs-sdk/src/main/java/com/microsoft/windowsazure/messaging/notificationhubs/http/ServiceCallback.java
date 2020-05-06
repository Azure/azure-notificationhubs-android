/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.windowsazure.messaging.notificationhubs.http;

/**
 * The callback used for client side asynchronous operations.
 */
public interface ServiceCallback {

    /**
     * Implement this method to handle successful REST call results.
     *
     * @param httpResponse the HTTP response.
     */
    void onCallSucceeded(HttpResponse httpResponse);

    /**
     * Implement this method to handle REST call failures.
     *
     * @param e the exception thrown from the pipeline.
     */
    void onCallFailed(Exception e);
}