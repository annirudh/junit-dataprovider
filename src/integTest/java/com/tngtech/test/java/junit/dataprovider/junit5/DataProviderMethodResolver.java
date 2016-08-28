package com.tngtech.test.java.junit.dataprovider.junit5;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.internal.cglib.proxy.UndeclaredThrowableException;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.discovery.ElementResolver;
import org.junit.jupiter.engine.discovery.predicates.IsTestMethod;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.runners.model.FrameworkMethod;

import com.tngtech.java.junit.dataprovider.DataProviderFrameworkMethod;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;
import com.tngtech.java.junit.dataprovider.internal.TestGenerator;

public class DataProviderMethodResolver implements ElementResolver {

    // TODO
    private static final IsTestMethod isTestMethod = new IsTestMethod();
    // private static final MethodFinder methodFinder = new MethodFinder();

    static final String SEGMENT_TYPE = "dataprovider-method";

    private final String segmentType;

    DataProviderMethodResolver() {
        this(SEGMENT_TYPE);
    }

    DataProviderMethodResolver(String segmentType) {
        this.segmentType = segmentType;
    }

    @Override
    public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {
        if (!(element instanceof Method)) {
            return Collections.emptySet();
        }
        if (!(parent instanceof ClassTestDescriptor)) {
            return Collections.emptySet();
        }

        // TODO
        // Method testMethod = (Method) element;
        // if (!isTestMethod(testMethod)) {
        // return Collections.emptySet();
        // }

        Set<TestDescriptor> result = new HashSet<>();

        Method method = (Method) element;
        FrameworkMethod testMethod = new FrameworkMethod(method);

        if (testMethod.getAnnotation(UseDataProvider.class) == null) {
            return result;
        }

        FrameworkMethod dataProviderMethod;
        try {
            dataProviderMethod = new FrameworkMethod(testMethod.getDeclaringClass().getMethod("dataProviderAdd"));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new UndeclaredThrowableException(e);
        }

        TestGenerator testGenerator = new TestGenerator(new DataConverter());
        List<FrameworkMethod> testMethods = testGenerator.generateExplodedTestMethodsFor(testMethod, dataProviderMethod);

        for (FrameworkMethod frameworkMethod : testMethods) {
            // UniqueId uniqueId = createUniqueId(testMethod, parent);
            UniqueId uniqueId = createUniqueId(frameworkMethod.getName(), parent);
            // return Collections.singleton(resolveMethod(testMethod, (ClassTestDescriptor) parent, uniqueId));
            // result.add(new MethodTestDescriptor(uniqueId, (ClassTestDescriptor) parent, testMethod.getMethod()));
            // parametersMap.put(uniqueId.toString(), ((DataProviderFrameworkMethod) frameworkMethod).getParameters());
            result.add(resolveMethod(frameworkMethod.getMethod(), (ClassTestDescriptor) parent, uniqueId,
                    ((DataProviderFrameworkMethod) frameworkMethod).getParameters()));
        }
        return result;
    }

    @Override
    public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {
        // TODO check this
        if (!segment.getType().equals(this.segmentType)) {
            return Optional.empty();
        }

        if (!(parent instanceof ClassTestDescriptor)) {
            return Optional.empty();
        }

        // Optional<Method> optionalMethod = findMethod(segment, (ClassTestDescriptor) parent);
        // if (!optionalMethod.isPresent()) {
        // return Optional.empty();
        // }

        // Method testMethod = optionalMethod.get();
        // if (!isTestMethod(testMethod)) {
        // return Optional.empty();
        // }

        // UniqueId uniqueId = createUniqueId(testMethod, parent);
        // return Optional.of(resolveMethod(testMethod, (ClassTestDescriptor) parent, uniqueId));
        return Optional.empty();
    }

    // TODO copied from here
    protected boolean isTestMethod(Method candidate) {
        return isTestMethod.test(candidate);
    }

    private UniqueId createUniqueId(String uniqueTestMethodName, TestDescriptor parent) {
        return parent.getUniqueId().append(this.segmentType, uniqueTestMethodName);
    }

    // private Optional<Method> findMethod(UniqueId.Segment segment, ClassTestDescriptor parent) {
    // return methodFinder.findMethod(segment.getValue(), parent.getTestClass());
    // }

    protected TestDescriptor resolveMethod(Method testMethod, ClassTestDescriptor parentClassDescriptor,
            UniqueId uniqueId, Object[] args) {
        return new DataProviderMethodTestDescriptor(uniqueId, parentClassDescriptor, testMethod, args);
    }

}
