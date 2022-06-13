# Whitelist exporter

This tool is capable to export some json objects from pingaccess and pingfederate in a common format.

## Project structure

```
whitelist-exporter
│   jsecacerts
│   pom.xml
│   README.md
│
└─── src/main/java/at/bat/export/whitelists
│   │   Exporter.java
│   │   Main.java
│   │   RestAPIGetter.java
│   │   Utils.java
│   │
│   └─── auth
│       │   ...
│   │
│   └─── config
│       │   ...
│   │
│   └─── test
│       │   InstallCACerts.java
│       │   ...
│
└─── src/main/resources
│   │   *.json
│   │   config.properties
│   │   ...
│
└─── src/test/java/at/bat/export/whitelists
│   │   ExportIT.java
│   │   PingFederateAPIConfigurator.java
│   │   ...
│
└─── src/test/resources
│   │   *.json
│   │   test.properties
│   │   ...
```
Main artifacts:
- jsecacerts - java trust store used by java application at runtime
- pom.xml - maven tool configuration file used to guide the build and test processes
- Main.java - the entry point of java application
- InstallCACerts.java - this is an executable class that creates/updates the java trust store
- config.properties - the configuration file for java application
- test.properties - the configuration file for integration tests

Other artifacts:
- Exporter.java - the class that makes the export process
- RestAPIGetter.java - the class that brings the support of rest api calls
- Utils.java - adds some utilities
- src/main/resources/*.json - example json files used just as examples for api calls
- src/test/resources/*.json - json files used to configure pingfederate and pingaccess

## How to build executable jar

```
$ cd root_of_git_repo
$ mvn clean package assembly:single
```

## How to run executable jar

```
$ cd root_of_git_repo
$ java -Djavax.net.ssl.trustStore=jssecacerts -Djavax.net.ssl.trustStorePassword=changeit -jar target/whitelist-exporter-1.0-SNAPSHOT-jar-with-dependencies.jar -zone TEST
```

## How to import self signed certicates in java trust store

To import those self signed certificates you should:
- update InstallCACerts class with your services
- run InstallCACerts class to update the local java trust store

## How to run integration tests

Integration tests are disabled by default.
Firstly enable them updating the pom.xml file:
```
<skip.tests>false</skip.tests>
```

Run the build and test process:
```
$ cd root_of_git_repo
$ mvn clean verify
```

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://git.bat.at/PingIdentity/whitelist-exporter.git
git branch -M main
git push -uf origin main
```

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Automatically merge when pipeline succeeds](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/index.html)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing(SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

