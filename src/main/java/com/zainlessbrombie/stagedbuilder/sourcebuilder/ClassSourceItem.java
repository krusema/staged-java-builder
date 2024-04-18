package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import java.util.List;

public class ClassSourceItem extends SourceCodeBuilder.SourceItem {
    ;
    private String modifier;
    private String name;
    private String extendsClass;
    private List<String> interfaces;

    public ClassSourceItem(String modifier, String name, String extendsClass, List<String> interfaces) {
        this.modifier = modifier;
        this.name = name;
        this.extendsClass = extendsClass;
        this.interfaces = interfaces;
    }

    public void addSourceItem(ConstructorSourceItem item) {
        sourceItems.add(item);
    }

    public void addSourceItem(MethodSourceItem item) {
        sourceItems.add(item);
    }

    public void addSourceItem(ClassSourceItem item) {
        sourceItems.add(item);
    }

    public void addSourceItem(InterfaceSourceItem item) {
        sourceItems.add(item);
    }


    public void addSourceItem(FieldDeclarationItem item) {
        sourceItems.add(item);
    }

    public void addSourceItem(StaticBlockItem item) {
        sourceItems.add(item);
    }


    @Override
    public List<String> generateLines() {
        String headerLine = modifier + " class " + name;
        if (extendsClass != null) {
            headerLine += " extends " + extendsClass;
        }
        if (!interfaces.isEmpty()) {
            headerLine += " implements " + String.join(", ", interfaces);
        }

        headerLine += " {";

        List<String> content = super.generateLines();

        return enclose(headerLine, "}", indented(content));
    }

    public String getName() {
        return name;
    }
}
