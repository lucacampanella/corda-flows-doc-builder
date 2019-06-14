# Corda flows doc builder plugin
Gradle plugin to automatically generate graph that represent Corda flows interactions. 
The plugin is intended to help developers automatically build documentation regarding the 
[Corda](https://www.corda.net/) flows they write. 

The graphs built by the plugin try to
closely follow the guidelines given by Corda on how to write such documentation. 
(See [Carda modelling notation](https://solutions.corda.net/corda-modelling-notation/views/views-flow-sequence.html))
The plugin works by decompiling the class files inside the flows jar, looking for
classes that represent flows and statically analyzing them for `send` and `receive` calls.

With the information obtained from static analysis the .svg images useful for documentation
are built. They are then integrated into an ascii doc file giving a summary of the analysis of the Jar.

## Getting Started

The doc builder can be used in two ways:
- as a Gradle plugin
- as a standalone executable

### Installing

#### As a Gradle plugin

To apply the plugin, in the `build.gradle`:

Using the plugin DSL

<!-- //todo: use final version here, use right repo  -->

```
plugins {
  id "com.github.lucacampanella.plugin.flows-doc-builder-plugin" version "0.0.0-SNAPSHOT"
}
```

Or using legacy plugin application:

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.lucacampanella:plugin:0.0.0-SNAPSHOT"
  }
}

apply plugin: "com.github.lucacampanella.plugin.flows-doc-builder-plugin"
```

Be careful: in any case the plugin should be applied after the Java plugin or any other plugin that creates 
a Jar task. Otherwise the plugin won't find these tasks and won't analyze the corresponding Jars.

#### As a standalone executable
Download latest version of the Jar from 
https://bintray.com/lucacampanella/mvn-release/graph-builder
//todo: check this is the latest link


## Usage

### As a Gradle plugin

The plugin hooks itself into the tasks of type Jar, creating an analyze task for each Jar task. 
The tasks created have the format `<originalTaskName>AnalyzerTask`. For example the task that analyzes
the default Jar is called: `jarAnalyzerTask`. 

To analyze a jar just call his corresponding analyzer task. Example, to analyze the Jar output of the
`Jar` task use `./gradlew jarAnalyzerTask`

To see all the available analyzer tasks run: `./gradlew listFlowAnalysisTasks`

By default the documentation graphs are put inside `build/reports/flowsdocbuilder` in .svg and .adoc format.

#### Configurable options
For each analyzer task you can configure various options:
- `outPath`: the path where the documentation files are placed. 
Example: `outPath = "myFavouriteFolder/docs"`
Default: `build/reports/flowsdocbuilder`  
- `logLevel`: the log level to use for logging inside the task. It should be one of the 
[slf4j log levels](https://www.slf4j.org/apidocs/org/slf4j/event/Level.html).  
Example: `logLevel = Level.TRACE` or `logLevel = Level.DEBUG`
Default: defaults to gradle specified log level. For example run task with `--info`
option to obtain slf4j `Level.DEBUG`. If the `logLevel` is specified by the user in the task
this always takes precedence over gradle specified log level.
-  `removeJavaAgents`: decide whether you want the plugin to remove java agents from
the execution of the task. It's very common in Corda development environments to have
the Quasar plugin modify every `JavaExec` task to have as `jvmArg` of the task something like
`-javaagent:path/to/quasar.jar`. This just slows the analysis down and sometimes 
creates unrelated exceptions, thus is disabled by default, but user can decide
to leave the java agent by selecting `removeJavaAgents = false`.  
Example: `removeJavaAgents = false`  
Default: `true`
- `pathToExecJar`: change the jar executable used for the analysis. By default the
plugin relies on the analysis engine that can be found in this repo under the `graph-builder`
 submodule. The dependency is automatically fetched and exceuted by gradle when the plugin
 is applied. If the user wants to change this for any reason can use this option.  
 Example: `pathToExecJar = "path/to/my/analyzerExtended.jar"`
 Default: the latest version of the Jar is downloaded from the artifactory automatically 
 Gradle dependency handler.
 

For example using the Groovy DSL:
```
jarAnalyzerTask {
    outPath = project.buildDir.path + "/reports/differentdir/flowsdocbuilder"
    logLevel = Level.TRACE
    removeJavaAgents = false
    pathToExecJar = "path/to/my/analyzerExtended.jar"
}
```

#### Tips
The plugin works best on fat/uber jars with all the dependencies inside. The best way to achieve such a jar
is to use the [shadow plugin](https://github.com/johnrengelman/shadow) to create the fat jar and then run
`shadowJarAnalyzerTask`

### As a standalone executable
Run with:
```
java -jar [-Dorg.slf4j.simpleLogger.defaultLogLevel=<logLevel>] graph-builder-<version>-all.jar <path/to/input_jar.jar> [path/to/additionalClasspathJars.jar ...] [-o <path/to/output_folder>]
```
Meaning:
- `-Dorg.slf4j.simpleLogger.defaultLogLevel=<logLevel>`: optional parameter to decide the 
log level. Complete `<logLevel>` with one of the 
[slf4j log levels](https://www.slf4j.org/apidocs/org/slf4j/event/Level.html).
- `graph-builder-<version>-all.jar`: the analyzer executable 
- `<path/to/input_jar.jar>`: path to the jar to be analyzed
- `path/to/additionalClasspathJars.jar ...`: optional, add more jars to the analysis. Especially
useful when the jar to be analyzed is not a fat Jar and the analysis still needs some dependencies.
- `-o <path/to/output_folder>`: the output folder where the resulting documentation should be placed.  
Default: `graphs`

## Running the tests

After cloning the repo, run `./gradlew test`

## Built With

* [Gradle](https://gradle.org/) - Gradle build tool
* [Spoon](http://spoon.gforge.inria.fr/) - Static analysis and decompilation library
* [JFreeSVG](http://www.jfree.org/jfreesvg/) - Library to draw SVG files in java

## Contributing

//todo
Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [Reckon Gradle Plugin](https://github.com/ajoberstar/reckon) for versioning. For the versions available, 
see the [tags on this repository](https://github.com/lucacampanella/corda-flows-doc-builder/tags). 

## Authors

* **Luca Campanella** - *Initial work* - [GitHub](https://github.com/lucacampanella)

<!-- See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project. -->

This project was developed for [AdNovum](https://www.adnovum.ch/) in the scope of the projects
[SB4B](https://www.adnovum.ch/en/company/media/media_releases/2018/adnovum_launches_secure_blockchain_for_business.html) 
and [Cardossier](https://www.adnovum.ch/en/innovation/blockchain_car_dossier.html).

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details

## Acknowledgments
//todo
* Special and huge thanks goes to Michael von Känel for answering all my questions thoroughly and always with a smile
* Hat tip to anyone whose code was used

