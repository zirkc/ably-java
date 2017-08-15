package io.ably.lib.types;

import io.ably.lib.http.Http.BodyHandler;
import io.ably.lib.util.Codec;
import io.ably.lib.util.Log;

/**
 * MessageReader: internal
 * Utility class to convert response bodies in different formats to Message
 * and Message arrays.
 */
public class MessageSerializer {

	/****************************************
	 *              BodyHandler
	 ****************************************/
	
	public static BodyHandler<Message> getMessageResponseHandler(ChannelOptions opts) {
		return opts == null ? messageResponseHandler : new MessageBodyHandler(opts);
	}

	private static class MessageBodyHandler implements BodyHandler<Message> {

		public MessageBodyHandler(ChannelOptions opts) { this.opts = opts; }

		@Override
		public Message[] handleResponseBody(String contentType, byte[] body) throws AblyException {
			Message[] messages = null;
			Codec<Message> codec = Codec.get(contentType, Message.class);
			messages = codec.decodeArray(body);

			if(messages != null) {
				for (Message message : messages) {
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

	private static BodyHandler<Message> messageResponseHandler = new MessageBodyHandler(null);

	private static final String TAG = MessageSerializer.class.getName();
}
