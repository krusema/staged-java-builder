package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import java.util.Arrays;
import java.util.List;

public class LineSourceItem extends SourceCodeBuilder.SourceItem {
    public LineSourceItem(String line) {
        sourceItems.add(new SourceCodeBuilder.SourceItem() {
            @Override
            public List<String> generateLines() {
                return Arrays.asList(line);
            }
        });
    }
}
