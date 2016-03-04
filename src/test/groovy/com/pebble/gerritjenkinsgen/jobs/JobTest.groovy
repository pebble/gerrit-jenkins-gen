package com.pebble.gerritjenkinsgen.jobs

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

class JobTest {
    static final String PROJECT_NAME = 'test-project'
    static final String JOB_NAME = 'test-project-verify'
    static final String GIT_URL = 'ssh://user:password@host/path.git'

    Job job

    @Before
    void setupJob() {
        job = new TestImpl(
                projectName: PROJECT_NAME,
                jobName: JOB_NAME,
                gitUrl: GIT_URL
        )
    }

    @Test
    void dslSimple() {
        def dsl = job.toDsl().replaceAll('\n', '')
        assertTrue(dsl.contains("job('${JOB_NAME}')"))
    }

    @Test
    void dslMatrix() {
        job.testAxes = ['text("BE", ["to be", "not to be"])']
        def dsl = job.toDsl().replaceAll('\n', '')

        assertTrue(dsl.contains("matrixJob('${JOB_NAME}')"))
    }

    @Test
    void configureBlock() {
        job.testConfigure = [
                'project'
        ]

        def dsl = job.toDsl().replaceAll('\n', '')

        assertTrue(dsl.contains("configure { project ->"))
    }

    static class TestImpl extends Job {
        List<String> testAxes
        List<String> testConfigure

        @Override
        protected List<String> getAxes() {
            testAxes != null ? testAxes : super.axes
        }

        @Override
        protected List<String> getConfigure() {
            testConfigure != null ? testConfigure : super.configure
        }
    }
}
