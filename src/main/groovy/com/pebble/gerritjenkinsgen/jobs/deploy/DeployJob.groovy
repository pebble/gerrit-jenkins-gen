package com.pebble.gerritjenkinsgen.jobs.deploy

import com.pebble.gerritjenkinsgen.jobs.release.ReleaseJob

/**
 * Deploys a release build to an environment.
 */
class DeployJob extends ReleaseJob {
    String environment

    @Override
    protected List<String> getTriggers() {
        []
    }

    @Override
    protected List<String> getSteps() {
        def containerName = "pwagner/${projectName}:\\\${TAG}"
        [
                'shell("""',
                // Dump tag to version.txt
                'TAG=\\$(echo \\$GERRIT_REFNAME | sed s!^refs/tags/!!g)',
                'echo \\${TAG} > version.txt',
                // Dump jenkins.json to project.json
                'git archive \\${TAG} jenkins.json | tar -x',
                "mv jenkins.json ${projectName}.json",
                '""")',

                // Deploy via flotilla
                "shell('''",
                'TAG=\\$(cat version.txt)',
                "tar -c ${projectName}.json | docker run -i " +
                        '-v /var/jenkins_home/.aws/:/root/.aws/ ' +
                        'pwagner/flotilla revision' +
                        " --name ${projectName}" +
                        " --environment ${environment}" +
                        " --env-var DOCKER_IMAGE=${containerName}" +
                        ' --highlander 600',
                "''')"
        ]
    }
}
