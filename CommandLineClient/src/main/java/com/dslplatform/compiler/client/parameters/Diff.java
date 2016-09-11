package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.diff.diff_match_patch;
import org.fusesource.jansi.Ansi;

import java.util.*;

public enum Diff implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "diff";
	}

	@Override
	public String getUsage() {
		return null;
	}

	private static void compareDsls(final Context context, final Map<String, String> previousDsl) throws ExitException {
		final Map<String, String> currentDsl = DslPath.getCurrentDsl(context);

		final Set<String> currentFiles = new HashSet<String>(currentDsl.keySet());
		currentFiles.removeAll(previousDsl.keySet());
		for (final String name : currentFiles) {
			context.show("New DSL file: " + name + ". Total lines: " + currentDsl.get(name).split("\n").length);
			context.log("----------------------------------------------");
			context.log(currentDsl.get(name));
		}
		final Set<String> previousFiles = new HashSet<String>(previousDsl.keySet());
		previousFiles.removeAll(currentDsl.keySet());
		for (final String name : previousFiles) {
			context.show("Removed DSL file: " + name + ". Total lines: " + previousDsl.get(name).split("\n").length);
			context.log("----------------------------------------------");
			context.log(previousDsl.get(name));
		}
		final Set<String> sharedFiles = new HashSet<String>(currentDsl.keySet());
		sharedFiles.retainAll(previousDsl.keySet());
		diff_match_patch diff = new diff_match_patch();
		boolean hasChanges = false;
		for (final String name : sharedFiles) {
			String current = currentDsl.get(name);
			String previous = previousDsl.get(name);
			if (current.equals(previous)) {
				continue;
			}
			LinkedList<diff_match_patch.Diff> changes = diff.diff_main(previous, current);
			context.show("Changed DSL file: " + name);
			context.show("----------------------------------------------");
			final int totalDiffs = changes.size();
			int cur = 0;
			hasChanges = hasChanges || totalDiffs > 0;
			final StringBuilder sb = new StringBuilder();
			final boolean inColor = !context.contains(DisableColors.INSTANCE);
			for (final diff_match_patch.Diff aDiff : changes) {
				cur++;
				final String text = aDiff.text;
				switch (aDiff.operation) {
					case INSERT:
						if (inColor) {
							sb.append(Context.inColor(Ansi.Color.GREEN, text));
						} else {
							sb.append("[+ ").append(text).append(']');
						}
						break;
					case DELETE:
						if (inColor) {
							sb.append(Context.inColor(Ansi.Color.RED, text));
						} else {
							sb.append("[- ").append(text).append(']');
						}
						break;
					case EQUAL:
						String[] lines = text.split("\n");
						if (cur < totalDiffs) {
							if (lines.length <= 10) {
								sb.append(text);
							} else {
								int width = 0;
								if (cur > 1) {
									for (int i = 0; i < 5; i++) {
										width += lines[i].length() + 1;
									}
									sb.append(text.substring(0, width));
									width = 0;
								}
								for (int i = Math.max(5, lines.length - 5); i < lines.length; i++) {
									width += lines[i].length() + 1;
								}
								sb.append("\n").append("...").append("\n");
								sb.append(text.substring(text.length() - width));
							}
						} else {
							if (lines.length <= 5) {
								sb.append(text);
							}
							int width = 0;
							for (int i = 0; i < 5; i++) {
								width += lines[i].length() + 1;
							}
							sb.append(text.substring(0, width));
						}
						break;
				}
			}
			context.show(sb.toString());
			context.show();
		}
		if (currentFiles.size() == 0 && previousFiles.size() == 0 && !hasChanges) {
			context.show("No changes found in DSL");
		}
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		if (context.contains(INSTANCE)) {
			if (!context.contains(PostgresConnection.INSTANCE)
					&& !context.contains(OracleConnection.INSTANCE)) {
				context.error("Connection string is required to perform a diff operation.\n" +
						"Neither Oracle or Postgres connection string detected");
				throw new ExitException();
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) throws ExitException {
		if (context.contains(INSTANCE)) {
			if (context.contains(PostgresConnection.INSTANCE)) {
				final Map<String, String> previousDsl = PostgresConnection.getDatabaseDsl(context);
				context.show("Comparing Postgres DSL diff...");
				context.show();
				compareDsls(context, previousDsl);
			}
			if (context.contains(OracleConnection.INSTANCE)) {
				final Map<String, String> previousDsl = OracleConnection.getDatabaseDsl(context);
				context.show("Comparing Oracle DSL diff...");
				context.show();
				compareDsls(context, previousDsl);
			}
		}
	}

	@Override
	public String getShortDescription() {
		return "Diff current DSL files to previous DSL files";
	}

	@Override
	public String getDetailedDescription() {
		return "Provide diff on changed DSL files. For fast confirmation of changes done before creating new library models or SQL migrations.\n" +
				"Diff requires read-only access to -DSL- schema in Postgres/Oracle database where previously applied DSL is stored.\n" +
				"To disable diff confirmation, use no prompt parameter.";
	}
}
