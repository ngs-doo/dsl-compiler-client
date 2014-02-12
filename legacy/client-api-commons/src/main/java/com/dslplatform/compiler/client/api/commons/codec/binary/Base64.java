package com.dslplatform.compiler.client.api.commons.codec.binary;

import javax.xml.bind.DatatypeConverter;

public class Base64 {
    public static String encodeBase64String(final byte[] binaryData) {
        return DatatypeConverter.printBase64Binary(binaryData);
    }
}
