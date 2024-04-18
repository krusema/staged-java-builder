package com.zainlessbrombie.stagedbuilder.sourcebuilder;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldDeclarationItem extends SourceCodeBuilder.SourceItem {
    @NotNull
    private String modifiers;

    @NotNull
    private String type;

    @Nullable
    private String fieldName;

    @Nullable
    private String initializer;


    public FieldDeclarationItem(String modifiers, String type, String fieldName, String initializer) {
        this.modifiers = modifiers;
        this.type = type;
        this.fieldName = fieldName;
        this.initializer = initializer;
    }

    @Override
    public List<String> generateLines() {
        String line = "";

        if (modifiers != null) {
            line += modifiers;
        }
        line += " " + type + " " + fieldName;

        if (initializer != null && !initializer.isEmpty()) {
            line += " = " + initializer;
        }

        if (!line.endsWith(";")) {
            line += ";";
        }

        return new ArrayList<>(Collections.singletonList(line));
    }
}
