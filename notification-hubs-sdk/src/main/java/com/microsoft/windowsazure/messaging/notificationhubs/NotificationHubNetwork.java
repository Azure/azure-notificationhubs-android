package com.microsoft.windowsazure.messaging.notificationhubs;

import android.os.SystemClock;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interprets raw HTTP responses to fit the higher-level Volley abstractions. This includes applying
 * retry policies.
 *
 * The {@link Network} implementation {@link com.android.volley.toolbox.BasicNetwork} does not give
 * us the flexibility we need to write retry behavior that will match the other Azure Notification
 * Hub SDKs. Notably, {@link com.android.volley.RetryPolicy} only gives one control of the time
 * delay that will be applied and whether or not to continue retrying on a status code the Network
 * has already deemed appropriate to retry on.
 */
class NotificationHubNetwork implements Network {
    /**
     * Offers control of how {@link NotificationHubNetwork} should interpret the result of an HTTP
     * request.
     */
    public enum StatusCodePolicy {
        SUCCESS,
        RETRIABLE_FAILURE,
        IMMEDIATE_FAILURE
    }

    private static final Map<Integer, StatusCodePolicy> DEFAULT_STATUS_CODE_POLICY;
    public static final int HTTP_TOO_MANY = 429;

    private final BaseHttpStack mBaseHttpStack;
    private final Map<String, String> mAdditionalHeaders;
    private final Map<Integer, StatusCodePolicy> mStatusCodePolicies;

