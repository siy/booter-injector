package io.booter.injector.core.beans;

public class ClassWith4ParametersConstructor {
    private final Long p0;
    private final String p1;
    private final int p2;
    private final Long p3;

    public ClassWith4ParametersConstructor(Long p0, String p1, int p2, Long p3) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
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
}
