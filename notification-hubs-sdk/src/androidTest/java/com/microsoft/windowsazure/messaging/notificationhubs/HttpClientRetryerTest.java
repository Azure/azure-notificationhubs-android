/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.windowsazure.messaging.notificationhubs;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.microsoft.windowsazure.messaging.notificationhubs.HttpClient;
import com.microsoft.windowsazure.messaging.notificationhubs.HttpClientRetryer;
import com.microsoft.windowsazure.messaging.notificationhubs.HttpException;
import com.microsoft.windowsazure.messaging.notificationhubs.HttpResponse;
import com.microsoft.windowsazure.messaging.notificationhubs.ServiceCall;
import com.microsoft.windowsazure.messaging.notificationhubs.ServiceCallback;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.CONTENT_TYPE_KEY;
import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.CONTENT_TYPE_VALUE;
import static com.microsoft.windowsazure.messaging.notificationhubs.DefaultHttpClient.X_MS_RETRY_AFTER_MS_HEADER;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("unused")
public class HttpClientRetryerTest {

    private static void simulateRetryAfterDelay(Handler handler) {
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {
                Message message = (Message) invocation.getArguments()[0];
                message.getCallback().run();
                return null;
            }
        }).when(handler).postDelayed(any(Runnable.class), anyLong());
    }

    private static void verifyDelay(Handler handler, final int retryIndex) {
        verify(handler).postDelayed(any(Runnable.class), longThat(new ArgumentMatcher<Long>() {

            @Override
            public boolean matches(Object argument) {
                long interval = (Long) argument - SystemClock.uptimeMillis();
                long retryInterval = HttpClientRetryer.RETRY_INTERVALS[retryIndex];
                return interval >= retryInterval / 2 && interval <= retryInterval;
            }
        }));
    }

    private static void verifyDelayFromHeader(Handler handler, final long retryAfter) {
        verify(handler).postDelayed(any(Runnable.class), longThat(new ArgumentMatcher<Long>() {

            @Override
            public boolean matches(Object  argument) {
                long interval = (Long) argument - SystemClock.uptimeMillis();
                long delta = interval - retryAfter;
                return Math.abs(delta) < 100;
            }
        }));
    }

    @Test
    public void success() {
        final ServiceCall call = mock(ServiceCall.class);
        final ServiceCallback callback = mock(ServiceCallback.class);
        HttpClient httpClient = mock(HttpClient.class);
        final HttpResponse succeededResponse = new HttpResponse(200, "mockSuccessPayload");
        Answer<ServiceCall> answer = new Answer<ServiceCall>() {

            @Override
            public ServiceCall answer(InvocationOnMock invocationOnMock) {
                ((ServiceCallback) invocationOnMock.getArguments()[4]).onCallSucceeded(succeededResponse);
                return call;
            }
        };

        doAnswer(answer).when(httpClient).callAsync(anyString(), anyString(), anyMapOf(String.class, String.class), any(HttpClient.CallTemplate.class), any(ServiceCallback.class));
        HttpClientRetryer retryer = new HttpClientRetryer(httpClient);
        retryer.callAsync(null, null, null, null, callback);
        verify(callback).onCallSucceeded(eq(succeededResponse));
        verifyNoMoreInteractions(callback);
        verifyNoMoreInteractions(call);
    }

    @Test
    public void successAfterOneRetry() {
        final ServiceCallback callback = mock(ServiceCallback.class);
        HttpClient httpClient = mock(HttpClient.class);
        doAnswer(new Answer<ServiceCall>() {

            @Override
            public ServiceCall answer(InvocationOnMock invocationOnMock) {
                ((ServiceCallback) invocationOnMock.getArguments()[4]).onCallFailed(new HttpException(new HttpResponse(403)));
                return mock(ServiceCall.class);
            }
        }).doAnswer(new Answer<ServiceCall>() {

            @Override
            public ServiceCall answer(InvocationOnMock invocationOnMock) {
                ((ServiceCallback) invocationOnMock.getArguments()[4]).onCallSucceeded(new HttpResponse(200, "mockSuccessPayload"));
                return mock(ServiceCall.class);
            }
        }).when(httpClient).callAsync(anyString(), anyString(), anyMapOf(String.class, String.class), any(HttpClient.CallTemplate.class), any(ServiceCallback.class));
        Handler handler = mock(Handler.class);
        HttpClient retryer = new HttpClientRetryer(httpClient, handler);
        simulateRetryAfterDelay(handler);
        retryer.callAsync(null, null, null, null, callback);
        verifyDelay(handler, 0);
        verifyNoMoreInteractions(handler);
        verify(callback).onCallSucceeded(eq(new HttpResponse(200, "mockSuccessPayload")));
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void retryOnceThenFail() {
        final HttpException expectedException = new HttpException(new HttpResponse(500));
        final HttpException unexpectedException = new HttpException(new HttpResponse(503));
        final ServiceCallback callback = mock(ServiceCallback.class);
        HttpClient httpClient = mock(HttpClient.class);
        doAnswer(new Answer<ServiceCall>() {

            @Override
            public ServiceCall answer(InvocationOnMock invocationOnMock) {
                ((ServiceCallback) invocationOnMock.getArguments()[4]).onCallFailed(unexpectedException);
                return mock(ServiceCall.class);
            }
        }).doAnswer(new Answer<ServiceCall>() {

            @Override
            public ServiceCall answer(InvocationOnMock invocationOnMock) {
                ((ServiceCallback) invocationOnMock.getArguments()[4]).onCallFailed(expectedException);
                return mock(ServiceCall.class);
            }
        }).when(httpClient).callAsync(anyString(), anyString(), anyMapOf(String.class, String.class), any(HttpClient.CallTemplate.class), any(ServiceCallback.class));
        Handler handler = mock(Handler.class);
        HttpClient retryer = new HttpClientRetryer(httpClient, handler);
        simulateRetryAfterDelay(handler);
        retryer.callAsync(null, null, null, null, callback);
        verifyDelay(handler, 0);
        verifyNoMoreInteractions(handler);
        verify(callback).onCallFailed(any(Exception.class));
        verify(callback).onCallFailed(expectedException);
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void exhaustRetries() {
        final ServiceCall call = mock(ServiceCall.class);
        ServiceCallback callback = mock(ServiceCallback.class);
        HttpClient httpClient = mock(HttpClient.class);
        doAnswer(new Answer<ServiceCall>() {

            @Override
            public ServiceCall answer(InvocationOnMock invocationOnMock) {
                ((ServiceCallback) invocationOnMock.getArguments()[4]).onCallFailed(new HttpException(new HttpResponse(408)));
                return call;
            }
        }).when(httpClient).callAsync(anyString(), anyString(), anyMapOf(String.class, String.class), any(HttpClient.CallTemplate.class), any(ServiceCallback.class));
        Handler handler = mock(Handler.class);
        HttpClient retryer = new HttpClientRetryer(httpClient, handler);
        simulateRetryAfterDelay(handler);
        retryer.callAsync(null, null, null, null, callback);
        verifyDelay(handler, 0);
        verifyDelay(handler, 1);
        verifyDelay(handler, 2);
        verifyNoMoreInteractions(handler);
        verify(callback).onCallFailed(new HttpException(new HttpResponse(408)));
        verifyNoMoreInteractions(callback);
        verifyNoMoreInteractions(call);
    }

    @Test
    public void delayUsingRetryHeader() {

        /* Mock httpException onCallFailed with the HTTP Code 429 (Too many Requests) and the x-ms-retry-after-ms header set. */
        long retryAfterMS = 1234;
        Map<String, String> responseHeader = new HashMap<>();
        responseHeader.put(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
        responseHeader.put(X_MS_RETRY_AFTER_MS_HEADER, Long.toString(retryAfterMS));
        final HttpException expectedException = new HttpException(new HttpResponse(429, "call hit the retry limit", responseHeader));

        final ServiceCallback callback = mock(ServiceCallback.class);
        HttpClient httpClient = mock(HttpClient.class);
        doAnswer(new Answer<ServiceCall>() {

            @Override
            public ServiceCall answer(InvocationOnMock invocationOnMock) {
                ((ServiceCallback) invocationOnMock.getArguments()[4]).onCallFailed(expectedException);
                return mock(ServiceCall.class);
            }
        }).doAnswer(new Answer<ServiceCall>() {

            @Override
            public ServiceCall answer(InvocationOnMock invocationOnMock) {
                ((ServiceCallback) invocationOnMock.getArguments()[4]).onCallSucceeded(new HttpResponse(200, "mockSuccessPayload"));
                return mock(ServiceCall.class);
            }
        }).when(httpClient).callAsync(anyString(), anyString(), anyMapOf(String.class, String.class), any(HttpClient.CallTemplate.class), any(ServiceCallback.class));
        Handler handler = mock(Handler.class);
        HttpClient retryer = new HttpClientRetryer(httpClient, handler);
        simulateRetryAfterDelay(handler);

        /* Make the call. */
        retryer.callAsync(null, null, null, null, callback);

        /* Verify that onCallFailed we actually check for the response header and use that value to set the delay on the retry call. */
        verifyDelayFromHeader(handler, retryAfterMS);
        verifyNoMoreInteractions(handler);
        verify(callback).onCallSucceeded(eq(new HttpResponse(200, "mockSuccessPayload")));
        verifyNoMoreInteractions(callback);
    }
}