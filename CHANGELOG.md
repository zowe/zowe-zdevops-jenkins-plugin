# Zowe™ zDevOps Jenkins™ plugin Changelog

All notable changes to the Zowe zDevOps Jenkins Plugin will be documented in this file.

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
