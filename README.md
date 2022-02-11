# zOS-DevOps-Jenkins-plugin
zOS DevOps Jenkins plugin development repo

# How to run in Debug:
- ./gradlew server --debug-jvm
- wait until `Attach debugger` appears in console
- click it
- wait until Jenkins is deployed
- open `localhost:8080`
- enjoy

# Use case
- Add a zosmf connection in settings (<b>Manage Jenkins -> Configure System -> z/OS Connection List</b>). Enter connection name, zosmf url, username and password.
- Create Pipeline plugin and open its configuration.
Create a section <b>zosmf</b> inside <b>steps</b> of <b>stage</b> and pass connection name as a parameter of section. Inside zosmf body invoke necessary zosmf functions (they will be automatically done in specified connection context). Take a look at example below:
```groovy
stage ("stage-name") {
    steps {
        // ...
        zosmf("connection-name") {
            submitJob "//'EXAMPLE.DATASET(JCLJOB)'"
            submitJobSync "//'EXAMPLE.DATASET(JCLJOB)'"
            downloadDS "USER.LIB(MEMBER)"
            downloadDS dsn:"USER.LIB(MEMBER)", vol:"VOL001"
            allocateDS dsn:"STV.TEST5", alcUnit:"TRK", dsOrg:"PS", primary:1, secondary:1, recFm:"FB"
            writeFileToDS dsn:"USER.DATASET", file:"workspaceFile"
            writeFileToDS dsn:"USER.DATASET", file:"D:\\files\\localFile"
            writeToDS dsn:"USER.DATASET", text:"Write this string to dataset"
            writeFileToMember dsn:"USER.DATASET", member:"MEMBER", file:"workspaceFile"
            writeFileToMember dsn:"USER.DATASET", member:"MEMBER", file:"D:\\files\\localFile"
            writeToMember dsn:"USER.DATASET", member:"MEMBER", text:"Write this string to member"
        }
        // ...
    }
}
```