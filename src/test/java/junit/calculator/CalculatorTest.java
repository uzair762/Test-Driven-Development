package junit.calculator;

import org.example.junit.Calculator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalculatorTest {

    @Test
    public void shouldAddTwoNumbers() {
        Calculator calculator = new Calculator();

        int result = calculator.add(2, 3);

        assertEquals(5, result);
    }

    @Test
    public void shouldMultiplyTwoNumbers() {
        Calculator calculator = new Calculator();

        int result = calculator.multiply(4, 5);

        assertEquals(20, result);
    }
}
