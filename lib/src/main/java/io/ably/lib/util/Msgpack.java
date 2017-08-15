package io.ably.lib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePack.PackerConfig;
import org.msgpack.core.MessagePack.UnpackerConfig;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;
import io.ably.lib.types.PresenceMessage;
import io.ably.lib.types.ProtocolMessage;

@SuppressWarnings("unchecked")
public class Msgpack {

	/**
	 * Describes the functionality needed in message types in order
	 * to be encodable by msgpack
	 * @param <T> the message type
	 */
	public interface MsgpackEncodable<T> extends Encodable {
		public T readMsgpack(MessageUnpacker unpacker) throws IOException;
		public void writeMsgpack(MessagePacker packer) throws IOException;
	}

	public static final MessageCodec messageCodec;
	public static final PresenceCodec presenceCodec;
	public static final ProtocolCodec protocolCodec;

	/**
	 * Base class for msgpack codecs
	 * @param <T> the message type
	 */
	public abstract static class MsgpackCodec<T extends MsgpackEncodable<T>> extends Codec<T> {

		/**
		 * Factory method to get a codec by type
		 * @param claz
		 * @return
		 */
		static <V extends Encodable> Codec<V> create(Class<V> claz) {
			return (Codec<V>) codecs.get(claz);
		}

		/**
		 * Private constuctor
		 * @param claz
		 */
		private MsgpackCodec(Class<T> claz) {
			super(true, "application/x-msgpack", claz);
		}

		/**
		 * Unused methods; applicable only to codecs that serialize to String
		 */
		@Override
		public T decodeFromText(String encoded) { throw textException(); }
		@Override
		public T[] decodeArrayFromText(String encoded) { throw textException(); }
		@Override
		public String encodeToText(T obj) { throw textException(); }
		@Override
		public String encodeArrayToText(T[] obj) { throw textException(); }

		/**
		 * Codec methods
		 */
		@Override
		public T decode(byte[] encoded) throws AblyException {
			try {
				MessageUnpacker unpacker = Msgpack.msgpackUnpackerConfig.newUnpacker(encoded);
				T result = claz.newInstance();
				result.readMsgpack(unpacker);
				return result;
			} catch(Throwable t) {
				throw AblyException.fromThrowable(t);
			}
		}

		@Override
		public T[] decodeArray(byte[] encoded) throws AblyException {
			try {
				MessageUnpacker unpacker = Msgpack.msgpackUnpackerConfig.newUnpacker(encoded);
				return decodeArray(unpacker);
			} catch(Throwable t) {
				throw AblyException.fromThrowable(t);
			}
		}

		@Override
		public byte[] encode(T obj) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				MessagePacker packer = msgpackPackerConfig.newPacker(out);
				obj.writeMsgpack(packer);
				packer.flush();
				return out.toByteArray();
			} catch(IOException e) { return null; }
		}

		@Override
		public byte[] encodeArray(T[] obj) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				MessagePacker packer = msgpackPackerConfig.newPacker(out);
				encodeArray(obj, packer);
				packer.flush();
				return out.toByteArray();
			} catch(IOException e) { return null; }
		}

		/**
		 * Array decode helper
		 * @param unpacker
		 * @return
		 * @throws IOException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 */
		public T[] decodeArray(MessageUnpacker unpacker) throws IOException {
			try {
				int count = unpacker.unpackArrayHeader();
				T[] result = (T[]) Array.newInstance(claz, count);
				for(int i = 0; i < count; i++)
					result[i] = (claz.newInstance()).readMsgpack(unpacker);
				return result;
			} catch (InstantiationException | IllegalAccessException e) {
				return null;
			}
		}

		/**
		 * Array encode helper
		 * @param obj
		 * @param packer
		 * @throws IOException
		 */
		public void encodeArray(T[] obj, MessagePacker packer) throws IOException {
			int count = obj.length;
			packer.packArrayHeader(count);
			for(T message : obj) {
				message.writeMsgpack(packer);
			}
		}

		private static RuntimeException textException() {
			return new RuntimeException("Msgpack can't process text");
		}
	}

	/**
	 * Concrete subtypes for specific message types
	 */
	public static class MessageCodec extends MsgpackCodec<Message> {
		MessageCodec() {
			super(Message.class);
		}
	}

	public static class PresenceCodec extends MsgpackCodec<PresenceMessage> {
		PresenceCodec() {
			super(PresenceMessage.class);
		}
	}

	public static class ProtocolCodec extends MsgpackCodec<ProtocolMessage> {
		ProtocolCodec() {
			super(ProtocolMessage.class);
		}
	}

	private static Map<Class<? extends Encodable>, Codec<? extends Encodable>> codecs = new HashMap<Class<? extends Encodable>, Codec<? extends Encodable>>();

	private static final PackerConfig msgpackPackerConfig;
	private static final UnpackerConfig msgpackUnpackerConfig;

	static {
		codecs.put(Message.class, (messageCodec = new MessageCodec()));
		codecs.put(PresenceMessage.class, (presenceCodec = new PresenceCodec()));
		codecs.put(ProtocolMessage.class, (protocolCodec = new ProtocolCodec()));

		msgpackPackerConfig = Platform.name.equals("android") ?
				new PackerConfig().withSmallStringOptimizationThreshold(Integer.MAX_VALUE) :
					MessagePack.DEFAULT_PACKER_CONFIG;
	
		msgpackUnpackerConfig = MessagePack.DEFAULT_UNPACKER_CONFIG;
	}
}
