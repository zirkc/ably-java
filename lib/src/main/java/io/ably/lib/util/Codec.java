package io.ably.lib.util;

import io.ably.lib.http.Http.RequestBody;

import java.lang.reflect.Array;
import java.nio.charset.Charset;

import io.ably.lib.http.Http.ByteArrayRequestBody;
import io.ably.lib.http.Http.StringRequestBody;
import io.ably.lib.types.AblyException;

public abstract class Codec<T extends Encodable> {

	public enum Format {
		json,
		msgpack,
		bson
	}

	public static <T extends Encodable> Codec<T> get(Format format, Class<T> claz) {
		switch(format) {
		case json:
			return Json.JsonCodec.create(claz);
		case msgpack:
			return Msgpack.MsgpackCodec.create(claz);
		case bson:
			return null;//new BsonCodec();
		default:
			return null;
		}
	}

	public static <T extends Encodable> Codec<T> get(String mime, Class<T> claz) {
		switch(mime) {
		case "application/json":
			return Json.JsonCodec.create(claz);
		case "application/x-msgpack":
			return Msgpack.MsgpackCodec.create(claz);
		case "application/x-bson":
			return null;//new BsonCodec();
		default:
			return null;
		}
	}

	public abstract T decode(byte[] encoded) throws AblyException;
	public abstract T decodeFromText(String encoded) throws AblyException;
	public abstract T[] decodeArray(byte[] encoded) throws AblyException;
	public abstract T[] decodeArrayFromText(String encoded) throws AblyException;

	public abstract byte[] encode(T obj);
	public abstract String encodeToText(T obj);
	public abstract byte[] encodeArray(T[] obj);
	public abstract String encodeArrayToText(T[] obj);

	public RequestBody getRequestBody(T obj) {
		return isBinary ? new ByteArrayRequestBody(encode(obj), mime) : new StringRequestBody(encodeToText(obj), mime);
	}

	public RequestBody getRequestBody(T[] obj) {
		return isBinary ? new ByteArrayRequestBody(encodeArray(obj), mime) : new StringRequestBody(encodeArrayToText(obj), mime);
	}

	public T decodeFromRequestBody(RequestBody body) throws AblyException {
		return isBinary ?
				decode(body.getEncoded()) :
				decodeFromText(new String(body.getEncoded(), Charset.forName("UTF-8")));
	}

	public boolean getIsBinary() { return isBinary; }
	public String getMime() { return mime; }

	@SuppressWarnings("unchecked")
	protected Codec(boolean isBinary, String mime, Class<T> claz) {
		this.isBinary = isBinary;
		this.mime = mime;
		this.claz = claz;
		this.arrClaz = (Class<T[]>) Array.newInstance(claz, 0).getClass();
	}

	protected final boolean isBinary;
	protected final String mime;
	protected final Class<T> claz;
	protected final Class<T[]> arrClaz;
}
