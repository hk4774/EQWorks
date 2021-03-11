package EQMobileWorkSample

import kotlin.test.Test
import kotlin.test.assertTrue

class LibraryTest {
    @Test
    fun testSetup() {
        val classUnderTest = Library()
        assertTrue(classUnderTest.setup(), "setup should return 'true'")
    }

    @Test
    fun testLog() {
        val classUnderTest = Library()
        classUnderTest.log(LocationEvent(0F, 0F, System.currentTimeMillis() / 1000L, "empty"))
    }
}
