package com.dslplatform.compiler.client.api.config;

public interface KeystoreConfiguration {
    /** Classpath or filepath of the keystore */
    public String getPath();

    /** Truststore configuration: JKS, BKS */
    public String getType();

    /** Password for the keystore */
    public char[] getPassword();
}
