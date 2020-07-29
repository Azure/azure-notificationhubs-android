package com.microsoft.windowsazure.messaging.notificationhubs;

import androidx.test.filters.SmallTest;

import com.android.volley.Header;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.HurlStack;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SmallTest
public class NotificationHubNetworkTest {

    @Test
    public void copyInstallationPutOK() throws JSONException, VolleyError {
        InstallationAdapter.Listener success = new InstallationAdapter.Listener() {
            @Override
            public void onInstallationSaved(Installation i) {

            }
        };

        InstallationAdapter.ErrorListener failure = new InstallationAdapter.ErrorListener() {
            @Override
            public void onInstallationSaveError(Exception e) {

            }
        };

        Installation installation = new Installation();
        installation.setPushChannel(UUID.randomUUID().toString());

        HttpResponse fauxResponse = getInstallationPutResponse(HttpURLConnection.HTTP_OK);
        //BaseHttpStack httpStack = new MockBaseHttpStack(fauxResponse);
        BaseHttpStack httpStack = new HurlStack();


        NotificationHubNetwork subject = new NotificationHubNetwork(httpStack);
        ConnectionString connectionString = ConnectionString.parse(ConnectionStringTest.WELL_FORMED_CONNECTION_STRING);

        NetworkResponse response = subject.performRequest(new InstallationPutRequest(connectionString, "hub1", installation, success, failure));

        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode);
        Assert.assertNotNull(response.data);
        Assert.assertEquals(0, response.data.length);
    }

    private static HttpResponse getInstallationPutResponse(int statusCode) {
        return getInstallationPutResponse("namespace1", "hub1", UUID.randomUUID().toString(), statusCode);
    }

    private static HttpResponse getInstallationPutResponse(String namespaceName, String hubName, String installationId, int statusCode) {
        String installationLocation = InstallationPutRequest.getInstallationUrl(namespaceName + ".servicebus.windows.net", hubName, installationId);

         List<Header> headers = new ArrayList<>(2);
         headers.add(new Header("Content-Type", "application/json"));
         headers.add(new Header("Content-Location", installationLocation));

        return new HttpResponse(statusCode, headers);
    }
}
