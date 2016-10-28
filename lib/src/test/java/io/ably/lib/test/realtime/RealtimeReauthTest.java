package io.ably.lib.test.realtime;

import org.junit.Test;

import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.ConnectionState;
import io.ably.lib.rest.AblyRest;
import io.ably.lib.rest.Auth;
import io.ably.lib.test.common.Helpers;
import io.ably.lib.test.common.Setup;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Capability;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.ErrorInfo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by VOstopolets on 8/26/16.
 */
public class RealtimeReauthTest {

	/**
	 * RTC8a: In-place reauthorization on a connected connection.
	 */
	@Test
	public void reauth_tokenDetails() {
		String wrongChannel = "wrongchannel";
		String rightChannel = "rightchannel";
		String testClientId = "testClientId";

		try {
			/* init ably for token */
			final Setup.TestVars optsTestVars = Setup.getTestVars();
			ClientOptions optsForToken = optsTestVars.createOptions(optsTestVars.keys[0].keyStr);
			final AblyRest ablyForToken = new AblyRest(optsForToken);
			System.out.println("done init ably for token");

			/* get first token */
			Auth.TokenParams tokenParams = new Auth.TokenParams();
			Capability capability = new Capability();
			capability.addResource(wrongChannel, "*");
			tokenParams.capability = capability.toString();
			tokenParams.clientId = testClientId;
			System.out.println("done get first token");

			Auth.TokenDetails firstToken = ablyForToken.auth.requestToken(tokenParams, null);
			assertNotNull("Expected token value", firstToken.token);

			/* create ably realtime with tokenDetails and clientId */
			final Setup.TestVars testVars = Setup.getTestVars();
			ClientOptions opts = testVars.createOptions();
			opts.clientId = testClientId;
			opts.tokenDetails = firstToken;
			AblyRealtime ablyRealtime = new AblyRealtime(opts);
			System.out.println("done create ably");

			/* wait for connected state */
			Helpers.ConnectionWaiter connectionWaiter = new Helpers.ConnectionWaiter(ablyRealtime.connection);
			connectionWaiter.waitFor(ConnectionState.connected);
			assertEquals("Verify connected state is reached", ConnectionState.connected, ablyRealtime.connection.state);
			System.out.println("connected");

			/* create a channel and check can't attach */
			Channel channel = ablyRealtime.channels.get(rightChannel);
			Helpers.CompletionWaiter waiter = new Helpers.CompletionWaiter();
			channel.attach(waiter);
			ErrorInfo error = waiter.waitFor();
			assertNotNull("Expected error", error);
			assertEquals("Verify error code 40160 (channel is denied access)", error.code, 40160);
			System.out.println("can't attach");

			/* RTC8a1: A test should exist that performs an upgrade of
			 * capabilities without any loss of continuity or connectivity
			 * during the upgrade process. */

			/* get second token */
			tokenParams = new Auth.TokenParams();
			capability = new Capability();
			capability.addResource(wrongChannel, "*");
			capability.addResource(rightChannel, "*");
			tokenParams.capability = capability.toString();
			tokenParams.clientId = testClientId;
			System.out.println("got second token");

			Auth.TokenDetails secondToken = ablyForToken.auth.requestToken(tokenParams, null);
			assertNotNull("Expected token value", secondToken.token);

			/* reauthorize */
			connectionWaiter.reset();
			Auth.AuthOptions authOptions = new Auth.AuthOptions();
			authOptions.key = optsTestVars.keys[0].keyStr;
			authOptions.tokenDetails = secondToken;
			Auth.TokenDetails reauthTokenDetails = ablyRealtime.auth.authorize(null, authOptions);
			assertNotNull("Expected token value", reauthTokenDetails.token);
			System.out.println("done reauthorize");

			/* re-attach to the channel */
			waiter = new Helpers.CompletionWaiter();
			System.out.println("attaching");
			channel.attach(waiter);

			/* verify onSuccess callback gets called */
			waiter.waitFor();
			System.out.println("waited for attach");
			assertThat(waiter.success, is(true));
			/* Verify that the connection never disconnected (0.9 in-place authorization) */
			assertTrue("Expected in-place authorization", connectionWaiter.getCount(ConnectionState.connecting) == 0);

			/* RTC8a2: Another test should exist where the capabilities are
			 * downgraded resulting in Ably sending an ERROR ProtocolMessage
			 * with a channel property, causing the channel to enter the FAILED
			 * state. That test must assert that the channel becomes failed
			 * soon after the token update and the reason is included in the
			 * channel state change event. */

			/* reauthorize */
			System.out.println("Switching back to first token");
			connectionWaiter.reset();
			authOptions = new Auth.AuthOptions();
			authOptions.key = optsTestVars.keys[0].keyStr;
			authOptions.tokenDetails = firstToken;
			reauthTokenDetails = ablyRealtime.auth.authorize(null, authOptions);
			assertNotNull("Expected token value", reauthTokenDetails.token);
			System.out.println("done reauthorize 2");

			/* Sleep to allow for server error message. */
			try {
				Thread.sleep(4000);
			} catch (Exception e) {
			}



		} catch (AblyException e) {
			e.printStackTrace();
			fail();
		}
	}
}
