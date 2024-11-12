# Zowe™ zDevOps Jenkins™ plugin Changelog

All notable changes to the Zowe zDevOps Jenkins Plugin will be documented in this file.

## 0.2.0 (2024-06-24)

### Features

* Feature: Added "deleteDataset" Jenkins pipeline declarative method ([687dbfae](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/687dbfae))
* Feature: Added "deleteDatasetsByMask" Jenkins pipeline declarative method ([3682fc6b](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/3682fc6b))
* Feature: Added Jenkins Freestyle UI method - "Allocate dataset" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Delete dataset/member" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Delete dataset by mask" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Download dataset-member" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Perform TSO command" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Write file to USS file" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Write file to dataset" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Write file to member" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Write text to USS file" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Write text to dataset" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added Jenkins Freestyle UI method - "Write text to member" ([4e4d73cc](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc))
* Feature: Added dataset member name validation ([b05c7436](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/b05c7436))
* Feature: Added hpi-builder.yml GitHub workflow for automated .hpi builds ([c0fd9f27](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/c0fd9f27))
* Feature: Added failOnExist parameter for allocateDS, deleteDataset and deleteDatasetsByMask declarative/Freestyle UI methods (checks the presence/absence of a dataset on the system) ([db232f49](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/db232f49))

### Bugfixes

* Bugfix: GitHub issue #13: spotbugs-maven-plugin found a bug during mvn install ([5fd7e5c7](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/5fd7e5c7))
* Bugfix: Fixed tests execution, ClassFilter error, added dep mngment, some updates and improvements in pom.xml ([60f4f2f0](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/60f4f2f0))

## 0.1.0 (2024-04-16)

### Breaking changes

* Breaking: Plugin redesigned for Maven, with additional features and improvements ([874bf32e](https://github.com/jenkinsci/zdevops-plugin/commit/874bf32e))

### Features

* Feature: Added the ability to add and save mainframe connection sessions using Jenkins Credential Manager
* Feature: Added "submitJob" Jenkins pipeline declarative method
* Feature: Added "submitJobSync" Jenkins pipeline declarative method
* Feature: Added "downloadDS" Jenkins pipeline declarative method
* Feature: Added "allocateDS" Jenkins pipeline declarative method
* Feature: Added "writeFileToDS" Jenkins pipeline declarative method
* Feature: Added "writeToDS" Jenkins pipeline declarative method
* Feature: Added "writeFileToMember" Jenkins pipeline declarative method
* Feature: Added "writeToMember" Jenkins pipeline declarative method
* Feature: Added "writeToFile" Jenkins pipeline declarative method
* Feature: Added "writeFileToFile" Jenkins pipeline declarative method
* Feature: Added Jenkins Freestyle UI method for JCL submit, RC waiting and displaying log output
* Feature: Added z/OSMF connection validation feature
* Feature: Added the ability to view the JCL job execution log (link to it) from the "Console output" in the Jenkins web interface

### Bugfixes

* Bugfix: Refactored packages and imports with io.jenkins.plugins  ([bf51f0b9](https://github.com/jenkinsci/zdevops-plugin/commit/bf51f0b9))
* Bugfix: Dependency fixes for later 0.1.0 release ([3d728f39](https://github.com/jenkinsci/zdevops-plugin/commit/3d728f39))