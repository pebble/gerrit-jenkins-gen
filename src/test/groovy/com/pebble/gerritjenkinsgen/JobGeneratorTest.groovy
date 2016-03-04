package com.pebble.gerritjenkinsgen

import com.pebble.gerritjenkinsgen.jobs.deploy.DeployJob
import com.pebble.gerritjenkinsgen.jobs.release.DockerReleaseJob
import com.pebble.gerritjenkinsgen.jobs.verify.DockerVerifyJob
import com.pebble.gerritjenkinsgen.jobs.verify.NodeVerifyJob
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*;
import static com.pebble.gerritjenkinsgen.jobs.JobTest.*

class JobGeneratorTest {
    JobGenerator jobGenerator

    @Before
    public void setUp() {
        jobGenerator = new TestJobGenerator(
                projectName: PROJECT_NAME,
                gerritHost: 'gerrit',
                gerritPort: 29418
        )
    }

    @Test
    void getJobsFromParams_Node() {
        def jobs = jobGenerator.getJobsFromParams([
                'node': ['4.1', '4.4']
        ])
        assertEquals(1, jobs.size())

        NodeVerifyJob nodeVerify = jobs.find { it instanceof NodeVerifyJob }
        assertNotNull(nodeVerify)
        assertEquals(['4.1', '4.4'], nodeVerify.nodeVersions)
    }

    @Test
    void getJobsFromParams_Docker() {
        def jobs = jobGenerator.getJobsFromParams([
                'docker': [:]
        ])

        assertEquals(2, jobs.size())
        DockerVerifyJob dockerVerify = jobs.find {
            it instanceof DockerVerifyJob
        }
        assertNotNull(dockerVerify)

        DockerReleaseJob dockerRelease = jobs.find {
            it instanceof DockerReleaseJob
        }
        assertNotNull(dockerRelease)
        assertNull(dockerRelease.pipelineNext)
        assertNull(dockerRelease.pipelineNextAuto)
    }

    @Test
    void getJobsFromParams_FlotillaPipeline() {
        def jobs = jobGenerator.getJobsFromParams([
                'docker'  : [:],
                'flotilla': [
                        'pipeline': ['develop']
                ]
        ])

        assertEquals(3, jobs.size())
        DockerReleaseJob dockerRelease = jobs.find {
            it instanceof DockerReleaseJob
        }
        assertTrue(dockerRelease.pipelineNext.indexOf('develop') >= 0)
        assertNull(dockerRelease.pipelineNextAuto)
    }

    @Test
    void getJobsFromParams_FlotillaPipelineAuto() {
        def jobs = jobGenerator.getJobsFromParams([
                'docker'  : [:],
                'flotilla': [
                        'pipeline': ['!develop']
                ]
        ])

        assertEquals(3, jobs.size())
        DockerReleaseJob dockerRelease = jobs.find {
            it instanceof DockerReleaseJob
        }
        assertNull(dockerRelease.pipelineNext)
        assertTrue(dockerRelease.pipelineNextAuto.indexOf('develop') >= 0)
    }

    @Test
    void getJobsFromParams_FlotillaPipelineDeep() {
        def jobs = jobGenerator.getJobsFromParams([
                'docker'  : [:],
                'flotilla': [
                        'pipeline': ['!develop', '!staging', 'production']
                ]
        ])
        assertEquals(5, jobs.size())

        DeployJob developDeploy = jobs.find {
            it instanceof DeployJob && it.jobName =~ '.*develop.*'
        }
        DeployJob stagingDeploy = jobs.find {
            it instanceof DeployJob && it.jobName =~ '.*staging.*'
        }
        DeployJob productionDeploy = jobs.find {
            it instanceof DeployJob && it.jobName =~ '.*production.*'
        }

        assertNull(developDeploy.pipelineNext)
        assertEquals(stagingDeploy.jobName, developDeploy.pipelineNextAuto)

        assertEquals(productionDeploy.jobName, stagingDeploy.pipelineNext)
        assertNull(stagingDeploy.pipelineNextAuto)

        assertNull(productionDeploy.pipelineNext)
        assertNull(productionDeploy.pipelineNextAuto)
    }

    @Test
    void getViewsFromParams() {
        def views = jobGenerator.getViewsFromParams([:])
        assertTrue(views.indexOf('buildPipelineView') == -1)
    }

    @Test
    void getViewsFromParams_FlotillaPipeline() {
        def views = jobGenerator.getViewsFromParams([
                'flotilla': [
                        'pipeline': ['!develop', '!staging', 'production']
                ]
        ])
        assertTrue(views.indexOf('buildPipelineView') >= 0)
    }

    @Test
    void gitUrl() {
        assertEquals('ssh://admin@gerrit:29418/test-project.git', jobGenerator.gitUrl)
    }

    @Test
    void getJobs() {
        jobGenerator.files['jenkins.json'] = '{"docker":{}}'
        assertEquals(2, jobGenerator.jobs.size())
    }

    @Test
    void getJobs_DockerOnly() {
        jobGenerator.files['Dockerfile'] = 'FROM debian:jessie'
        assertEquals(1, jobGenerator.jobs.size())
    }

    @Test
    public void generateJobs() {
        jobGenerator.files['Dockerfile'] = 'FROM debian:jessie'
        jobGenerator.generateJobs()
    }

    static class TestJobGenerator extends JobGenerator {
        Map<String, String> files = [:]

        @Override
        protected String getFileContents(String filename) {
            files[filename]
        }
    }
}
