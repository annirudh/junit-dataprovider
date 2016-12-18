package com.tngtech.java.junit.dataprovider.internal.jupiter;

import com.tngtech.java.junit.dataprovider.DataProviderFrameworkMethod;
import com.tngtech.java.junit.dataprovider.DataProviderMethodResolver;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;
import com.tngtech.java.junit.dataprovider.internal.TestGenerator;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

public class DataProviderTestExtension implements TestTemplateInvocationContextProvider {

    private static final Logger logger = Logger.getLogger(DataProviderTestExtension.class.getName());

    // TODO
    private DataConverter dataConverter = new DataConverter();
    private TestGenerator testGenerator = new TestGenerator(dataConverter);

    @Override
    public boolean supports(ContainerExtensionContext context) {
        return isAnnotated(context.getTestMethod(), UseDataProvider.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {

        List<TestTemplateInvocationContext> result = new ArrayList<>();

        Method templateMethod = Preconditions.notNull(context.getTestMethod().orElse(null), "test method must not be null");
        FrameworkMethod testMethod = new FrameworkMethod(templateMethod);

        for (FrameworkMethod dataProviderMethod : getDataProviderMethods(testMethod)) {
            List<FrameworkMethod> frameworkMethods = testGenerator.generateExplodedTestMethodsFor(testMethod, dataProviderMethod);
            for (FrameworkMethod frameworkMethod : frameworkMethods) {
                if (frameworkMethod instanceof DataProviderFrameworkMethod) {
                    DataProviderFrameworkMethod fm = (DataProviderFrameworkMethod) frameworkMethod;
                    result.add(new DataProviderTestInvocationContext(fm.getMethod(), fm.getIdx(), fm.getParameters(), fm.getNameFormat()));
                }

            }
        }
        return result.stream();
    }

//    protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ContainerExtensionContext context) {
//        try {
//            return provider.provideArguments(context);
//        } catch (Exception e) {
//            throw ExceptionUtils.throwAsUncheckedException(e);
//        }
//    }

    /**
     * <p>
     * This method is package private (= visible) for testing.
     * </p>
     */
    List<FrameworkMethod> getDataProviderMethods(FrameworkMethod testMethod) {
        List<FrameworkMethod> result = new ArrayList<FrameworkMethod>();

        UseDataProvider useDataProvider = testMethod.getAnnotation(UseDataProvider.class);
        if (useDataProvider == null) {
            result.add(null);
        } else {
            for (Class<? extends DataProviderMethodResolver> resolverClass : useDataProvider.resolver()) {
                DataProviderMethodResolver resolver = getResolverInstanceInt(resolverClass);

                List<FrameworkMethod> dataProviderMethods = resolver.resolve(testMethod, useDataProvider);
                if (UseDataProvider.ResolveStrategy.UNTIL_FIRST_MATCH.equals(useDataProvider.resolveStrategy()) && !dataProviderMethods.isEmpty()) {
                    result.addAll(dataProviderMethods);
                    break;

                } else if (UseDataProvider.ResolveStrategy.AGGREGATE_ALL_MATCHES.equals(useDataProvider.resolveStrategy())) {
                    result.addAll(dataProviderMethods);
                }
            }
        }
        return result;
    }

    /**
     * Returns a new instance of {@link DataProviderMethodResolver}. This method is required for testing. It calls
     * {@link Class#newInstance()} which needs to be stubbed while testing.
     * <p>
     * This method is package private (= visible) for testing.
     * </p>
     */
    DataProviderMethodResolver getResolverInstanceInt(Class<? extends DataProviderMethodResolver> resolverClass) {
        Constructor<? extends DataProviderMethodResolver> constructor;
        try {
            constructor = resolverClass.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find default constructor to instantiate resolver " + resolverClass, e);
        } catch (SecurityException e) {
            throw new IllegalStateException(
                    "Security violation while trying to access default constructor to instantiate resolver " + resolverClass, e);
        }

        try {
            return constructor.newInstance();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access default constructor to instantiate resolver " + resolverClass, e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not instantiate resolver " + resolverClass + " using default constructor", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("The default constructor of " + resolverClass + " has thrown an exception", e);
        }
    }
}
