package com.pebble.gerritjenkinsgen.jobs.verify

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

import static com.pebble.gerritjenkinsgen.jobs.JobTest.*

class NodeVerifyJobTest {
    static final NODE_VERSIONS = ['4.3', '4.4']

    NodeVerifyJob nodeVerifyJob

    @Before
    void setUp() {
        nodeVerifyJob = new NodeVerifyJob(
                projectName: PROJECT_NAME,
                jobName: JOB_NAME,
                gitUrl: GIT_URL,
                nodeVersions: NODE_VERSIONS)
    }

    @Test
    void axes() {
        def axes = nodeVerifyJob.axes
        def nodeAxis = axes.find { it.indexOf('NODE') >= 0 }
        NODE_VERSIONS.each { assertTrue(nodeAxis.indexOf(it) >= 0) }
    }

    @Test
    void steps() {
        def steps = String.join('', nodeVerifyJob.steps)
        assertTrue(steps.indexOf('Dockerfile.nodeTemp') >= 0)
    }

    @Test
    void publishers() {
        def publishers = nodeVerifyJob.publishers
        def junitPublisher = publishers.find { it.indexOf('Junit') >= 0 }
        assertNotNull(junitPublisher)
    }
}
