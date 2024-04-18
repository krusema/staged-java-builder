package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import java.util.List;

public class InterfaceSourceItem extends SourceCodeBuilder.SourceItem {
    private String modifier;
    private String name;
    private List<String> extendsInterfaces;

    public InterfaceSourceItem(String modifier, String name, List<String> extendsInterfaces) {
        this.modifier = modifier;
        this.name = name;
        this.extendsInterfaces = extendsInterfaces;
    }

    public void addSourceItem(SourceCodeBuilder.SourceItem item) {
        sourceItems.add(item);
    }

    @Override
    public List<String> generateLines() {
        String headerLine = modifier + " interface " + name;
        if (!extendsInterfaces.isEmpty()) {
            headerLine += " extends " + String.join(", ", extendsInterfaces);
        }

        headerLine += " {";

        List<String> content = super.generateLines();

        return enclose(headerLine, "}", indented(content));
    }
}
