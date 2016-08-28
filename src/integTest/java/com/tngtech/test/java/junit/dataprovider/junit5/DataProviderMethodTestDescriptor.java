package com.tngtech.test.java.junit.dataprovider.junit5;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.MethodTestDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.ThrowableCollector;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.UniqueId;

public class DataProviderMethodTestDescriptor extends MethodTestDescriptor {

    private final Object[] arguments; // TODO use list to be immutable ;-)

    public DataProviderMethodTestDescriptor(UniqueId uniqueId, ClassTestDescriptor parentClassDescriptor, Method testMethod,
            Object[] arguments) {
        super(uniqueId, parentClassDescriptor, testMethod);
        this.arguments = arguments;
    }

    @Override
    protected void invokeTestMethod(JupiterEngineExecutionContext context) {
        // super.invokeTestMethod(context);
        TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();
        ThrowableCollector throwableCollector = context.getThrowableCollector();

        throwableCollector.execute(() -> {
            try {
                Method method = testExtensionContext.getTestMethod().get();
                Object instance = testExtensionContext.getTestInstance();
                // executableInvoker.invoke(method, instance, testExtensionContext, context.getExtensionRegistry());

                // TODO just for now to test
                ReflectionUtils.invokeMethod(method, instance, arguments);
            } catch (Throwable throwable) {
                invokeTestExecutionExceptionHandlers(context.getExtensionRegistry(), testExtensionContext, throwable);
            }
        });

    }
}
