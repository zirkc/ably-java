package io.ably.lib.util;

import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;
import io.ably.lib.types.PresenceMessage;
import io.ably.lib.types.ProtocolMessage;

public class Json {
	public static class JsonCodec<T extends Encodable> extends Codec<T> {

		static <T extends Encodable> JsonCodec<T> create(Class<T> claz) {
			return new JsonCodec<T>(claz);
		}

		private JsonCodec(Class<T> claz) {
			super(false, "application/json", claz);
		}

		@Override
		public T decode(byte[] encoded) {
			return decodeFromText(new String(encoded, utf8));
		}

		@Override
		public T decodeFromText(String encoded) {
			return gson.fromJson(encoded, claz);
		}

		@Override
		public T[] decodeArray(byte[] encoded) throws AblyException {
			return decodeArrayFromText(new String(encoded, utf8));
		}

		@Override
		public T[] decodeArrayFromText(String encoded) throws AblyException {
			return gson.fromJson(encoded, arrClaz);
		}

		@Override
		public byte[] encode(Encodable obj) {
			return encodeToText(obj).getBytes(utf8);
		}

		@Override
		public String encodeToText(Encodable obj) {
			return gson.toJson(obj, claz);
		}

		@Override
		public byte[] encodeArray(Encodable[] obj) {
			return encodeArrayToText(obj).getBytes(utf8);
		}

		@Override
		public String encodeArrayToText(Encodable[] obj) {
			return gson.toJson(obj, arrClaz);
		}

		private static Charset utf8 = Charset.forName("UTF-8");
	}

	public static final JsonParser gsonParser;
	public static final GsonBuilder gsonBuilder;
	public static final Gson gson;

	static {
		gsonParser = new JsonParser();
		gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Message.class, new Message.Serializer());
		gsonBuilder.registerTypeAdapter(PresenceMessage.class, new PresenceMessage.Serializer());
		gsonBuilder.registerTypeAdapter(PresenceMessage.Action.class, new PresenceMessage.ActionSerializer());
		gsonBuilder.registerTypeAdapter(ProtocolMessage.Action.class, new ProtocolMessage.ActionSerializer());
		gson = gsonBuilder.create();
	}
}
