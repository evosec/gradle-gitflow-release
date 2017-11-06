# gradle-gitflow-release

[![Build Status](https://travis-ci.org/evosec/gradle-gitflow-release.svg?branch=develop)](https://travis-ci.org/evosec/gradle-gitflow-release)

## Usage

### Apply the plugin

Add one of the following snippets to your project

```
plugins {
    id "de.evosec.gradle.gitflow.release" version "${latest.version}"
}
```
or
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "de.evosec:gradle-gitflow-release:${latest.version}"
    }
}

apply plugin: "de.evosec.gradle.gitflow.release"
```

**NOTE**: For projects with multiple modules apply the plugin to the toplevel `build.gradle` and not inside the `allprojects { }` block.

### Configure the plugin
In your `build.gradle`
```
release {
    username "git_username"                  // Username to be used to push changes. Default: null
    password "git_password"                  // Password to be used to push changes. Default: null
    failOnSnapshotDependencies true          // Should the release start fail when SNAPSHOT dependencies are found? Default: true
    pushAfterReleaseFinish false             // Should the plugin push changes to remote after release finish? Default: false
    incrementMinorVersion false              // Should the plugin increment the minor part of the version instead of the patch level? Default: false
    versionPropertyFile "gradle.properties"  // properties file containing the "version" property. Default: gradle.properties  
}
```
In your `gradle.properties` or the file configured in `versionPropertyFile`
```
version=1.0.0-SNAPSHOT
```

### Doing a release
```
./gradlew releaseStart
./gradlew build             // or any other task to build your artifact 
./gradlew releaseFinish
```

#### Overriding configured properties
You can override all properties that can be configured in `build.gradle` by passing them as parameters to the gradlew call.

For example you can set the git credentials using command line parameters that you do not need to save them in your `build.gradle`
```
./gradlew releaseFinish -Prelease.username=myuser -Prelease.password=mypassword
```
or in a CI build by using environment variables
```
./gradlew releaseFinish -Prelease.username="${GIT_USERNAME}" -Prelease.password="${GIT_PASSWORD}"
```
