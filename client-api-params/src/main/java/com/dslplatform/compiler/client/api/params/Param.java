package com.dslplatform.compiler.client.api.params;

import java.util.Stack;

public interface Param {
    public boolean allowMultiple();

    public void addToPayload(final XMLOut xO);

    public static class XMLOut {
        private final StringBuilder sB;

        public XMLOut(
                final String root) {
            sB = new StringBuilder();
            depth = new Stack<String>();
            start(root);
        }

        // ---------------------------------------------------------------------

        private final Stack<String> depth;

        private void writePadding() {
            for (int i = depth.size(); i > 1; i--) {
                sB.append("    ");
            }
        }

        private void newLine() {
            sB.append('\n');
        }

        // ---------------------------------------------------------------------

        private void open(final String name) {
            depth.add(name);
            writePadding();
            sB.append('<').append(depth.peek()).append('>');
        }

        private void close() {
            sB.append("</").append(depth.pop()).append('>');
            newLine();
        }

        // ---------------------------------------------------------------------

        public XMLOut node(final String name, final String text) {
            open(name);
            text(text);
            close();
            return this;
        }

        public XMLOut start(final String name) {
            open(name);
            newLine();
            return this;
        }

        // format: OFF
        public XMLOut text(
                final String text) {
            sB.append(text.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;"));
            return this;
        }

        public XMLOut end() {
            writePadding();
            close();
            return this;
        }

        // ---------------------------------------------------------------------

        @Override
        public String toString() {
            while (!depth.isEmpty()) {
                end();
            }
            return sB.toString();
        }
    }
}
