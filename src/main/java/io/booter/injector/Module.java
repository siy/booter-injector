package io.booter.injector;

import java.util.List;

public interface Module {
    List<Binding<?>> collectBindings();
}
