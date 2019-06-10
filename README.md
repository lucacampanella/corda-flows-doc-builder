# Corda flows doc builder plugin
Gradle plugin to automatically generate graph that represent Corda flows interactions. 
The plugin is intended to help developers automatically build documentation regarding the 
[Corda](https://www.corda.net/) flows they write. The graphs built by the plugin try to
closely follow the guidelines given by Corda on how to write such documentation.
//todo: here put link to documentation
The plugin works by decompiling the class files inside the flows jar, looking for
classes that represent flows and statically analyzing them for send and receive.
Static analysis is performed using the [Spoon library](http://spoon.gforge.inria.fr/).
With the information obtained from static analysis the .svg images useful for documentation
are built.

## Getting Started

The doc builder can be used in two ways:
- as a Gradle plugin
- as a standalone executable

### Prerequisites

//todo

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

The plugin hooks itself into the tasks of type Jar and analyses the classes inside the
Jar. The documentation graphs are put inside `build/graphs` in .svg format.

#### As a standalone executable
Download latest version of the Jar from 
https://bintray.com/lucacampanella/mvn-release/graph-builder
//todo: check this is the latest link

Run with:
```
java -jar graph-builder-0.96.0-all.jar <path/to/input_jar.jar> <path/to/output_folder>
```

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

We use [Reckon Gradle Plugin](https://github.com/ajoberstar/reckon) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

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
* Hat tip to anyone whose code was used
* Inspiration
* etc

