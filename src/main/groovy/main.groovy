import com.pebble.gerritjenkinsgen.JobGenerator

def env = System.getenv()
def projectName = env['GERRIT_PROJECT']
if (projectName != 'jenkins-seed') {
    def gerritHost = env['GERRIT_HOST']
    def gerritPort = env['GERRIT_PORT'] as Integer
    def gerritRefSpec = env['GERRIT_REFSPEC'] ?: 'master'

    JobGenerator generator = new JobGenerator(
            projectName: projectName,
            gerritHost: gerritHost,
            gerritPort: gerritPort,
            gerritRefSpec: gerritRefSpec
    )

    generator.generateJobs()
}
