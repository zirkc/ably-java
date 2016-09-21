package io.ably.lib.transport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by VOstopolets on 9/19/16.
 */
public class TestTransportFactory extends WebSocketTransport.Factory {
	private final List<String> hostArgumentStack = new ArrayList<>();

	public List<String> getHostArgumentStack() {
		return hostArgumentStack;
	}

	@Override
	public WebSocketTransport getTransport(ITransport.TransportParams params, ConnectionManager connectionManager) {
		hostArgumentStack.add(params.host);
		return super.getTransport(params, connectionManager);
	}
}
