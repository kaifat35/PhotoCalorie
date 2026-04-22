package com.stafeewa.photocalorie.app

import com.stafeewa.photocalorie.app.presentation.workers.TrainingScheduleConfig
import junit.framework.TestCase.assertEquals
import org.junit.Test


class TrainingScheduleConfigTest {

    @Test
    fun `normalizeFrequencyHoursClampsOutOfRangeValues`() {
        assertEquals(24, TrainingScheduleConfig.normalizeFrequencyHours(1))
        assertEquals(24, TrainingScheduleConfig.normalizeFrequencyHours(24))
        assertEquals(168, TrainingScheduleConfig.normalizeFrequencyHours(200))
    }

    @Test
    fun `normalizeMinExamplesClampsOutOfRangeValues`() {
        assertEquals(1, TrainingScheduleConfig.normalizeMinExamples(0))
        assertEquals(8, TrainingScheduleConfig.normalizeMinExamples(8))
        assertEquals(100, TrainingScheduleConfig.normalizeMinExamples(150))
    }
}