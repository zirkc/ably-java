package io.ably.lib.types;

import io.ably.lib.http.Http.BodyHandler;
import io.ably.lib.util.Codec;
import io.ably.lib.util.Log;

/**
 * PresenceSerializer: internal
 * Utility class to convert response bodies in different formats to PresenceMessage
 * and PresenceMessage arrays.
 */
public class PresenceSerializer {

	/****************************************
	 *              BodyHandler
	 ****************************************/
	
	public static BodyHandler<PresenceMessage> getPresenceResponseHandler(ChannelOptions opts) {
		return opts == null ? presenceResponseHandler : new PresenceBodyHandler(opts);
	}

	private static class PresenceBodyHandler implements BodyHandler<PresenceMessage> {

		public PresenceBodyHandler(ChannelOptions opts) { this.opts = opts; }

		@Override
		public PresenceMessage[] handleResponseBody(String contentType, byte[] body) throws AblyException {
			PresenceMessage[] messages = null;
			Codec<PresenceMessage> codec = Codec.get(contentType, PresenceMessage.class);
			messages = codec.decodeArray(body);

			if(messages != null) {
				for (PresenceMessage message : messages) {
					try {
						message.decode(opts);
					} catch (MessageDecodeException e) {
						Log.e(TAG, e.errorInfo.message);
					}
				}
			}
			return messages;
		}

		private ChannelOptions opts;
	}

	private static BodyHandler<PresenceMessage> presenceResponseHandler = new PresenceBodyHandler(null);

	private static final String TAG = PresenceSerializer.class.getName();
}
