package com.pebble.gerritjenkinsgen.jobs.verify

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

import static com.pebble.gerritjenkinsgen.jobs.JobTest.*

class VerifyJobTest {
    VerifyJob verifyJob

    @Before
    void setupJob() {
        verifyJob = new VerifyJob(
                projectName: PROJECT_NAME,
                jobName: JOB_NAME,
                gitUrl: GIT_URL)
    }

    @Test
    void scm() {
        assertNotNull(verifyJob.scm.find { it =~ 'skipTag' })
    }

    @Test
    void triggers() {
        assertNotNull(verifyJob.triggers.find { it =~ 'patchsetCreated()' })
    }

    @Test
    void quietPeriod() {
        assertEquals(0, verifyJob.quietPeriod)
    }

    @Test
    void logRotationDays() {
        assertEquals(14, verifyJob.logRotationDays)
    }
}
