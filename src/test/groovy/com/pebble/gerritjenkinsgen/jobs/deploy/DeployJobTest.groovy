package com.pebble.gerritjenkinsgen.jobs.deploy

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

import static com.pebble.gerritjenkinsgen.jobs.JobTest.*

class DeployJobTest {
    DeployJob deployJob

    @Before
    void setUp() {
        deployJob = new DeployJob(
                projectName: PROJECT_NAME,
                jobName: JOB_NAME,
                gitUrl: GIT_URL)
    }

    @Test
    void triggers() {
        assertEquals([], deployJob.triggers)
    }

    @Test
    void steps() {
        def steps = String.join('', deployJob.steps)
        assertTrue(steps.indexOf('jenkins.json') >= 0)
        assertTrue(steps.indexOf('flotilla') >= 0)
    }
}
