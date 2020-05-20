/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.windowsazure.messaging.notificationhubs;

interface ServiceCall {

    /**
     * Cancel the call if possible.
     */
    void cancel();
}