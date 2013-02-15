junit-dataprovider
==================

A TestNG like dataprovider runner for JUnit

Here is an easy usage example:

```java
package com.tngtech.java.junit.dataprovider;

import static org.fest.assertions.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class DataProviderTest {

    @DataProvider
    public static Object[][] dataProviderAdd() {
        // @formatter:off
        return new Object[][] {
                { 0, 0, 0 },
                { 1, 1, 2 },
                /* ... */
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("dataProviderAdd")
    public void testAdd(int a, int b, int expected) {
        // Given:

        // When:
        int result = a + b;

        // Then:
        assertThat(result).isEqualTo(expected);
    }
}
```
