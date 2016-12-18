package com.tngtech.java.junit.dataprovider.internal.jupiter;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

class DataProviderTestParameterResolver implements ParameterResolver {

    private final Object[] arguments;

    DataProviderTestParameterResolver(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Executable declaringExecutable = parameterContext.getParameter().getDeclaringExecutable();
        Method testMethod = extensionContext.getTestMethod().orElse(null);
        return declaringExecutable.equals(testMethod) && parameterContext.getIndex() < arguments.length;
    }

    // TODO junit-dataprovider should also distinguish between creation and resolving but for JUnit4 it is still necessary like that!
    @Override
    public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return arguments[parameterContext.getIndex()];
    }
}
