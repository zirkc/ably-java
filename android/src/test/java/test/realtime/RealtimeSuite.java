package test.realtime;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.ably.lib.test.common.Setup;
import io.ably.lib.test.realtime.EventEmitterTest;
import io.ably.lib.test.realtime.RealtimeChannelHistoryTest;
import io.ably.lib.test.realtime.RealtimeChannelTest;
import io.ably.lib.test.realtime.RealtimeConnectFailTest;
import io.ably.lib.test.realtime.RealtimeConnectTest;
import io.ably.lib.test.realtime.RealtimeCryptoMessageTest;
import io.ably.lib.test.realtime.RealtimeCryptoTest;
import io.ably.lib.test.realtime.RealtimeInitTest;
import io.ably.lib.test.realtime.RealtimeMessageTest;
import io.ably.lib.test.realtime.RealtimePresenceHistoryTest;
import io.ably.lib.test.realtime.RealtimePresenceTest;
import io.ably.lib.test.realtime.RealtimeRecoverTest;
import io.ably.lib.test.realtime.RealtimeResumeTest;

@RunWith(Suite.class)
@SuiteClasses({
	EventEmitterTest.class,
	RealtimeInitTest.class,
	RealtimeConnectTest.class,
	RealtimeConnectFailTest.class,
	RealtimeChannelTest.class,
	RealtimePresenceTest.class,
	RealtimeMessageTest.class,
	RealtimeResumeTest.class,
	RealtimeRecoverTest.class,
	RealtimeCryptoTest.class,
	RealtimeCryptoMessageTest.class,
	RealtimeChannelHistoryTest.class,
	RealtimePresenceHistoryTest.class
})
public class RealtimeSuite {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Setup.getTestVars();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Setup.clearTestVars();
	}
}
