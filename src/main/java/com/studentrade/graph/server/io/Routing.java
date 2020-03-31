package com.studentrade.graph.server.io;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;

public abstract class Routing<T> {

    @Inject
    private Injector injector;

    private Class<T> controller;

    protected Routing() {
    }

    public abstract void bindRoutes();

    public T getResource() {
        return injector.getInstance(getResourceFromGenericType());
    }

    private Class<T> getResourceFromGenericType() {
        if (controller == null) {
            controller = (Class<T>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return controller;
    }
}
