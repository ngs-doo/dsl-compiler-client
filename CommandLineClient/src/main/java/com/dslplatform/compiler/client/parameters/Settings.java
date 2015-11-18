package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Settings implements CompileParameter, ParameterParser {
	INSTANCE;

	@Override
	public String getAlias() { return "settings"; }
	@Override
	public String getUsage() { return "options"; }

	public enum Option {
		ACTIVE_RECORD("active-record", "Active record pattern in client libraries"),
		UTC("utc", "Timestamp should use UTC by default"),
		JACKSON("jackson", "Add Jackson annotations"),
		JAVA_BEANS("java-beans", "Add Java Beans support"),
		MANUAL_JSON("manual-json", "Add optimized serialization/deserialization methods"),
		NO_HELPERS("no-helpers", "Don't use helper methods"),
		MULTI_TENANCY("multi-tenancy", "Support Multi-tenancy on server"),
		LEGACY("legacy", "Legacy methods"),
		SOURCE_ONLY("source-only", "Only provide source code, don't run post-step (compilation)"),
		JODA_TIME("joda-time", "Use Joda Time library (instead of Java time API)"),
		NO_PREPARE_EXECUTE("no-prepare-execute", "Don't use PREPARE/EXECUTE statements in Postgres"),
		MINIMAL_SERIALIZATION("minimal-serialization", "Minimize serialization output (omit default values)"),
		DISABLE_COMPANION("disable-companion", "Don't use companion object for scala classes");

		private final String value;
		private final String description;

		Option(final String value, final String description) {
			this.value = value;
			this.description = description;
		}

		private static Option from(final String value) {
			for (final Option o : Option.values()) {
				if (o.value.equalsIgnoreCase(value)) {
					return o;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	private static final String CACHE_NAME = "settings_option_cache";

	public static List<Option> get(final Context context) {
		return context.load(CACHE_NAME);
	}

	private static void listOptions(final Context context) {
		for (final Option o : Option.values()) {
			context.show(o.value + " - " + o.description);
		}
		context.show("Example usages:");
		context.show("\tsettings=active-record,joda-time,jackson");
		context.show("\tactive-record manual-json");
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
		if (context.contains(INSTANCE)) {
			final String value = context.get(INSTANCE);
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
			if (context.contains(INSTANCE)) {
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
