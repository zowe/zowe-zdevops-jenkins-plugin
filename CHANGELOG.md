# Zowe™ zDevOps Jenkins™ plugin Changelog

All notable changes to the Zowe zDevOps Jenkins Plugin will be documented in this file.

## 0.2.0 (2024-06-24)

### Features

* Feature: Added "deleteDataset" Jenkins pipeline declarative method ([a7f6021](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/687dbfae054fe00acacf8d583169e553033702dc))
* Feature: Added "deleteDatasetsByMask" Jenkins pipeline declarative method ([687dbfa](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/3682fc6b3ecd8ea133e8b02ca227147d3574c2bc))
* Feature: Added Jenkins Freestyle UI method - "Allocate dataset" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Delete dataset/member" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Delete dataset by mask" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Download dataset-member" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Perform TSO command" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Write file to USS file" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Write file to dataset" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Write file to member" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Write text to USS file" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Write text to dataset" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added Jenkins Freestyle UI method - "Write text to member" ([4e4d73c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/4e4d73cc191cfda5aeed37fbc793ec65c046f7a7))
* Feature: Added dataset member name validation ([111327e](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/b05c7436ce4d86218d302c3966c0896ce074ee32))
* Feature: Added hpi-builder.yml GitHub workflow for automated .hpi builds ([c0fd9f2](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/c0fd9f271928d5058b8f0fe736ea9e9b3ff73707))

### Bugfixes

* Bugfix: GitHub issue #13: spotbugs-maven-plugin found a bug during mvn install ([5fd7e5c](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/5fd7e5c7389248adbba65aae054e07e81f3eeaa8))
* Bugfix: Fixed tests execution, ClassFilter error, added dep mngment, some updates and improvements in pom.xml ([60f4f2f](https://github.com/zowe/zowe-zdevops-jenkins-plugin/commit/60f4f2f0444253a0cfd0e7c56d6626e430a53fbc))

## 0.1.0 (2024-04-16)

### Breaking changes

* Breaking: Plugin redesigned for Maven, with additional features and improvements ([874bf32](https://github.com/jenkinsci/zdevops-plugin/commit/874bf32e78db200740a6507ee344eccbfaae5ddb))

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

* Bugfix: Refactored packages and imports with io.jenkins.plugins  ([c73226f6](https://github.com/jenkinsci/zdevops-plugin/commit/bf51f0b9b6405f6f7e1001e51bce22ecfc1645bd)
* Bugfix: Dependency fixes for later 0.1.0 release ([629da4b](https://github.com/jenkinsci/zdevops-plugin/commit/3d728f39917eff398f62c4351f49111bc3f3a000))
