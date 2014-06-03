package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.commons.FileLoader;
import com.dslplatform.compiler.client.api.commons.Hash;
import com.dslplatform.compiler.client.api.diff.HashBodyMapTool;
import com.dslplatform.compiler.client.api.diff.PathAction;
import com.dslplatform.compiler.client.api.diff.diff_match_patch;
import org.apache.commons.io.Charsets;
import org.eclipse.jgit.diff.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

public class DiffProcessor {

    public static String jGitDiff(
            final Map<String, String> olddsl,
            final Map<String, String> newdsl) {
        StringBuilder sb = new StringBuilder();
        FileLoader oldfl = makeFL(olddsl);
        FileLoader newfl = makeFL(newdsl);

        final Map<Hash, SortedSet<String>> oldHash = oldfl.getHashBodyMap();

        final Map<Hash, SortedSet<String>> newHash = newfl.getHashBodyMap();

        final Map<String, PathAction> actions = HashBodyMapTool.compareHashBodyMaps(oldHash, newHash);

        final SortedMap<String, byte[]> oldFiles = oldfl.getBodies();
        final SortedMap<String, byte[]> newFiles = newfl.getBodies();

        for (final Map.Entry<String, PathAction> hashAction : actions.entrySet()) {
            final PathAction action = hashAction.getValue();

            switch (action.action) {
                case NO_CHANGE:
                    sb.append("No Change ").append(action.source);
                    break;

                case SKIPPED:
                    sb.append("Skipped ").append(action.source);
                    break;

                case CREATED_DIR:
                    break;

                case CREATED:
                    sb.append("Created ").append(action.source);
                    break;

                case MODIFIED:
                    sb.append("Modified ").append(action.source);
                    sb.append(action.source);

                    histogramDiff(sb, oldFiles.get(action.source), newFiles.get(action.source));
                    break;

                case MOVED:
                    sb.append("Moved ").append(action.source).append("->").append(action.destination);
                    break;

                case COPY:
                    sb.append("Copied ").append(action.source);
                    break;

                case DELETED_DIR:
                    sb.append("Deleted directory ").append(action.source);
                    break;

                case DELETED:
                    sb.append("Deleted file ").append(action.source);
                    break;
            }
        }

        return sb.toString();
    }

    private static FileLoader makeFL(final Map<String, String> fileMap) {
        FileLoader fileLoader = new FileLoader();
        for (final Map.Entry<String, String> file : fileMap.entrySet())
            fileLoader.addBytes(file.getKey(), file.getValue().getBytes(Charsets.UTF_8));
        return fileLoader;
    }

    public static void histogramDiff(final StringBuilder sb, final byte[] file1, final byte[] file2) {
        RawText rt1 = new RawText(file1);
        RawText rt2 = new RawText(file2);

        EditList diffList = new EditList();
        diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT, rt1, rt2));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            new DiffFormatter(baos).format(diffList, rt1, rt2);
        } catch (IOException e) {
            sb.append(e.getMessage());
        }

        String some = new String(baos.toByteArray(), Charsets.UTF_8);
        sb.append(some);
    }


    public static void diffMatchPatch(final StringBuilder sb, final byte[] ba1, final byte[] ba2) {
        String text1 = new String(ba1);
        String text2 = new String(ba2);
        diff_match_patch dmp = new diff_match_patch();
        for (diff_match_patch.Diff diff : dmp.diff_main(text1, text2)) {
            String operation = null;
            switch (diff.operation) {
                case DELETE:
                    operation = "\u001B[33m";
                    break;

                case INSERT:
                    operation = "\u001B[35m";
                    break;
                case EQUAL:
                    operation = "";
                    break;
                default:
            }
            sb.append(operation);
            sb.append(diff.text + "\u001B[0m");
        }
    }
}
