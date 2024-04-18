package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import java.util.List;

public class MethodSourceItem extends SourceCodeBuilder.SourceItem {
    private String modifier;
    private String returnType;
    private String name;
    private List<String> args;

    public MethodSourceItem(String modifier, String returnType, String name, List<String> args) {
        this.modifier = modifier;
        this.returnType = returnType;
        this.name = name;
        this.args = args;
    }

    public void addSourceItem(SourceCodeBuilder.SourceItem item) {
        sourceItems.add(item);
    }

    public MethodSourceItem withSourceItem(SourceCodeBuilder.SourceItem item) {
        addSourceItem(item);
        return this;
    }

    @Override
    public List<String> generateLines() {
        return enclose(
                modifier + " " + returnType + " " + name
                        + "(" + String.join(",", args) + ") {",
                "}",
                indented(super.generateLines())
        );
    }
}
