package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import java.util.List;

public class StaticBlockItem extends SourceCodeBuilder.SourceItem {

    public void addSourceItem(SourceCodeBuilder.SourceItem item) {
        sourceItems.add(item);
    }

    public StaticBlockItem withSourceItem(SourceCodeBuilder.SourceItem item) {
        addSourceItem(item);
        return this;
    }

    @Override
    public List<String> generateLines() {
        return enclose(
                "static {",
                "}",
                indented(super.generateLines())
        );
    }
}
