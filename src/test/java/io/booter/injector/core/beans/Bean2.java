package io.booter.injector.core.beans;

public class Bean2 {
    private final Long p0;
    private final String p1;

    public Bean2(Long p0, String p1) {
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
