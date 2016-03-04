package com.pebble.gerritjenkinsgen.jobs.verify

import com.pebble.gerritjenkinsgen.jobs.Job

/**
 * Triggers when a PatchSet is pushed to Gerrit.
 */
class VerifyJob extends Job {
    /**
     * Git SCM, customized to checkout the current PR.
     */
    @Override
    protected List<String> getScm() {
        [
                'git(',
                "'${gitUrl}',",
                '"\\$GERRIT_BRANCH"',
                ') { node ->',
                '  node/skipTag(true)',
                '  node/extensions {',
                '    "hudson.plugins.git.extensions.impl.BuildChooserSetting"{',
                '      buildChooser(class: "com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritTriggerBuildChooser", plugin:"gerrit-trigger@2.18.3") {',
                '        separator("#")',
                '      }',
                '    }',
                '  }',
                '  def remoteConfig = node/userRemoteConfigs/"hudson.plugins.git.UserRemoteConfig"',
                '  remoteConfig << refspec("\\$GERRIT_REFSPEC")',
                '}'
        ]
    }

    /**
     * Trigger when PatchSet is created in Gerrit.
     */
    @Override
    protected List<String> getTriggers() {
        [
                'gerrit {',
                '  events {',
                '    patchsetCreated()',
                '  }',
                "  project('${projectName}', 'ant:**')",
                "  buildSuccessful(10, null)",
                '}',
        ]
    }

    @Override
    int getQuietPeriod() {
        0
    }

    @Override
    int getLogRotationDays() {
        14
    }
}
