package com.dslplatform.compiler.client.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public abstract class JavaSerialization {
    public static <T> byte[] serialize(final T t) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(
                new DeflaterOutputStream(baos));
        oos.writeObject(t);
        oos.close();
        return baos.toByteArray();
    }

    public static <T> T deserialize(final byte[] buffer, final Class<T> clazz)
            throws IOException {
        final ObjectInputStream ois = new ObjectInputStream(
                new InflaterInputStream(new ByteArrayInputStream(buffer)));
        try {
            return clazz.cast(ois.readObject());
        } catch (final ClassNotFoundException e) {
            throw new IOException("Could not deserialize object of class: "
                    + clazz.getSimpleName(), e);
        } finally {
            ois.close();
        }
    }
}
