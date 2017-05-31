package io.booter.injector.core.beans;

public class ClassWith5ParametersConstructor {
    private final Long p0;
    private final String p1;
    private final int p2;
    private final Long p3;
    private final String p4;

    public ClassWith5ParametersConstructor(Long p0, String p1, int p2, Long p3, String p4) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
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

    public Long p3() {
        return p3;
    }

    public String p4() {
        return p4;
    }
}