    static {
        DEFAULT_STATUS_CODE_POLICY = new HashMap<Integer, StatusCodePolicy>();

        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_INTERNAL_ERROR, StatusCodePolicy.RETRIABLE_FAILURE); // 500
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_UNAVAILABLE, StatusCodePolicy.RETRIABLE_FAILURE); // 503
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, StatusCodePolicy.RETRIABLE_FAILURE); // 504
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_FORBIDDEN, StatusCodePolicy.RETRIABLE_FAILURE); // 403 Forbidden, used by old Notification Hub to indicate you were forbidden from call this often.
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_CLIENT_TIMEOUT, StatusCodePolicy.RETRIABLE_FAILURE); // 408
        DEFAULT_STATUS_CODE_POLICY.put(HTTP_TOO_MANY, StatusCodePolicy.RETRIABLE_FAILURE); // 429

        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_OK, StatusCodePolicy.SUCCESS); // 200
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_CREATED, StatusCodePolicy.SUCCESS); // 201
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_NO_CONTENT, StatusCodePolicy.SUCCESS); // 204

        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_BAD_REQUEST, StatusCodePolicy.IMMEDIATE_FAILURE); // 400
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_UNAUTHORIZED, StatusCodePolicy.IMMEDIATE_FAILURE); // 401
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_NOT_FOUND, StatusCodePolicy.IMMEDIATE_FAILURE); // 404
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_BAD_METHOD, StatusCodePolicy.IMMEDIATE_FAILURE); // 405
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_CONFLICT, StatusCodePolicy.IMMEDIATE_FAILURE); // 409
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_GONE, StatusCodePolicy.IMMEDIATE_FAILURE); // 410
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_PRECON_FAILED, StatusCodePolicy.IMMEDIATE_FAILURE); // 412
        DEFAULT_STATUS_CODE_POLICY.put(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, StatusCodePolicy.IMMEDIATE_FAILURE); // 413
    }

    public NotificationHubNetwork(BaseHttpStack httpStack) {
        this(httpStack, DEFAULT_STATUS_CODE_POLICY, Collections.<String, String>emptyMap());
    }

    public NotificationHubNetwork(BaseHttpStack httpStack, Map<Integer, StatusCodePolicy> statusCodes, Map<String, String> additionalHeaders) {
        mBaseHttpStack = httpStack;
        mAdditionalHeaders = new HashMap<String, String>(additionalHeaders);
        mStatusCodePolicies = new HashMap<Integer, StatusCodePolicy>(statusCodes);
    }

    /**
     * Performs the specified request.
     *
     * @param request Request to process
     * @return A {@link NetworkResponse} with data and caching metadata; will never be null
     * @throws VolleyError
     */
    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        final RetryPolicy retryPolicy = request.getRetryPolicy();

        while(true) {
            HttpResponse httpResponse = null;
            NetworkResponse networkResponse = null;

            try {
                final long startTimeMillis = SystemClock.elapsedRealtime();
                httpResponse = mBaseHttpStack.executeRequest(request, mAdditionalHeaders);
                networkResponse = convertResponse(httpResponse, startTimeMillis);

                StatusCodePolicy policy = getStatusCodePolicy(httpResponse.getStatusCode());

                switch (policy) {
                    case SUCCESS:
                        return networkResponse;
                    case IMMEDIATE_FAILURE:
                        throw getVolleyError(networkResponse);
                    case RETRIABLE_FAILURE:
                        // Q: Why not retry here?
                        // A: We cannot guarantee that the BaseHttpStack will not always communicate
                        //    failure via a status code. It is equally likely that it will throw an
                        //    IOException. In order to not repeat ourselves, we will relay that a
                        //    retriable status code was received by throwing an exception that will
                        //    be caught and processed the same way one thrown directly by the HTTP
                        //    Stack would be handled.
                        throw new IOException("A non-success status code was encountered");
                    default:
                        String errMessage = "An unrecognized StatusCodePolicy has been applied to status code " + httpResponse.getStatusCode();
                        throw new UnsupportedOperationException(errMessage);
                }
            } catch (MalformedURLException e)  {
                // Match behavior of com.android.volley.toolbox.BasicNetwork @ 1.1.1
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (SocketTimeoutException e) {
                retryPolicy.retry(new TimeoutError());
            } catch (IOException e) {
                if (httpResponse == null) {
                    throw new NoConnectionError(e);
                }

                VolleyError error;
                if (networkResponse == null) {
                    error = new NetworkError(e);
                } else {
                    error = getVolleyError(networkResponse);
                }

                retryPolicy.retry(error);
            }
        }
    }

    /**
     * Fetches the behavior that should be applied to a given HTTP status code.
     *
     * Status codes that are passed to the constructor of this type, or inherited from
     * DEFAULT_STATUS_CODE_POLICIES are essentially explicit declared desired behavior. If a status
     * code is not found, it reverts to the following behavior:
     *
     * | Range | Behavior          |
     * | :---: | :---------------- |
     * | 2**   | Success           |
     * | 5**   | Retriable Failure |
     * | else  | Immediate Failure |
     *
     * @param statusCode The HTTP status code that was encountered.
     * @return The behavior that is most applicable to this status code.
     */
    public StatusCodePolicy getStatusCodePolicy(final int statusCode) {
        StatusCodePolicy policy = mStatusCodePolicies.get(statusCode);
        if (policy == null) {
            if (statusCode >= 200 && statusCode <= 299) {
                policy = StatusCodePolicy.SUCCESS;
            } else if (statusCode >= 500 && statusCode <= 599) {
                policy = StatusCodePolicy.RETRIABLE_FAILURE;
            } else {
                policy = StatusCodePolicy.IMMEDIATE_FAILURE;
            }
        }
        return policy;
    }

    /**
     * Fetches the body of the Response (i.e. do the download of the stream.)
     * @param response The {@link HttpResponse} providing the handle to download the body.
     * @return The body of the response, if there is no body an empty byte array is returned.
     */
    byte[] readBody(HttpResponse response) throws IOException {
        InputStream inputStream = response.getContent();
        if (inputStream == null) {
            return new byte[]{};
        }

        try {
            byte[] retval;
            final int contentLength = response.getContentLength();
            if (contentLength > 0 && contentLength < Integer.MAX_VALUE) {
                retval = new byte[contentLength];
                if (inputStream.read(retval) != -1) {
                    throw new IOException("Content-Length does not match stream encountered");
                }
            } else {
                List<Byte> buffer = new ArrayList<Byte>(5 * 1024); // 5KB if we don't know how much is coming.
                Scanner scanner = new Scanner(inputStream);

                while(scanner.hasNextByte()) {
                    buffer.add(scanner.nextByte());
                }

                // Can't call List<Byte>.toArray(T[]) because of boxing/type parameter constraints.
                retval = new byte[buffer.size()];
                for (int i = 0; i < buffer.size(); i++) {
                    retval[i] = buffer.get(i);
                }
            }
            return retval;
        } finally {
            inputStream.close();
        }
    }

    private static VolleyError getVolleyError(NetworkResponse response) {
        if(response.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return new AuthFailureError(response);
        } else if(response.statusCode >= 500 && response.statusCode <= 599) {
            return new ServerError(response);
        } else if(response.statusCode >= 400 && response.statusCode <= 499) {
            return new ClientError(response);
        } else {
            // Match behavior of com.android.volley.toolbox.BasicNetwork @ 1.1.1
            return new ServerError(response);
        }
    }

    private NetworkResponse convertResponse(HttpResponse response, final long startTimeMillis) throws IOException {
        return new NetworkResponse(
                response.getStatusCode(),
                readBody(response),
                response.getStatusCode() == HttpURLConnection.HTTP_NOT_MODIFIED,
                SystemClock.elapsedRealtime() - startTimeMillis,
                response.getHeaders());
    }
}
