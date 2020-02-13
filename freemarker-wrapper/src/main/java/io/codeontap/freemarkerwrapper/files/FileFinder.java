package io.codeontap.freemarkerwrapper.files;
/**
 * Sample code that finds files that match the specified glob pattern.
 * For more information on what constitutes a glob pattern, see
 * https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
 *
 * The file or directories that match the pattern are printed to
 * standard out.  The number of matches is also printed.
 *
 * When executing this application, you must put the glob pattern
 * in quotes, so the shell will not expand any wild cards:
 *              java Find . -name "*.java"
 */

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;


public class FileFinder {

    public static class Finder
            extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private int numMatches = 0;
        private List<Path> matches;
        private boolean ignoreDotDirectories = true;
        private boolean ignoreDotFiles = true;

        public Finder(String pattern, boolean ignoreDotDirectories, boolean ignoreDotFiles) {
            this.ignoreDotDirectories = ignoreDotDirectories;
            this.ignoreDotFiles = ignoreDotFiles;
            matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + pattern);
            matches = new ArrayList<>();
        }

        // Compares the glob pattern against
        // the file or directory name.
        void find(Path file) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                numMatches++;
/*
                System.out.println(file);
*/
                matches.add(file);
            }
        }

        // Prints the total number of
        // matches to standard out.
        public List<Path> done() {
            /*System.out.println("Matched: "
                    + numMatches);*/
            Collections.sort(matches);
            return matches;
        }

        // Invoke the pattern matching
        // method on each file.
        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) {
            if(ignoreDotFiles && file.getFileName().toString().startsWith("."))
                return CONTINUE;
            find(file);
            return CONTINUE;
        }

        // Invoke the pattern matching
        // method on each directory.
        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attrs) {
            if(ignoreDotDirectories && dir.getFileName().toString().startsWith("."))
                return SKIP_SUBTREE;
            find(dir);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                                               IOException exc) {
            System.err.println(exc);
            return CONTINUE;
        }
    }
}