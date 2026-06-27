package com.example.sololeveling90days.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLogicTest {

    @Test
    fun testXpForLevel() {
        assertEquals(500, xpForLevel(1))
        assertEquals(1000, xpForLevel(2))
        assertEquals(1500, xpForLevel(3))
    }

    @Test
    fun testLevelFromXp() {
        // Level 1: 0 - 499 XP
        assertEquals(1, levelFromXp(0))
        assertEquals(1, levelFromXp(250))
        assertEquals(1, levelFromXp(499))

        // Level 2: 500 - 1499 XP (threshold = 500 + 1000 = 1500)
        assertEquals(2, levelFromXp(500))
        assertEquals(2, levelFromXp(1000))
        assertEquals(2, levelFromXp(1499))

        // Level 3: 1500 - 2999 XP (threshold = 1500 + 1500 = 3000)
        assertEquals(3, levelFromXp(1500))
        assertEquals(3, levelFromXp(2000))
        assertEquals(3, levelFromXp(2999))

        // Level 4: 3000+ XP
        assertEquals(4, levelFromXp(3000))
    }

    @Test
    fun testXpProgressInLevel() {
        // Level 1 (0 to 500)
        assertEquals(0f, xpProgressInLevel(0), 0.001f)
        assertEquals(0.5f, xpProgressInLevel(250), 0.001f)
        assertEquals(0.998f, xpProgressInLevel(499), 0.001f)

        // Level 2 (500 to 1500)
        assertEquals(0f, xpProgressInLevel(500), 0.001f)
        assertEquals(0.5f, xpProgressInLevel(1000), 0.001f) // (1000 - 500) / 1000 = 0.5
        assertEquals(0.999f, xpProgressInLevel(1499), 0.001f)

        // Level 3 (1500 to 3000)
        assertEquals(0f, xpProgressInLevel(1500), 0.001f)
        assertEquals(0.333f, xpProgressInLevel(2000), 0.01f) // (2000 - 1500) / 1500 = 0.333
    }

    }
}
