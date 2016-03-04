package com.pebble.gerritjenkinsgen.jobs.release

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

import static com.pebble.gerritjenkinsgen.jobs.JobTest.*

class ReleaseJobTest {
    ReleaseJob releaseJob

    @Before
    void setUp() {
        releaseJob = new ReleaseJob(
                projectName: PROJECT_NAME,
                jobName: JOB_NAME,
                gitUrl: GIT_URL)
    }

    @Test
    void scm() {
        assertNotNull(releaseJob.scm.find { it =~ 'skipTag' })
    }

    @Test
    void triggers() {
        assertNotNull(releaseJob.triggers.find { it =~ 'refUpdated()' })
    }

    @Test
    void configure() {
        def configure = String.join('', releaseJob.configure)
        assertTrue(configure.indexOf('BuildNameUpdater') >= 0)
    }

    @Test
    void steps() {
        def steps = String.join('', releaseJob.steps)
        assertTrue(steps.indexOf('version.txt') >= 0)
    }

    @Test
    void quietPeriod() {
        assertEquals(0, releaseJob.quietPeriod)
    }

    @Test
    void logRotationBuilds() {
        assertEquals(50, releaseJob.logRotationBuilds)
    }
}
