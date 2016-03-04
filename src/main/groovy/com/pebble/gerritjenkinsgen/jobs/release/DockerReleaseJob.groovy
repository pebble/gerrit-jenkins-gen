package com.pebble.gerritjenkinsgen.jobs.release

/**
 * Publish docker image with appropriate tag when pushed.
 */
class DockerReleaseJob extends ReleaseJob {
    @Override
    protected List<String> getSteps() {
        def steps = super.steps
        def containerName = "pwagner/${projectName}:\\\${TAG}"
        steps += [
                "shell('''",
                'TAG=\\$(cat version.txt)',
                'if [ -f package.json ]; then',
                '  cp package.json package.json.bak',
                '  cat package.json.bak | sed "s/\\\\"version\\\\":.*/\\\\"version\\\\": \\\\"\\${TAG}\\\\",/g" | tee package.json',
                'fi',
                "docker build -t ${containerName} .",
                "docker push ${containerName}",
                "''')"
        ]
        steps
    }
}
