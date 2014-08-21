package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Settings implements CompileParameter, ParameterParser {
	INSTANCE;

	public static enum Option {
		ACTIVE_RECORD("active-record", "Active record pattern in client libraries", "active-record"),
		NO_JACKSON("no-jackson", "Don't use Jackson annotations", "no-jackson"),
		JAVA_BEANS("java-beans", "Add Java Beans support", "java-beans"),
		MANUAL_JSON("manual-json", "JSON without external library", "manual-json"),
        NO_HELPERS("no-helpers", "Don't use helper methods", "no-helpers"),
        DISABLE_COMPANION("disable-companion", "Don't use companion object for scala classes", "disable-companion");

		private final String value;
		private final String description;
		private final String platformName;

		Option(final String value, final String description, final String platformName) {
			this.value = value;
			this.description = description;
			this.platformName = platformName;
		}

		private static Option from(final String value) {
			for (final Option o : Option.values()) {
				if (o.value.equalsIgnoreCase(value)) {
					return o;
				}
			}
			return null;
		}
	}

	private static final String CACHE_NAME = "settings_option_cache";

	public static String parseAndConvert(final Context context) {
		final List<Option> settings = context.load(CACHE_NAME);
		if(settings == null) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		for (final Option o : settings) {
			sb.append(o.platformName);
			sb.append(',');
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}

	private static void listOptions(final Context context) {
		for (final Option o : Option.values()) {
			context.show(o.value + " - " + o.description);
		}
		context.show("Example usages:");
		context.show("		-settings=active-record,no-jackson");
		context.show("		-active-record -no-jackson -manual-json");
	}

	@Override
	public Either<Boolean> tryParse(final String name, final String value, final Context context) {
		if (Option.from(name) != null) {
			if (value != null && value.length() > 0) {
				return Either.fail("Settings parameter detected, but settings don't support arguments. Parameter: " + name);
			}
			context.put(name, null);
			return Either.success(true);
		}
		return Either.success(false);
	}

	@Override
	public boolean check(final Context context) {
		final List<String> settings = new ArrayList<String>();
		if (context.contains(InputParameter.SETTINGS)) {
			final String value = context.get(InputParameter.SETTINGS);
			if (value == null || value.length() == 0) {
				context.error("Settings not provided. Available settings: ");
				listOptions(context);
				return false;
			}
			Collections.addAll(settings, value.split(","));
		}
		for(final Option o : Option.values()) {
			if (context.contains(o.value) && !settings.contains(o.value)) {
				settings.add(o.value);
			}
		}
		if(settings.size() == 0) {
			if (context.contains(InputParameter.SETTINGS)) {
				context.error("Settings not provided. Available settings: ");
				listOptions(context);
				return false;
			}
			return true;
		}
		final List<Option> options = new ArrayList<Option>(settings.size());
		for(final String name : settings) {
			final Option o = Option.from(name);
			if (o == null) {
				context.error("Unknown setting: " + name);
				listOptions(context);
				return false;
			}
			options.add(o);
		}
		context.cache(CACHE_NAME, options);
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Custom compile settings for DSL -> target conversion (Active record, Jackson, etc...)";
	}

	@Override
	public String getDetailedDescription() {
		final StringBuilder sb = new StringBuilder();
		sb.append("DSL Platform compiler supports various compilation options for tweaking target library.\n");
		sb.append("Some options are only available in some languages.\n");
		sb.append("--------------------------------------------------\n");
		for (final Option o : Option.values()) {
			sb.append(o.value).append(" - ").append(o.description).append("\n");
		}
		return sb.toString();
	}
}
