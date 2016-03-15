# gerrit-jenkins-gen

This project is intended to be triggered by [Jenkins Gerrit Trigger Plugin](https://github.com/jenkinsci/gerrit-trigger-plugin).
It generates [Jenkins Job DSL](https://github.com/jenkinsci/job-dsl-plugin) output in response to gerrit events.

The idea is to trigger this on the "Patch Set Created" event for all projects in Gerrit. It will generate Jenkins jobs according to the target project's configuration:

* If a jenkins.json file is defined, it is interpreted (see below).
* If a Dockerfile is found, a simple "does the Dockerfile work?" job is created.

Currently there's a bias towards NodeJS projects that are deployed via Docker.


### Jenkins.json

```
{
  "node": [
    "4.1",
    "4.2",
    "4.3",
    "4.4"
  ],
  "docker": {},
  "flotilla": {
    "pipeline": [
      "!develop",
      "staging"
    ],
    "defaults": {
      "REGIONS": ["us-east-1"],
      "DOCKER_PORT_80": 8080,
      "INSTANCE_TYPE": "t2.nano",
      "INSTANCE_MIN": "1",
      "INSTANCE_MAX": "2",
      "HEALTH_CHECK": "HTTP:80/"
    },
    "develop": {
      "MESSAGE": "hello develop 1054"
    },
    "staging": {
      "MESSAGE": "hello staging",
      "INSTANCE_MIN": "2"
    }
  }
}
```

The `node` block defines versions of NodeJS that should be used for testing.

The `docker` block designates the project is released via Docker.

The `flotilla` block defines a pipeline of [flotilla](https://github.com/pebble/flotilla) environments for deployment.



### Disclaimer

This is not used by Pebble, it's a [20% project](https://www.pebble.com/jobs) hacked together over 2 days.
