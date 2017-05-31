package io.booter.injector.core.beans;

public class ClassWith3ParametersConstructor {
    private final Long p0;
    private final String p1;
    private final int p2;

    public ClassWith3ParametersConstructor(Long p0, String p1, int p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    public Long p0() {
        return p0;
    }

    public String p1() {
        return p1;
    }

    public int p2() {
        return p2;
    }
}
