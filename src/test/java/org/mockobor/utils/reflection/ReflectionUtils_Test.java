package org.mockobor.utils.reflection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;


class ReflectionUtils_Test {

    @ParameterizedTest
    @CsvSource( {
            "1.0, 0", "1.1, 1", "1.5, 5", "1.8, 8",
            "9, 9", "11, 11", "16, 16", "17, 17",
            "invalid-version, 8", "1.7.1_192, 7"
    } )
    void parseJavaSpecificationVersion( String specification, int expectedVersion ) {
        assertThat( ReflectionUtils.parseJavaSpecificationVersion( specification ) ).isEqualTo( expectedVersion );
    }
}
