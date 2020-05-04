/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.windowsazure.messaging.notificationhubs.http;

import java.io.IOException;

public abstract class HttpClientDecorator implements HttpClient {

    final HttpClient mDecoratedApi;

    HttpClientDecorator(HttpClient decoratedApi) {
        mDecoratedApi = decoratedApi;
    }

    @Override
    public void close() throws IOException {
        mDecoratedApi.close();
    }

    @Override
    public void reopen() {
        mDecoratedApi.reopen();
    }

    HttpClient getDecoratedApi() {
        return mDecoratedApi;
    }
}