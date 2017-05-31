package io.booter.injector.core.beans;

public class ClassWith2ParametersConstructor {
    private final Long p0;
    private final String p1;

    public ClassWith2ParametersConstructor(Long p0, String p1) {
        this.p0 = p0;
        this.p1 = p1;
    }

    public Long p0() {
        return p0;
    }

    public String p1() {
        return p1;
    }
}
