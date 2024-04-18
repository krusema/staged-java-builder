package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Simple helper class to build Java source code
 */
public class SourceCodeBuilder {
    private List<SourceItem> items = new ArrayList<>();
    private static final String INDENT = "    ";

    public List<String> generateLines() {
        return items.stream()
                .flatMap(item -> item.generateLines().stream())
                .collect(Collectors.toList());
    }

    public void addPackageDeclaration(String packagePath) {
        assert items.isEmpty();

        items.add(new LineSourceItem("package " + packagePath + ";"));
    }

    public void addImport(String importedItem) {
        items.add(new LineSourceItem("import " + importedItem + ";"));
    }

    public void addSourceItem(SourceItem item) {
        items.add(item);
    }

    public static abstract class SourceItem {
        protected List<SourceItem> sourceItems = new ArrayList<>();

        public List<String> generateLines() {
            return sourceItems.stream()
                    .flatMap(item -> item.generateLines().stream())
                    .collect(Collectors.toList());
        }

        public static List<String> indented(List<String> lines) {
            return lines.stream()
                    .map(line -> INDENT + line)
                    .collect(Collectors.toList());
        }

        public static List<String> enclose(String opener, String closer, List<String> content) {
            List<String> ret = new ArrayList<>();
            ret.add(opener);
            ret.addAll(indented(content));
            ret.add(closer);
            return ret;
        }
    }

}
