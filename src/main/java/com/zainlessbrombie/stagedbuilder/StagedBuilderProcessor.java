package com.zainlessbrombie.stagedbuilder;


import com.google.auto.service.AutoService;
import com.zainlessbrombie.stagedbuilder.sourcebuilder.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The main entry point for this Processor.
 * Processor registration is handled automatically by google auto service
 */
@SupportedAnnotationTypes("com.zainlessbrombie.stagedbuilder.StagedBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class StagedBuilderProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> registeredAnnotations, RoundEnvironment roundEnvironment) {
        // Nested loop iterates through annotated classes
        for (TypeElement typeElement : registeredAnnotations) {
            for (Element element : roundEnvironment.getElementsAnnotatedWith(typeElement)) {
                // Since only @StagedBuilder is a registered annotation, this block is only executed for
                // elements annotated with @StagedBuilder
                if (element instanceof TypeElement) {
                    TypeElement annotatedType = ((TypeElement) element);
                    try {
                        // process .java
                        processAnnotatedClass(annotatedType);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return false;
    }


    /**
     * Build the builder class and write it to a .java file.
     */
    private void processAnnotatedClass(TypeElement annotatedClass) throws IOException {
        List<VariableElement> allBuildableFields = determineBuildableFields(annotatedClass);

        List<VariableElement> requiredBuildableFields = new ArrayList<>();
        List<VariableElement> optionalBuildableFields = new ArrayList<>();

        for (VariableElement field : allBuildableFields) {
            if (isOptional(field)) {
                optionalBuildableFields.add(field);
            } else {
                requiredBuildableFields.add(field);
            }
        }

        String fullNameOfAnnotatedClass = annotatedClass.getQualifiedName().toString();

        String packageNameOfAnnotatedClass = null;

        int lastDot = fullNameOfAnnotatedClass.lastIndexOf('.');
        if (lastDot > 0) {
            // TODO inner classes are not supported right now
            packageNameOfAnnotatedClass = fullNameOfAnnotatedClass.substring(0, lastDot);
        }
        String simpleNameOfAnnotatedClass = fullNameOfAnnotatedClass.substring(lastDot + 1);


        String builderFQClassName = fullNameOfAnnotatedClass + "Builder";
        String builderSimpleName = simpleNameOfAnnotatedClass + "Builder";

        SourceCodeBuilder source = new SourceCodeBuilder();

        source.addPackageDeclaration(packageNameOfAnnotatedClass);

        ClassSourceItem outermostBuilderClass = new ClassSourceItem(
                "public",
                builderSimpleName,
                null,
                new ArrayList<>());
        source.addSourceItem(outermostBuilderClass);
        outermostBuilderClass.addSourceItem(new ConstructorSourceItem(outermostBuilderClass, "private ", new ArrayList<>()));

        ClassSourceItem stagedBuilderScopeClass = new ClassSourceItem(
                "public static",
                "S",
                null,
                new ArrayList<>()
        );
        outermostBuilderClass.addSourceItem(stagedBuilderScopeClass);

        List<InterfaceSourceItem> builderInterfaces = generateStageInterfaces(
                annotatedClass,
                requiredBuildableFields,
                optionalBuildableFields
        );

        for (InterfaceSourceItem builderInterface : builderInterfaces) {
            stagedBuilderScopeClass.addSourceItem(builderInterface);
        }

        String initialStageName;
        if (!requiredBuildableFields.isEmpty()) {
            initialStageName = capitalize(requiredBuildableFields.get(0).getSimpleName().toString()) + "Stage";
        } else {
            initialStageName = "FinalStage";
        }

        outermostBuilderClass.addSourceItem(generateBuilderCreator(initialStageName));

        ClassSourceItem actualBuilder = new ClassSourceItem(
                "public static",
                "Builder",
                null,
                Stream.concat(requiredBuildableFields
                                        .stream()
                                        .map(vE -> "S." + capitalize(vE.getSimpleName().toString()) + "Stage"),
                                Stream.of("S.FinalStage")
                        )
                        .collect(Collectors.toList())
        );

        outermostBuilderClass.addSourceItem(actualBuilder);

        actualBuilder.addSourceItem(new FieldDeclarationItem("", annotatedClass.getQualifiedName().toString(), " building", "new " + annotatedClass.getQualifiedName() + "();"));

        if (fieldAccessMethod(annotatedClass) == BuilderFieldAccessMethod.REFLECTION) {
            for (VariableElement buildableField : allBuildableFields) {
                actualBuilder.addSourceItem(new FieldDeclarationItem("private static", "java.lang.reflect.Field", "fieldAccessor_" + buildableField.getSimpleName(), null));

                StaticBlockItem initializerBlock = new StaticBlockItem();

                initializerBlock.addSourceItem(new LineSourceItem("try {"));
                initializerBlock.addSourceItem(new LineSourceItem(
                        "    fieldAccessor_" + buildableField.getSimpleName()
                                + " = "
                                + annotatedClass.getQualifiedName() + ".class.getDeclaredField(\"" + buildableField.getSimpleName() + "\");"));
                initializerBlock.addSourceItem(new LineSourceItem("    fieldAccessor_" + buildableField.getSimpleName() + ".setAccessible(true);"));
                initializerBlock.addSourceItem(new LineSourceItem("} catch (NoSuchFieldException e) {"));
                initializerBlock.addSourceItem(new LineSourceItem("    throw new RuntimeException(\"Builder could not find member field\", e);"));
                initializerBlock.addSourceItem(new LineSourceItem("}"));
                actualBuilder.addSourceItem(initializerBlock);
            }
        }

        List<MethodSourceItem> builderMethods = generateActualBuilderMethods(annotatedClass, requiredBuildableFields, optionalBuildableFields);

        for (MethodSourceItem builderMethod : builderMethods) {
            actualBuilder.addSourceItem(builderMethod);
        }

        writeOutcomeToFile(builderFQClassName, source);
    }

    private void writeOutcomeToFile(String builderFQClassName, SourceCodeBuilder source) throws IOException {
        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(builderFQClassName);


        try (PrintWriter writer = new PrintWriter(builderFile.openWriter())) {
            for (String line : source.generateLines()) {
                writer.println(line);
                // System.out.println(line);
            }
        }
    }

    private List<MethodSourceItem> generateActualBuilderMethods(
            TypeElement annotatedClass,
            List<VariableElement> requiredBuildableFields,
            List<VariableElement> optionalBuildableFields
    ) {
        List<VariableElement> combinedElements = new ArrayList<>(requiredBuildableFields);
        combinedElements.addAll(optionalBuildableFields);

        List<MethodSourceItem> builderMethods = new ArrayList<>();

        for (int i = 0; i < combinedElements.size(); i++) {
            VariableElement buildableField = combinedElements.get(i);


            String nextStageName;
            if (i + 1 < requiredBuildableFields.size()) {
                VariableElement nextUp = requiredBuildableFields.get(i + 1);
                nextStageName = "S." + capitalize(nextUp.getSimpleName().toString()) + "Stage";
            } else {
                nextStageName = "S.FinalStage";
            }

            MethodSourceItem builderMethod = new MethodSourceItem(
                    "public",
                    nextStageName,
                    buildableField.getSimpleName().toString(),
                    Collections.singletonList(last(buildableField.asType().toString().split(" ")).replaceAll("\\)$", "") + " " + buildableField.getSimpleName())
            );
            addFieldSetterLines(builderMethod, buildableField, annotatedClass);
            builderMethod.addSourceItem(new LineSourceItem("return this;"));

            builderMethods.add(builderMethod);
        }
        MethodSourceItem buildMethod = new MethodSourceItem(
                "public",
                annotatedClass.getQualifiedName().toString(),
                "build",
                new ArrayList<>()
        );

        String validatorMethod = validatorMethod(annotatedClass);
        if (!validatorMethod.isEmpty()) {
            if (validatorMethod.endsWith(";")) {
                validatorMethod = validatorMethod.substring(0, validatorMethod.length() - 1);
            }
            if (!validatorMethod.endsWith(")")) {
                validatorMethod += "()";
            }
            buildMethod.addSourceItem(new LineSourceItem("building." + validatorMethod + ";"));
        }

        buildMethod.addSourceItem(new LineSourceItem("return building;"));
        builderMethods.add(buildMethod);
        return builderMethods;
    }


    /**
     * Add a line to the given builderMethod that sets the field specified by buildableField according to the access technique specified
     * in the @StagedBuilder annotation on annotatedClass
     */
    private void addFieldSetterLines(MethodSourceItem builderMethod, VariableElement buildableField, TypeElement annotatedClass) {
        BuilderFieldAccessMethod accessMethod = fieldAccessMethod(annotatedClass);
        if (accessMethod == BuilderFieldAccessMethod.SETTER) {
            builderMethod.addSourceItem(new LineSourceItem("building.set" + capitalize(buildableField.getSimpleName().toString()) + "(" + buildableField.getSimpleName() + ");"));
        }
        if (accessMethod == BuilderFieldAccessMethod.DIRECT_WRITE) {
            builderMethod.addSourceItem(new LineSourceItem("building." + buildableField.getSimpleName().toString() + " = " + buildableField.getSimpleName() + ";"));
        }
        if (accessMethod == BuilderFieldAccessMethod.REFLECTION) {
            builderMethod.addSourceItem(new LineSourceItem("try {"));
            builderMethod.addSourceItem(new LineSourceItem("    fieldAccessor_" + buildableField.getSimpleName() + ".set(building, " + buildableField.getSimpleName() + ");"));
            builderMethod.addSourceItem(new LineSourceItem("} catch (IllegalAccessException e) {"));
            builderMethod.addSourceItem(new LineSourceItem("    throw new RuntimeException(\"Internal Builder error: This should be accessible\", e);"));
            builderMethod.addSourceItem(new LineSourceItem("}"));
        }
    }

    private MethodSourceItem generateBuilderCreator(String stageName) {
        return new MethodSourceItem(
                "public static",
                "S." + stageName,
                "create",
                new ArrayList<>()
        ).withSourceItem(new LineSourceItem("return new Builder();"));
    }

    private List<InterfaceSourceItem> generateStageInterfaces(TypeElement annotatedClass,
                                                              List<VariableElement> requiredBuildableFields,
                                                              List<VariableElement> optionalBuildableFields) {
        List<InterfaceSourceItem> builderInterfaces = new ArrayList<>();

        for (int i = 0; i < requiredBuildableFields.size(); i++) {
            VariableElement buildableField = requiredBuildableFields.get(i);

            InterfaceSourceItem builderStageInterface = new InterfaceSourceItem(
                    "public",
                    capitalize(buildableField.getSimpleName().toString()) + "Stage",
                    new ArrayList<>()
            );
            builderInterfaces.add(builderStageInterface);

            String nextStageName;
            if (i + 1 < requiredBuildableFields.size()) {
                VariableElement nextUp = requiredBuildableFields.get(i + 1);
                nextStageName = capitalize(nextUp.getSimpleName().toString()) + "Stage";
            } else {
                nextStageName = "FinalStage";
            }

            builderStageInterface.addSourceItem(new InterfaceMethodSourceItem(
                    "",
                    nextStageName,
                    buildableField.getSimpleName().toString(),
                    Collections.singletonList(last(buildableField.asType().toString().split(" ")).replaceAll("\\)$", "") + " " + buildableField.getSimpleName())
            ));
        }

        InterfaceSourceItem finalStageInterface = new InterfaceSourceItem(
                "public static",
                "FinalStage",
                new ArrayList<>()
        );
        for (VariableElement optionalBuildableField : optionalBuildableFields) {
            finalStageInterface.addSourceItem(new InterfaceMethodSourceItem(
                    "",
                    "FinalStage",
                    optionalBuildableField.getSimpleName().toString(),
                    Collections.singletonList(last(optionalBuildableField.asType().toString().split(" ")).replaceAll("\\)$", "") + " " + optionalBuildableField.getSimpleName())
            ));
        }
        finalStageInterface.addSourceItem(new InterfaceMethodSourceItem(
                "",
                annotatedClass.getQualifiedName().toString(),
                "build",
                new ArrayList<>()
        ));
        builderInterfaces.add(finalStageInterface);
        return builderInterfaces;
    }

    /**
     * Returns all fields of the given class that are neither final nor static and are not @BuilderIgnored either.
     */
    private List<VariableElement> determineBuildableFields(TypeElement annotatedClass) {
        return annotatedClass.getEnclosedElements()
                .stream()
                .filter(el -> el instanceof VariableElement)
                .map(el -> (VariableElement) el)
                .filter(el -> !el.getModifiers().contains(Modifier.FINAL) && !el.getModifiers().contains(Modifier.STATIC))
                .filter(el -> el.getAnnotation(BuilderIgnored.class) == null)
                .collect(Collectors.toList());
    }

    private BuilderFieldAccessMethod fieldAccessMethod(TypeElement annotatedClass) {
        StagedBuilder annotation = annotatedClass.getAnnotation(StagedBuilder.class);

        assert annotation != null;

        return annotation.fieldAccessMethod();
    }

    private String validatorMethod(TypeElement annotatedClass) {
        StagedBuilder annotation = annotatedClass.getAnnotation(StagedBuilder.class);

        assert annotation != null;

        return annotation.validator();
    }

    private static String capitalize(String str) {
        if (!str.isEmpty()) {
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str;
    }

    private static boolean isOptional(VariableElement field) {
        if (field.getAnnotation(BuilderOptional.class) != null) {
            return true;
        }
        for (AnnotationMirror annotation : field.getAnnotationMirrors()) {
            String annotationName = annotation.getAnnotationType().asElement().getSimpleName().toString();
            if ("Nullable".equals(annotationName)) {
                return true;
            }
        }

        return false;
    }

    private <T> T last(T[] arr) {
        return arr[arr.length - 1];
    }
}
