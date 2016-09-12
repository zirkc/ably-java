package io.ably.lib.test.common.android;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import io.ably.lib.test.common.ResourceLoader;

public class AssetResourceLoader implements ResourceLoader {
	public byte[] read(String resourceName) throws IOException {
		InputStream is = null;
		byte[] bytes = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream(resourceName);
			Log.v(TAG, "Reading " + is.available() + " bytes for resource " + resourceName);
			bytes = new byte[is.available()];
			is.read(bytes);
		} catch(IOException ioe) {
			Log.e(TAG, "Unexpected exception reading asset resource", ioe);
		} finally {
			if(is != null)
				is.close();
			return bytes;
		}
	}

	private static final String TAG = AssetResourceLoader.class.getName();
}
