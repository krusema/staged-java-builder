package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import java.util.Arrays;
import java.util.List;

public class InterfaceMethodSourceItem extends SourceCodeBuilder.SourceItem {
    private String modifier;
    private String returnType;
    private String name;
    private List<String> args;

    public InterfaceMethodSourceItem(String modifier, String returnType, String name, List<String> args) {
        this.modifier = modifier;
        this.returnType = returnType;
        this.name = name;
        this.args = args;
    }

    @Override
    public List<String> generateLines() {
        return Arrays.asList(modifier + " " + returnType + " " + name
                + "(" + String.join(",", args) + ");");
    }
}
