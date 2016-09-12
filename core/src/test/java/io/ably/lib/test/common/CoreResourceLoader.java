package io.ably.lib.test.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by SerhiiKukurik on 9/5/16.
 */
public class CoreResourceLoader implements ResourceLoader {
    @Override
    public byte[] read(String resourceName) throws IOException {
        System.err.println("loading .... " + new File("../core/src/test/resources", resourceName).getAbsolutePath());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File("../core/src/test/resources", resourceName));
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            return bytes;
        } finally {
            if(fis != null)
                fis.close();
        }
    }
}
