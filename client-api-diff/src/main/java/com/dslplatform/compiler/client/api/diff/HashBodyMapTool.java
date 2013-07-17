package com.dslplatform.compiler.client.api.diff;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import com.dslplatform.compiler.client.api.commons.Hash;

public class HashBodyMapTool {
    public static Map<String, PathAction> compareHashBodyMaps(
            final Map<Hash, SortedSet<String>> oldBodies,
            final Map<Hash, SortedSet<String>> newBodies) {

        if (oldBodies == null)
            throw new IllegalArgumentException("oldBodies cannot be null");
        if (newBodies == null)
            throw new IllegalArgumentException("newBodies cannot be null");

        final TreeMap<String, PathAction> ret = new TreeMap<String, PathAction>();

        final Map<String, Hash> oldMap = new TreeMap<String, Hash>();
        for (final Map.Entry<Hash, SortedSet<String>> oldEntry : oldBodies
                .entrySet()) {
            final Hash oldHash = oldEntry.getKey();
            final SortedSet<String> oldPaths = oldEntry.getValue();

            for (final String oldPath : oldPaths) {
                oldMap.put(oldPath, oldHash);
            }
        }

        for (final Map.Entry<Hash, SortedSet<String>> newEntry : newBodies
                .entrySet()) {
            final SortedSet<String> destinationPaths = newEntry.getValue();
            final Hash destinationHashBody = newEntry.getKey();

            for (final String destinationPath : destinationPaths) {
                final Hash sourceHashBody = oldMap.get(destinationPath);

                if (sourceHashBody != null) {
                    // old file exists at new file path
                    if (destinationHashBody.equals(sourceHashBody)) {
                        // check if file was changed
                        ret.put(destinationPath, new PathAction(
                                destinationPath, null, sourceHashBody,
                                ChangeAction.NO_CHANGE));
                    } else {
                        ret.put(destinationPath, new PathAction(
                                destinationPath, null, sourceHashBody,
                                ChangeAction.MODIFIED));
                    }
                    oldMap.remove(destinationPath);
                } else { // old file doesnt exist
                    final SortedSet<String> oldPaths = oldBodies
                            .get(destinationHashBody);
                    if (oldPaths != null) {
                        // file exists somewhere else assume first in set
                        final String sourcePath = oldPaths.first();
                        ret.put(sourcePath, new PathAction(sourcePath,
                                destinationPath, sourceHashBody,
                                ChangeAction.MOVED));

                        // cleanup
                        oldPaths.remove(sourcePath);
                        if (oldPaths.isEmpty())
                            oldBodies.remove(destinationHashBody);
                        oldMap.remove(sourcePath);
                    } else {
                        // hashbody not found in oldBodies, mark file as new or
                        // copy file is new if first in set, otherwise copy of
                        // first in set
                        final String firstDestinationPath = destinationPaths
                                .first();
                        if (destinationPath.equals(firstDestinationPath)) {
                            ret.put(destinationPath, new PathAction(
                                    destinationPath, null, destinationHashBody,
                                    ChangeAction.CREATED));
                        } else {
                            ret.put(destinationPath, new PathAction(
                                    firstDestinationPath, destinationPath,
                                    destinationHashBody, ChangeAction.COPY));
                        }
                    }
                }
            }
        }

        // leftover files in oldMap are to be deleted
        for (final Map.Entry<String, Hash> oldEntry : oldMap.entrySet()) {
            final String sourcePath = oldEntry.getKey();
            ret.put(sourcePath,
                    new PathAction(sourcePath, null, oldEntry.getValue(),
                            ChangeAction.DELETED));
        }

        return ret;
    }
}
