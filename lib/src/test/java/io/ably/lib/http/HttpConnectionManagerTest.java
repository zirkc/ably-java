package io.ably.lib.http;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.ConnectionState;
import io.ably.lib.test.common.Helpers;
import io.ably.lib.test.common.Setup;
import io.ably.lib.transport.ConnectionManager;
import io.ably.lib.transport.Defaults;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Param;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by VOstopolets on 9/21/16.
 */
public class HttpConnectionManagerTest {
	/**
	 * Verifies that, when the realtime client is connected to a fallback host endpoint,
	 * all HTTP requests should be first attempted to the same data centre.
	 * If however the HTTP request against that fallback host fails, then the normal fallback
	 * host behaviour should be followed attempting the request against another fallback host.
	 * <p>
	 * Spec: RTN17e
	 * </p>
	 */
	@Test
	public void http_connectionmanager_fallback_http_requests() throws AblyException {
		final String sandboxFallbackHost = String.format("%s-%s", "sandbox", Defaults.HOST_REALTIME);
		final String[] expectedFallbackHosts = new String[]{sandboxFallbackHost, "f.ably-realtime.com", "g.ably-realtime.com", "h.ably-realtime.com", "i.ably-realtime.com", "j.ably-realtime.com"};
		final List<String> fallbackHostsList = Arrays.asList(expectedFallbackHosts);

		/* Init ably with unreachable host and custom fallbackHosts */
		Setup.TestVars testVars = Setup.getTestVars();
		ClientOptions opts = new ClientOptions(testVars.keys[0].keyStr);
		testVars.fillInOptions(opts);
		opts.autoConnect = false;
		opts.environment = null;
		opts.realtimeHost = "un.reachable.host.example.com";
		opts.fallbackHosts = expectedFallbackHosts;
		AblyRealtime ably = new AblyRealtime(opts);
		ConnectionManager connectionManager = ably.connection.connectionManager;

		/* Connect, the first attempt to the unreachable host */
		connectionManager.connect();

		/* Waiting for connection established to the fallback host */
		new Helpers.ConnectionManagerWaiter(connectionManager).waitFor(ConnectionState.connected);

		/* Verify that,
		 *   - connectionState is connected
		 *   - connection established to the fallback host */
		assertThat("Unexpected connection state", connectionManager.getConnectionState().state, is(ConnectionState.connected));
		assertThat("Unexpected host", fallbackHostsList.contains(connectionManager.getHost()), is(true));

		/* Capture established fallback host */
		final String establishedFallbackHost = connectionManager.getHost();

		Http http = Mockito.spy(ably.http);
		String responseExpected = "Lorem Ipsum";
		ArgumentCaptor<URL> url = ArgumentCaptor.forClass(URL.class);
		/* Partially mock http */
		Answer answer = new HttpTest.GrumpyAnswer(
				2, /* Throw exception twice (2) */
				AblyException.fromErrorInfo(ErrorInfo.fromResponseStatus("Internal Server Error", 500)), /* That is HostFailedException */
				responseExpected /* Then return a valid response with third call */
		);

		doAnswer(answer) /* Behave as defined above */
				.when(http) /* when following method is executed on {@code Http} instance */
				.httpExecute(
						url.capture(), /* capture url arguments passed down httpExecute to assert fallback behavior executed with valid fallback url */
						any(Proxy.class), /* Ignore */
						anyString(), /* Ignore */
						aryEq(new Param[0]), /* Ignore */
						any(Http.RequestBody.class), /* Ignore */
						anyBoolean(), /* Ignore */
						any(Http.ResponseHandler.class) /* Ignore */
				);
		String responseActual = (String) http.ablyHttpExecute(
				"", /* Ignore */
				"", /* Ignore */
				new Param[0], /* Ignore */
				new Param[0], /* Ignore */
				mock(Http.RequestBody.class), /* Ignore */
				mock(Http.ResponseHandler.class) /* Ignore */
		);

		/* Verify call causes captor to capture same arguments thrice.
		 * Do the validation, after we completed the {@code ArgumentCaptor} related assertions */
		verify(http, times(3))
				.httpExecute( /* Just validating call counter. Ignore following parameters */
						any(URL.class), /* Ignore */
						any(Proxy.class), /* Ignore */
						anyString(), /* Ignore */
						aryEq(new Param[0]), /* Ignore */
						any(Http.RequestBody.class), /* Ignore */
						anyBoolean(), /* Ignore */
						any(Http.ResponseHandler.class) /* Ignore */
				);
		List<URL> allValues = url.getAllValues();
		assertThat("Unexpected response", responseActual, is(equalTo(responseExpected)));
		assertThat("Unexpected host", allValues.get(0).getHost(), is(equalTo(establishedFallbackHost)));
		assertThat("Unexpected host fallback", fallbackHostsList.contains(allValues.get(1).getHost()), is(true));
		assertThat("Unexpected host fallback", fallbackHostsList.contains(allValues.get(2).getHost()), is(true));
	}
}
