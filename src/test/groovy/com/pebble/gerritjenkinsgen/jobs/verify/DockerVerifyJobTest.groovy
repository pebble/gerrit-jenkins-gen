package com.pebble.gerritjenkinsgen.jobs.verify

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

import static com.pebble.gerritjenkinsgen.jobs.JobTest.*

class DockerVerifyJobTest {
    DockerVerifyJob dockerVerifyJob

    @Before
    void setupJob() {
        dockerVerifyJob = new DockerVerifyJob(
                projectName: PROJECT_NAME,
                jobName: JOB_NAME,
                gitUrl: GIT_URL)
    }

    @Test
    void steps() {
        def steps = dockerVerifyJob.steps
        assertEquals(1, steps.size())
        assertNotNull(steps.find { it =~ 'docker build' })
    }
}
