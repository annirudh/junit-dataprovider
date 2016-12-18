package com.tngtech.java.junit.dataprovider.internal.jupiter;

import static com.tngtech.java.junit.dataprovider.common.Preconditions.checkArgument;
import static com.tngtech.java.junit.dataprovider.common.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.Placeholders;
import com.tngtech.java.junit.dataprovider.internal.placeholder.BasePlaceholder;

class DataProviderTestInvocationContext implements TestTemplateInvocationContext {

    /**
     * Exploded test method.
     * <p>
     * This field is package private (= visible) for testing.
     * </p>
     */
    final Method method;

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
    public DataProviderTestInvocationContext(Method method, int idx, Object[] parameters, String nameFormat) {
        checkNotNull(method, "method must not be null");
        checkNotNull(parameters, "parameter must not be null");
        checkNotNull(nameFormat, "nameFormat must not be null");
        checkArgument(parameters.length != 0, "parameter must not be empty");

        this.method = method;
        this.idx = idx;
        this.parameters = Arrays.copyOf(parameters, parameters.length);
        this.nameFormat = nameFormat;
    }

    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public String getDisplayName(int invocationIndex) {
        String result = nameFormat;
        for (BasePlaceholder placeHolder : Placeholders.all()) {
            placeHolder.setContext(method, idx, Arrays.copyOf(getParameters(), getParameters().length));
            result = placeHolder.process(result);
        }
        return result;
        // TODO
        // return formatter.format(invocationIndex, arguments);
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
//        return super.invokeExplosively(target, getParameters());

        return singletonList(new DataProviderTestParameterResolver(getParameters()));
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
        DataProviderTestInvocationContext other = (DataProviderTestInvocationContext) obj;
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
}
