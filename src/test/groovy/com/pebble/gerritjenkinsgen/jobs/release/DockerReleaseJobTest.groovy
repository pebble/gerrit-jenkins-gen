package com.pebble.gerritjenkinsgen.jobs.release

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

import static com.pebble.gerritjenkinsgen.jobs.JobTest.*

class DockerReleaseJobTest {
    DockerReleaseJob releaseJob

    @Before
    void setUp() {
        releaseJob = new DockerReleaseJob(
                projectName: PROJECT_NAME,
                jobName: JOB_NAME,
                gitUrl: GIT_URL)
    }

    @Test
    void steps() {
        def steps = String.join('', releaseJob.steps)
        assertTrue(steps.indexOf('version.txt') >= 0)
    }
}
