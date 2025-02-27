package dev.config;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Nelson Tanko
 */
public class PrefixedSimpleKey implements Serializable {

    private final String prefix;
    private final transient Object[] params;
    private final String methodName;
    private int hashCodeValue;

    /**
     * <p>Constructor for PrefixedSimpleKey.</p>
     *
     * @param prefix     a {@link String} object.
     * @param methodName a {@link String} object.
     * @param elements   a {@link Object} object.
     */
    public PrefixedSimpleKey(String prefix, String methodName, Object... elements) {
        Assert.notNull(prefix, "Prefix must not be null");
        Assert.notNull(elements, "Elements must not be null");
        this.prefix = prefix;
        this.methodName = methodName;
        params = new Object[elements.length];
        System.arraycopy(elements, 0, params, 0, elements.length);

        hashCodeValue = prefix.hashCode();
        hashCodeValue = 31 * hashCodeValue + methodName.hashCode();
        hashCodeValue = 31 * hashCodeValue + Arrays.deepHashCode(params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        return (
                this == other ||
                        (other instanceof PrefixedSimpleKey &&
                                prefix.equals(((PrefixedSimpleKey) other).prefix) &&
                                methodName.equals(((PrefixedSimpleKey) other).methodName) &&
                                Arrays.deepEquals(params, ((PrefixedSimpleKey) other).params))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return hashCodeValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return prefix + " " + getClass().getSimpleName() + methodName + " [" + StringUtils.arrayToCommaDelimitedString(params) + "]";
    }
}

