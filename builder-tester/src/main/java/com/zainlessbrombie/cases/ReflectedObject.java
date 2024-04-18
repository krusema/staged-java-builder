package com.zainlessbrombie.cases;


import com.zainlessbrombie.stagedbuilder.BuilderFieldAccessMethod;
import com.zainlessbrombie.stagedbuilder.BuilderOptional;
import com.zainlessbrombie.stagedbuilder.StagedBuilder;

@StagedBuilder(fieldAccessMethod = BuilderFieldAccessMethod.REFLECTION)
public class ReflectedObject {
    private String a;
    private int b;
    private String[] c;

    @BuilderOptional
    private String d;

    private final String e;
    private static String f;

    public ReflectedObject() {
        e = null;
    }

    public void setA(String a) {
        throw new RuntimeException("Should not be called");
    }
}
