package com.pebble.gerritjenkinsgen.jobs.verify

/**
 * Verify a simple Docker project.
 */
class DockerVerifyJob extends VerifyJob {
    boolean publish

    @Override
    protected List<String> getSteps() {
        def imageName = "pebble/${projectName}:jenkins-\\\${BUILD_NUMBER}"
        def steps = ["shell('docker build -t ${imageName} .')"]

        if (publish) {
            steps += "shell('docker push ${imageName}')"
        }

        steps
    }
}
