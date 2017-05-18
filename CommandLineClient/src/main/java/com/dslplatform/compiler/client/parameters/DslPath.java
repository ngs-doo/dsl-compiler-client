package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.util.*;

public enum DslPath implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "dsl";
	}

	@Override
	public String getUsage() {
		return "path";
	}

	private static final String CACHE_MAP_NAME = "current_dsl_map_cache";
	private static final String CACHE_FILE_NAME = "current_dsl_file_cache";

	public static Map<String, String> getCurrentDsl(final Context context) throws ExitException {
		final Map<String, String> cache = context.load(CACHE_MAP_NAME);
		if (cache != null) {
			return cache;
		}
		findDsls(context);
		return context.load(CACHE_MAP_NAME);
	}

	public static List<File> getDslPaths(final Context context) throws ExitException {
		final List<File> cache = context.load(CACHE_FILE_NAME);
		if (cache != null) {
			return cache;
		}
		findDsls(context);
		return context.load(CACHE_FILE_NAME);
	}

	private static void findDsls(final Context context) throws ExitException {
		String value = context.get(INSTANCE);
		if (value == null) {
			if (!(new File("./dsl").exists())) {
				context.error("DSL path not provided. Can't use default path (./dsl) since it doesn't exists");
				throw new ExitException();
			}
			context.put(INSTANCE, value = "./dsl");
		}
		final List<File> allDslFiles = new ArrayList<File>();
		final Map<String, String> dslMap = new LinkedHashMap<String, String>();
		for (final String part : value.split(File.pathSeparator)) {
			final File dslPath = new File(part).getAbsoluteFile();
			final List<File> dslFiles = dslPath.isFile()
					? Collections.singletonList(dslPath)
					: Utils.findFiles(context, dslPath, Arrays.asList(".dsl", ".ddd"));
			final int pathLen = dslPath.getAbsolutePath().length();
			for (final File file : dslFiles) {
				if (!file.canRead()) {
					context.error("Can't read DSL file: " + file.getName());
					throw new ExitException();
				}
				final Either<String> content = Utils.readFile(file);
				if (content.isSuccess()) {
					final String relativeName = file.getAbsolutePath().substring(pathLen);
					if (dslMap.containsKey(relativeName)) {
						context.warning("Duplicate DSL file specified: " + file.getAbsolutePath() + " from base path: " + part);
					} else {
						dslMap.put(relativeName, content.get());
						allDslFiles.add(file);
					}
				} else {
					context.error("Error reading DSL file: " + file.getName());
					context.error(content.whyNot());
					throw new ExitException();
				}
			}
		}
		context.cache(CACHE_MAP_NAME, dslMap);
		context.cache(CACHE_FILE_NAME, allDslFiles);
	}

	@Override
	public boolean check(final Context context) {
		final String value = context.get(INSTANCE);
		if (value == null) {
			final File dslPath = new File("./dsl");
			if (!dslPath.exists()) {
				context.error("DSL path not provided. Can't use default path (./dsl) since it doesn't exists");
				return false;
			}
			context.put(INSTANCE, "./dsl");
		} else {
			final File dslPath = new File(value);
			if (!dslPath.exists()) {
				context.error("Provided DSL path (" + value + ") does not exists. Please provide valid path to DSL files");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Path(s) to DSL files";
	}

	@Override
	public String getDetailedDescription() {
		return "Domain Specification Language files contain description of domain model in a language leveraging DDD (Domain-Driven-Design) concepts.\n" +
				"DSL files should contain descriptions of data structures used through the application and from which various parts of the application will be maintained.\n" +
				"Snapshot of DSL files will be saved to the database, for future comparison on database migrations.\n" +
				"\n" +
				"UTF-8 will be assumed for DSL files.\n" +
				".dsl and .ddd extensions are supported.\n\n" +
				"Multiple files/paths can be specified via path separator.";
	}
}
