package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import java.util.List;

public class ConstructorSourceItem extends SourceCodeBuilder.SourceItem {
    private String className;

    private String modifier;
    private List<String> args;


    public ConstructorSourceItem(ClassSourceItem classSourceItem, String modifier, List<String> args) {
        this.className = classSourceItem.getName();
        this.modifier = modifier;
        this.args = args;
    }

    public void addSourceItem(SourceCodeBuilder.SourceItem item) {
        sourceItems.add(item);
    }

    @Override
    public List<String> generateLines() {
        return enclose(
                modifier + " " + className + "(" + String.join(",", args) + ") {",
                "}",
                indented(super.generateLines())
        );
    }
}
