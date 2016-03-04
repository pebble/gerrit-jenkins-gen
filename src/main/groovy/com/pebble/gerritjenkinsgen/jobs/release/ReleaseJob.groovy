package com.pebble.gerritjenkinsgen.jobs.release

import com.pebble.gerritjenkinsgen.jobs.Job

/**
 * Triggers when a tag is pushed to publish it.
 */
class ReleaseJob extends Job {
    @Override
    protected List<String> getScm() {
        [
                "git('${gitUrl}') { node ->",
                '  node/skipTag(true)',
                '}'
        ]
    }

    /**
     * Trigger when a tag is updated.
     */
    @Override
    protected List<String> getTriggers() {
        [
                'gerrit {',
                '  events {',
                '    refUpdated()',
                '  }',
                "  project('${projectName}', 'ant:refs/tags/**')",
                "  buildSuccessful(10, null)",
                '}',
        ]
    }

    /**
     * Checkout ref and store tag to version.txt.
     */
    @Override
    protected List<String> getSteps() {
        [
                'shell("""',
                'git checkout \\$GERRIT_REFNAME',
                'git log -1',
                'TAG=\\$(echo \\$GERRIT_REFNAME | sed s!^refs/tags/!!g)',
                'echo \\${TAG} > version.txt',
                '""")'
        ]
    }

    /**
     * Update buildName with version.txt.
     */
    @Override
    protected List<String> getConfigure() {
        [
                'project/builders << "org.jenkinsci.plugins.buildnameupdater.BuildNameUpdater"(plugin:"build-name-setter@1.5.1") {',
                '  buildName "version.txt"',
                '  fromFile "true"',
                '}'
        ]
    }

    @Override
    int getQuietPeriod() {
        0
    }

    @Override
    int getLogRotationBuilds() {
        50
    }
}
