package com.zainlessbrombie.stagedbuilder.sourcebuilder;

public class IndentedPrinter {
    private static final String indent = "    ";
    private int indents;
    private boolean isIndented = false;

    private StringBuilder content;

    public void addLine(String ... line) {
        if (!isIndented) {
            appendIndent();
        }
        content.append(String.join("", line));
        content.append("\n");
        isIndented = false;
    }

    public void appendToLine(String ... strs) {
        if (!isIndented) {
            appendIndent();
            isIndented = true;
        }
        content.append(String.join("", strs));
    }

    public void deeper() {
        indents++;
    }

    public void shallower() {
        indents--;
    }

    public void emptyLine() {
        content.append("\n");
        isIndented = false;
    }

    private void appendIndent() {
        for (int i = 0; i < indents; i++) {
            content.append(indent);
        }
    }

    @Override
    public String toString() {
        return content.toString();
    }
}
