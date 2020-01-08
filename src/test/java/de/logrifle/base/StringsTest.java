package de.logrifle.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringsTest {

    @ParameterizedTest
    @CsvSource({
            "foobar____,foobar,10,_,false",
            "____foobar,foobar,10,_,true",
            "!!foobar,foobar,8,!,true",
            "foobar,foobar,4,!,true",

    })
    void pad(String expectedResult, String inputString, String desiredLength, String paddingContent, String beginning) {
        String padded = Strings.pad(inputString, Integer.parseInt(desiredLength), paddingContent, Boolean.parseBoolean(beginning));
        Assertions.assertEquals(expectedResult, padded);
    }
}