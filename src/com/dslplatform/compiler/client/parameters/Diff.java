package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.diff.diff_match_patch;

import java.util.*;

public enum Diff implements CompileParameter {
	INSTANCE;

	public static void compareDsls(final Map<InputParameter, String> parameters) {
		final Map<String, String> currentDsl = DslPath.getCurrentDsl(parameters);
		final Map<String, String> previousDsl = DbConnection.getDatabaseDsl(parameters);

		final Set<String> currentFiles = new HashSet<String>(currentDsl.keySet());
		currentFiles.removeAll(previousDsl.keySet());
		for(final String name : currentFiles) {
			System.out.println("New file: " + name);
			//TODO: options which control whether to show content
			//System.out.println("----------------------------------------------");
			//System.out.println(currentDsl.get(name));
		}
		final Set<String> previousFiles = new HashSet<String>(previousDsl.keySet());
		previousFiles.removeAll(currentDsl.keySet());
		for(final String name : previousFiles) {
			System.out.println("Removed file: " + name);
			//System.out.println("----------------------------------------------");
			//System.out.println(previousDsl.get(name));
		}
		final Set<String> sharedFiles = new HashSet<String>(currentDsl.keySet());
		sharedFiles.retainAll(previousDsl.keySet());
		diff_match_patch diff = new diff_match_patch();
		for(final String name : sharedFiles) {
			String current = currentDsl.get(name);
			String previous = previousDsl.get(name);
			if (current.equals(previous)) {
				continue;
			}
			LinkedList<diff_match_patch.Diff> changes = diff.diff_main(previous, current);
			System.out.println("Changed file: " + name);
			System.out.println("----------------------------------------------");
			final int totalDifs = changes.size();
			int cur = 0;
			for (final diff_match_patch.Diff aDiff : changes) {
				cur++;
				final String text = aDiff.text;
				switch (aDiff.operation) {
					case INSERT:
						System.out.print("[+ ");
						System.out.print(text);
						System.out.print("]");
						break;
					case DELETE:
						System.out.print("[- ");
						System.out.print(text);
						System.out.print("]");
						break;
					case EQUAL:
						String[] lines = text.split("\n");
						if (cur < totalDifs) {
							if (lines.length <= 10) {
								System.out.print(text);
							}
							else {
								int width = 0;
								for (int i=0;i<5;i++) {
									width += lines[i].length() + 1;
								}
								System.out.print(text.substring(0, width));
								width = 0;
								for (int i=Math.max(5, lines.length - 5);i<lines.length;i++) {
									width += lines[i].length() + 1;
								}
								System.out.println();
								System.out.print("...");
								System.out.println();
								System.out.print(text.substring(text.length() - width));
							}
						}
						else {
							if (lines.length <= 5) {
								System.out.print(text);
							}
							int width = 0;
							for (int i=0;i<5;i++) {
								width += lines[i].length() + 1;
							}
							System.out.print(text.substring(0, width));
						}
						break;
				}
			}
			System.out.println();
		}
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.DIFF)) {
			if(!parameters.containsKey(InputParameter.CONNECTION_STRING)) {
				System.out.println("Connection string is required to perform a diff operation");
				System.exit(0);
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.DIFF)) {
			compareDsls(parameters);
		}
	}

	@Override
	public String getShortDescription() {
		return "Diff current DSL files to previous DSL files";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
