package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.*;

import java.util.*;

public class Main {
    public static void main(String[] args) {
		final Map<InputParameter, String> options = InputParameter.parse(args);
		for (final InputParameter ip : InputParameter.values()) {
			if (!ip.parameter.check(options)) {
				InputParameter.showHelpAndExit(false);
			}
		}
		for (final InputParameter ip : InputParameter.values()) {
			ip.parameter.run(options);
		}
    }
}
