package com.pebble.gerritjenkinsgen.jobs

/**
 * Definition for Job.
 */
abstract class Job {
    String jobName
    String projectName
    String gitUrl
    String pipelineNext
    String pipelineNextAuto

    /**
     * Return this job in jenkins-job-dsl format.
     * @return DSL format.
     */
    String toDsl() {
        StringBuilder sb = new StringBuilder()
        def axes = this.axes
        String jobType = axes != null ? 'matrixJob' : 'job'
        sb.append("${jobType}('${jobName}') {\n")
        sb.append("  quietPeriod(${quietPeriod})\n")
        sb.append("  logRotator(${logRotationDays}, ${logRotationBuilds})\n")

        addSection('axes', axes, sb)
        addSection('scm', scm, sb)
        addSection('triggers', triggers, sb)
        addSection('steps', steps, sb)
        addSection('publishers', publishers, sb)

        def configure = this.configure
        if (configure) {
            sb.append("  configure { project -> \n")
            configure.each { dataLine ->
                sb.append("    ${dataLine}\n")
            }
            sb.append('  }\n')
        }

        sb.append('}\n')
        return sb.toString()
    }

    protected List<String> getAxes() {
    }

    protected List<String> getScm() {
    }

    protected List<String> getTriggers() {
    }

    protected List<String> getSteps() {
    }

    protected List<String> getPublishers() {
        def publishers = []
        if (pipelineNext) {
            publishers += "  buildPipelineTrigger('${pipelineNext}')"
        }
        if (pipelineNextAuto) {
            publishers += [
                    '  downstreamParameterized {',
                    "    trigger('${pipelineNextAuto}') {",
                    '      condition("SUCCESS")',
                    '      parameters {',
                    '        currentBuild()',
                    '      }',
                    '    }',
                    '  }'
            ]
        }
        return publishers
    }

    protected List<String> getConfigure() {
    }

    int getQuietPeriod() {
        5
    }

    int getLogRotationDays() {
        -1
    }

    int getLogRotationBuilds() {
        -1
    }

    static void addSection(String label, Iterable<String> data, StringBuilder sb) {
        if (data) {
            sb.append("  ${label} {\n")
            data.each { dataLine ->
                sb.append("    ${dataLine}\n")
            }
            sb.append('  }\n')
        }
    }
}
