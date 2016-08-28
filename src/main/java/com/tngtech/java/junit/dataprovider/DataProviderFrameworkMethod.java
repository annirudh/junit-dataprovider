package com.tngtech.java.junit.dataprovider;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.runners.model.FrameworkMethod;

import com.tngtech.java.junit.dataprovider.internal.placeholder.BasePlaceholder;

/**
 * A special framework method that allows the usage of parameters for the test method.
 */
public class DataProviderFrameworkMethod extends FrameworkMethod {

    /**
     * Index of exploded test method such that each get a unique name.
     * <p>
     * This field is package private (= visible) for testing.
     * </p>
     */
    final int idx;

    /**
     * Parameters to invoke the test method.
     * <p>
     * This field is package private (= visible) for testing.
     * </p>
     */
    private final Object[] parameters;

    /**
     * Format of test method name.
     * <p>
     * This field is package private (= visible) for testing.
     * </p>
     */
    final String nameFormat;

    /**
     * Create a {@link FrameworkMethod} extended with special attributes for using this test with a dataprovider.
     *
     * @param method test method for which the {@link FrameworkMethod} is created
     * @param idx the index (row) of the used dataprovider
     * @param parameters used for invoking this test method
     * @param nameFormat defines the format of the test method name according to {@code @}{@link DataProvider#format()}
     */
    public DataProviderFrameworkMethod(Method method, int idx, Object[] parameters, String nameFormat) {
        super(method);

        if (parameters == null) {
            throw new NullPointerException("parameter must not be null");
        }
        if (nameFormat == null) {
            throw new NullPointerException("nameFormat must not be null");
        }
        if (parameters.length == 0) {
            throw new IllegalArgumentException("parameter must not be empty");
        }

        this.idx = idx;
        this.parameters = Arrays.copyOf(parameters, parameters.length);
        this.nameFormat = nameFormat;
    }

    @Override
    public String getName() {
        String result = nameFormat;
        for (BasePlaceholder placeHolder : Placeholders.all()) {
            placeHolder.setContext(getMethod(), idx, Arrays.copyOf(getParameters(), getParameters().length));
            result = placeHolder.process(result);
        }
        return result;
    }

    @Override
    public Object invokeExplosively(Object target, Object... params) throws Throwable {
        return super.invokeExplosively(target, getParameters());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + idx;
        result = prime * result + ((nameFormat == null) ? 0 : nameFormat.hashCode());
        result = prime * result + Arrays.hashCode(getParameters());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataProviderFrameworkMethod other = (DataProviderFrameworkMethod) obj;
        if (idx != other.idx) {
            return false;
        }
        if (nameFormat == null) {
            if (other.nameFormat != null) {
                return false;
            }
        } else if (!nameFormat.equals(other.nameFormat)) {
            return false;
        }
        if (!Arrays.equals(getParameters(), other.getParameters())) {
            return false;
        }
        return true;
    }

    public Object[] getParameters() {
        return parameters;
    }
}
