package com.dslplatform.compiler.client;

import java.util.*;

public class Main {
	public static void main(String[] args) {
		final Map<InputParameter, String> options = InputParameter.parse(args);
		for (final InputParameter ip : InputParameter.values()) {
			if (!ip.parameter.check(options)) {
				if (ip.parameter.getDetailedDescription() != null) {
					System.out.println();
					System.out.println();
					System.out.println(ip.parameter.getDetailedDescription());
				}
				System.exit(0);
			}
		}
		for (final InputParameter ip : InputParameter.values()) {
			ip.parameter.run(options);
		}
	}
}
