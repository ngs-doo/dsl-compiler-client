package com.dslplatform.compiler.client.api.config;

public class SimpleKeystoreConfiguration implements KeystoreConfiguration {
    private final String path;
    private final String type;
    private final String password;

    public SimpleKeystoreConfiguration(final String path, final String type, final char[] password) {
        this.type = type;
        this.path = path;
        this.password = new String(password);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public char[] getPassword() {
        return password.toCharArray();
    }
}
