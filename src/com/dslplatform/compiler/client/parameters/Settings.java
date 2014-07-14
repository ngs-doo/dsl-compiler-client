package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.util.Map;

public enum Settings implements CompileParameter {
	INSTANCE;

	public static String parseAndConvert(final Map<InputParameter, String> parameters) {
		final String value = parameters.get(InputParameter.SETTINGS);
		final String[] settingInputs = value != null ? value.split(",") : new String[0];
		final StringBuilder sb = new StringBuilder();
		for(int i=0;i<settingInputs.length;i++) {
			Option s = Option.from(settingInputs[i]);
			sb.append(s.platformName);
			sb.append(',');
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}

	public static enum Option {
		ACTIVE_RECORD("active-record", "Active record pattern in client libraries", "with-active-record"),
		NO_JACKSON("no-jackson", "Don't use Jackson annotations", "no-jackson"),
		MANUAL_JSON("manual-json", "JSON without external library", "manual-json");

		private final String value;
		private final String description;
		private final String platformName;
		Option(final String value, final String description, final String platformName) {
			this.value = value;
			this.description = description;
			this.platformName = platformName;
		}

		private static Option from(final String value) {
			for(final Option o : Option.values()) {
				if (o.value.equalsIgnoreCase(value)) {
					return o;
				}
			}
			return null;
		}
	}

	private static void listOptions() {
		for(final Option o : Option.values()) {
			System.out.println(o.value + " - " + o.description);
		}
		System.out.println("Example usage: -settings=active-record,no-jackson");
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		final String value = parameters.get(InputParameter.SETTINGS);
		final String[] settings = value != null ? value.split(",") : new String[0];
		for(final String s : settings) {
			final Option o = Option.from(s);
			if (o == null) {
				System.out.println("Unknown setting: " + s);
				listOptions();
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "Custom compile settings for DSL -> target conversion (Active record, Jackson, etc...)";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
