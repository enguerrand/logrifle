package de.rochefort.logrifle.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DigitsTest {
    @ParameterizedTest
    @CsvSource({
            "0,1",
            "7,1",
            "12,2",
            "422,3",
            "2346,4",
            "98573,5",
            "293875,6",
    })
    void getDigitCount(String n, String expectedResult) {
        Assertions.assertEquals(Integer.parseInt(expectedResult), Digits.getDigitCount(Integer.parseInt(n)));
    }

}