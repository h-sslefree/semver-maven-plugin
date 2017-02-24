
# semver-maven-plugin

The semver-maven-plugin is used to determine the next version of a MAVEN project. Symantic versioning is used to specify the version symantics.



Check: [semver.org](https://www.semver.org) for more information

## Usage

Include the following depedency in your pom.xml

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>semver-maven-plugin</artifactId>
  <version>${semver.maven.plugin-version}</version>
</plugin>
```

The latest version of the semver-maven-plugin is:

```
<semver.maven.plugin-version>0.1.0</semver.maven.plugin-version>
```

It has be used in combination with the **maven-release-plugin**. 

It generates the `release.properties` and then the *release:prepare*-goal can be used to make the maven release-artifact.
  
In version 3.0.0 the native-method is developed to get rid of the combination with the  

## Goals

* *patch*

Create a bug-fix on your project: 0.0.x. 

* *minor*

Create a non-breaking new feature on your project: 0.x.0.

* *major*

Create a breaking changes in ygour project: x.0.0.

* *cleanup-git-tags*

Cleanup remote and local **build-**tags. Those are your to let the buildserver (Jenkins) know if a new build is needed. Not everey tag has a build attached to it.

**Example:**

```mvn semver:patch```


## Build semver-maven-plugin

mvn install