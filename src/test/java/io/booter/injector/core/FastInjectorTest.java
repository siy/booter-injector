package io.booter.injector.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FastInjectorTest {

    @Test
    public void shouldBuildSimpleBean() throws Exception {
        FastInjector injector = new FastInjector();
        Root root = injector.get(Root.class);

        assertThat(root).isInstanceOf(Root.class);
    }

    public static class Root {
        private final Leaf1 p1;
        private final Leaf2 p2;

        public Root(Leaf1 p1, Leaf2 p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        public Leaf1 p1() {
            return p1;
        }

        public Leaf2 p2() {
            return p2;
        }
    }

    public static class Leaf1 {
    }

    public static class Leaf2 {

    }
}