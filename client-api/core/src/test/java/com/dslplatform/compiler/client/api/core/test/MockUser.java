package com.dslplatform.compiler.client.api.core.test;

import com.dslplatform.compiler.client.api.config.Tokenizer;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

public class MockUser {

    public static final String validUser = "ocd@dsl-platform.com";
    public static final String validPassword = "ocdcdo";

    public static final String inValidUser = "cdo@dsl-platform.com";
    public static final String inValidPassword = "cdoodc";

    public static final String validID = "d7b8ee4d-2226-49b5-a5aa-74af1d29e644";
    public static final String inValidID = "c3cc0007-5dc2-4fd0-b14b-0dcbfce1e4bf";

    public static final Charset ENCODING = Charset.forName("UTF-8");

    public static final String projectToken(
            final String user,
            final String pass,
            final String projectid) {
        return "Token " + Tokenizer.makeToken(user, pass, projectid);
    }

    public static final String userToken(
            final String user,
            final String pass) {
        return "Token " + Tokenizer.makeToken(user, pass);
    }
}
