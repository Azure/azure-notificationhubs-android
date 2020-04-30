package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import androidx.test.filters.SmallTest;

import com.android.volley.Header;
import com.android.volley.toolbox.Volley;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SmallTest
public class RetryPolicyTest {
    private Context context = getInstrumentation().getTargetContext();
    private Installation installation;

    @Before
    public void Before() {
        installation = new Installation();
        installation.setInstallationId("id_first");
        installation.setPushChannel("pushChannel");
        installation.addTags(Stream.of("tag1", "tag2", "tag3").collect(Collectors.toList()));
    }

    @Test
    public void InstallationManagerDoesNotRetryOn200() {
        when(Volley.newRequestQueue(any())).thenReturn(new FakeRequestQueue(context, 200, GetHeadersListWithoutRetryAfter()));
        NotificationHubInstallationAdapter nhInstallationManager = new NotificationHubInstallationAdapter("fake-hub", "fake-conn-string");

        nhInstallationManager.saveInstallation(context, installation);

        verify(nhInstallationManager, times(1)).saveInstallation(context, installation);
    }

    private List<Header> GetHeadersListWithRetryAfter(){
        Header retryAfterHeader = new Header("Retry-After", "2500");
        ArrayList<Header> headersList = new ArrayList<Header>(1);
        headersList.add(retryAfterHeader);

        return headersList;
    }

    private List<Header> GetHeadersListWithoutRetryAfter(){
        ArrayList<Header> headersList = new ArrayList<Header>(1);

        return headersList;
    }
}
