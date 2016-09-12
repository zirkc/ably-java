package test.rest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.ably.lib.test.common.Setup;
import io.ably.lib.test.rest.HttpTest;
import io.ably.lib.test.rest.RestAppStatsTest;
import io.ably.lib.test.rest.RestAuthTest;
import io.ably.lib.test.rest.RestCapabilityTest;
import io.ably.lib.test.rest.RestChannelHistoryTest;
import io.ably.lib.test.rest.RestChannelPublishTest;
import io.ably.lib.test.rest.RestCryptoTest;
import io.ably.lib.test.rest.RestInitTest;
import io.ably.lib.test.rest.RestPresenceTest;
import io.ably.lib.test.rest.RestProxyTest;
import io.ably.lib.test.rest.RestTimeTest;
import io.ably.lib.test.rest.RestTokenTest;

@RunWith(Suite.class)
@SuiteClasses({
	RestAppStatsTest.class,
	RestInitTest.class,
	RestTimeTest.class,
	RestAuthTest.class,
	RestTokenTest.class,
	RestCapabilityTest.class,
	RestChannelHistoryTest.class,
	RestChannelPublishTest.class,
	RestCryptoTest.class,
	RestPresenceTest.class,
	RestProxyTest.class,
	HttpTest.class
})
public class RestSuite {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Setup.getTestVars();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Setup.clearTestVars();
	}
}
