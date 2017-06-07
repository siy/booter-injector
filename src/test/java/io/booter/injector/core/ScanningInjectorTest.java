package io.booter.injector.core;

import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

public class ScanningInjectorTest {
    @Test
    public void treeIsBuilt() throws Exception {
        //Map<Key, ScanningInjector.Node> map = ScanningInjector.buildTree(Key.of(FastInjectorTest.Foo.class));
        ScanningInjector injector = new ScanningInjector();

        Supplier<Object> obj = injector.supplier(Key.of(FastInjectorTest.Foo.class));
        assertThat(obj).isNotNull();
        assertThat(obj.get()).isNotNull();
    }
    
//    @Test(expected = InjectorException.class)
//    public void shouldDetectCycle() throws Exception {
//        ScanningInjector.buildTree(Key.of(FastInjectorTest.ClassWithCyclicDependencies.class));
//    }
}