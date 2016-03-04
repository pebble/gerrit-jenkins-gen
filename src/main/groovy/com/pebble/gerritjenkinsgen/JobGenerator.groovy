package com.pebble.gerritjenkinsgen

import com.pebble.gerritjenkinsgen.jobs.Job
import com.pebble.gerritjenkinsgen.jobs.deploy.DeployJob
import com.pebble.gerritjenkinsgen.jobs.release.DockerReleaseJob
import com.pebble.gerritjenkinsgen.jobs.verify.DockerVerifyJob
import com.pebble.gerritjenkinsgen.jobs.verify.NodeVerifyJob
import groovy.json.JsonException
import groovy.json.JsonSlurper

import java.nio.file.Files

/**
 * Generates jobs for a project.
 */
class JobGenerator {
    String projectName
    String gerritHost
    Integer gerritPort = 29418
    String gerritRefSpec
    String tmpDir

    JobGenerator() {
        this.tmpDir = Files.createTempDirectory("jenkins-seed")
    }

    void generateJobs() {
        List<Job> jobs = this.getJobs()

        if (!jobs.isEmpty()) {
            println "folder('${projectName}') {"
            println "  displayName('${projectName}')"
            println "}"

            jobs.each { job ->
                println job.toDsl()
            }
        }
    }

    List<Job> getJobs() {
        List<Job> jobs = []

        // Check for jenkins.json file
        String jenkinsJson = this.getFileContents('jenkins.json')

        if (jenkinsJson) {
            Map<String, Object> jenkinsParams = parseJenkinsJson(jenkinsJson)
            jobs.addAll(getJobsFromParams(jenkinsParams))

            println getViewsFromParams(jenkinsParams)
        } else {
            // No jenkins.json, is there a Dockerfile?
            String dockerfile = getFileContents('Dockerfile')
            if (dockerfile) {
                // Just a dockerfile, do a dumb build to verify:
                jobs += new DockerVerifyJob(
                        jobName: "${projectName}/verify-docker",
                        projectName: projectName,
                        gitUrl: this.gitUrl
                )
            }
        }
        return jobs
    }


    protected List<Job> getJobsFromParams(Map<String, Object> jenkinsParams) {
        List<Job> jobs = []

        def nodeParams = jenkinsParams.get('node')
        if (nodeParams) {
            def nodeVersions = nodeParams.findAll { it =~ '^[0-9]+(.[0-9]+)?$' }
            jobs += new NodeVerifyJob(
                    jobName: "${projectName}/verify-node",
                    projectName: projectName,
                    gitUrl: this.gitUrl,
                    nodeVersions: nodeVersions)
        }

        def releaseJob
        Map<String, Object> dockerParams = jenkinsParams.get('docker')
        if (dockerParams != null) {
            // Is verify explicitly disabled?
            def dockerVerify = dockerParams.get('verify')
            if ((dockerVerify as String) != 'false') {
                jobs += new DockerVerifyJob(
                        jobName: "${projectName}/verify-docker",
                        projectName: projectName,
                        gitUrl: this.gitUrl)
            }

            // Is release disabled?
            def dockerRelease = dockerParams.get('release')
            if ((dockerRelease as String) != 'false') {
                releaseJob = new DockerReleaseJob(
                        jobName: "${projectName}/build-docker",
                        projectName: projectName,
                        gitUrl: this.gitUrl)

                jobs += releaseJob
            }
        }

        Map<String, Object> flotillaParams = jenkinsParams.get('flotilla')
        if (flotillaParams != null) {
            // Is there a deployment pipeline defined?
            List<String> flotillaPipeline = flotillaParams.get('pipeline')
            if (flotillaPipeline) {
                def lastJob = releaseJob
                flotillaPipeline.each { pipelineStage ->
                    def jobName = "${projectName}/deploy-${pipelineStage}"

                    if (pipelineStage =~ '!.*') {
                        pipelineStage = pipelineStage.substring(1)
                        jobName = "${projectName}/deploy-${pipelineStage}"
                        lastJob.pipelineNextAuto = jobName
                    } else {
                        lastJob.pipelineNext = jobName
                    }

                    def deployJob = new DeployJob(
                            jobName: jobName,
                            projectName: projectName,
                            gitUrl: this.gitUrl,
                            environment: pipelineStage)
                    jobs += deployJob
                    lastJob = deployJob
                }
            }
        }


        return jobs
    }

    protected String getViewsFromParams(Map<String, Object> jenkinsParams) {
        Map<String, Object> flotillaParams = jenkinsParams.get('flotilla')
        if (!flotillaParams) {
            return ""
        }

        List<String> flotillaPipeline = flotillaParams.get('pipeline')
        if (!flotillaPipeline) {
            return ""
        }

        """buildPipelineView('${projectName}/pipeline') {
  filterBuildQueue()
  filterExecutors()
  displayedBuilds(5)
  title('${projectName}')
  selectedJob('${projectName}/build-docker')
}"""
    }

    private Map<String, Object> parseJenkinsJson(String jenkinsJson) {
        Map<String, Object> jenkinsParams = [:]
        try {
            def jsonSlurper = new JsonSlurper()
            jenkinsParams = jsonSlurper.parseText(jenkinsJson)
        } catch (JsonException e) {
        }
        jenkinsParams
    }

    String getGitUrl() {
        "ssh://admin@${gerritHost}:${gerritPort}/${projectName}.git"
    }

    protected String getFileContents(String filename) {
        String gitRemote = "ssh://admin@${gerritHost}:${gerritPort}/${projectName}.git"
        String gitCmd = "git archive --remote=${gitRemote} ${gerritRefSpec} ${filename}"
        def cmd = ['/bin/sh', '-c', "${gitCmd} | tar -xC ${tmpDir}"] as String[]
        def process = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start()
        process.waitFor()

        File tmpFile = new File(tmpDir, filename)
        return tmpFile.exists() ? tmpFile.text : null
    }
}
