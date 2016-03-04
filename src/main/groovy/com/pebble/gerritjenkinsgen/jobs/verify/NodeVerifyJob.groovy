package com.pebble.gerritjenkinsgen.jobs.verify

/**
 * Verify a node project.
 *
 * It's assumed that Docker is used and 'npm test' works in the container and
 * produces xunit output to /apps/build/tests.xml .
 */
class NodeVerifyJob extends VerifyJob {
    def nodeVersions = ['4.2', '4.3']

    @Override
    protected List<String> getAxes() {
        def nodeFlags = '["' + String.join('","', nodeVersions) + '"]'
        return [
                "text('NODE', ${nodeFlags})"
        ]
    }

    @Override
    protected List<String> getSteps() {
        def tempDockerfile = 'Dockerfile.nodeTemp'
        def imageName = "pebble/${projectName}:jenkins-\\\${BUILD_NUMBER}"
        def containerName = projectName.replaceAll('-', '') + '_jenkins_\\${NODE}_\\${BUILD_NUMBER}'
        return [
                // Swap the node version in the dockerfile
                "shell('''",
                "cat Dockerfile | sed \\\"s/^FROM node:.*/FROM node:\\\${NODE}/g\\\" > ${tempDockerfile}",
                "docker build -f ${tempDockerfile} -t ${imageName} .",
                "''')",
                "shell('''",
                "mkdir -p build/",
                "docker run --name ${containerName} ${imageName} npm test",
                "docker cp ${containerName}:/app/build/tests.xml build/tests.xml",
                "docker rm ${containerName}",
                "''')",
                "shell('docker run --rm ${imageName} npm run lint')"
        ]
    }

    @Override
    protected List<String> getPublishers() {
        [
                "archiveJunit('build/tests.xml')"
        ]
    }
}
