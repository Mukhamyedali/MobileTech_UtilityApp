package au.edu.jcu.assessment.utilityapp

import org.junit.Assert.*
import org.junit.Test

class ConversionTest {
    @Test fun convertsValidAmounts() {
        assertEquals(32500.0, convertAmount("100", 325.0)!!, 0.001)
        assertEquals(0.0, convertAmount("0", 325.0)!!, 0.001)
        assertEquals(6.5, convertAmount("10.00", 0.65)!!, 0.001)
    }
    @Test fun rejectsInvalidInputsAndMissingRates() {
        listOf("", "abc", "-1", "NaN", "Infinity", "1e309").forEach {
            assertNull(convertAmount(it, 1.0))
        }
        assertNull(convertAmount("100", null))
        assertNull(convertAmount("100", -1.0))
        assertNull(convertAmount("100", Double.NaN))
        assertNull(convertAmount("1e308", 100.0))
    }
}
