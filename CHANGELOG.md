# Changelog

## Unreleased (2023-01-25)

#### Others

* package updates ([#101](https://github.com/hamlet-io/engine-core/issues/101))

Full set of changes: [`8.8.0...c996e68`](https://github.com/hamlet-io/engine-core/compare/8.8.0...c996e68)

## 8.8.0 (2022-10-13)

#### Others

* changelog bump ([#98](https://github.com/hamlet-io/engine-core/issues/98))
* (deps): bump jackson-databind from 2.10.5.1 to 2.12.6.1 in /freemarker-wrapper ([#99](https://github.com/hamlet-io/engine-core/issues/99))
* changelog bump
* changelog bump

Full set of changes: [`2.0.0, 8.6.0...8.8.0`](https://github.com/hamlet-io/engine-core/compare/2.0.0, 8.6.0...8.8.0)

## 2.0.0, 8.6.0 (2022-03-25)

#### New Features

* OS based images for bundled jar
* include docker based build and refactor test
* use .cmdb as a directory for cmdb related information
* hide stack trace on stop exception
* custom exceptions and return codes
* add toConsole method [#45](https://github.com/hamlet-io/engine-core/issues/45)
* support pretty print for toCMDB method
* pin the version for the release
* Files/Directories only search optimisation
* bump freemarker version
* pin the version
* pin the version
* set Format to json by default
* use hamlet instead of codeontap
* (cmdb): methods to update a CMDB
* (cicd): setup github actions
#### Fixes

* find for wrapper
* extract artifact after download
* use tar to preserve permissions
* tagging suffix for images
* docker build process and pipeline
* handle options with empty values
* tag syntax for actions
* default tags
* tag naming
* default tag for docker package
* (ci): update packaging process for artifacts ([#68](https://github.com/hamlet-io/engine-core/issues/68))
* (ci): invalid syntax in package workflow
* add .hamlet build info in docker image
* setup package version
* update engine version
* update artifact details
* (ci): test result description
* (ci): package syntax
* remove artifact upload on test
* push trigger
* create a file if it doesn't exest when call toCMDB
* add a test to cover rmCMDb specific file path
* adjust rmCMDB logic to remove a specified file
* method name for InitPluginsMethod
* ignore FileNotFoundException during wrapper parameters processing
* read Format parameter of toCMDB method
* add toConsole method to the freemarker input
* support a default value for sendTo
* mkdir method
* mkdir method
* mkdir method
* FilenameGlob processing
* (cmdb): always include isDirectory flag
* filesystem caching for all methods
* filesystem caching
* proper processing of IgnoreSubtreeAfterMatch flag
* nullpointer exception for getFileTree in case when there are CMDBs with common prefix
* comment out code that produces wrong JSON serialisation
* proper multiple plugin handling in mingw
* (plugin): multiple plugin handling in mingw
* (cmdb): fix mkdir method
* (logging): use Log4j2 for logging
* corrupting the plugin file system
* corrupting the plugin file system
#### Refactorings

* only split paramters on first =
* (ci): quality of life updates
#### Others

* changelog bump ([#90](https://github.com/hamlet-io/engine-core/issues/90))
* changelog bump ([#85](https://github.com/hamlet-io/engine-core/issues/85))
* (deps): bump log4j-api from 2.17.0 to 2.17.1 in /freemarker-wrapper ([#88](https://github.com/hamlet-io/engine-core/issues/88))
* (deps): bump log4j-core in /freemarker-wrapper ([#89](https://github.com/hamlet-io/engine-core/issues/89))
* changelog bump ([#80](https://github.com/hamlet-io/engine-core/issues/80))
* (deps): bump log4j-core in /freemarker-wrapper ([#84](https://github.com/hamlet-io/engine-core/issues/84))
* (deps): bump log4j-api from 2.16.0 to 2.17.0 in /freemarker-wrapper ([#83](https://github.com/hamlet-io/engine-core/issues/83))
* (deps): bump log4j-api from 2.15.0 to 2.16.0 in /freemarker-wrapper ([#81](https://github.com/hamlet-io/engine-core/issues/81))
* (deps): bump log4j-core in /freemarker-wrapper ([#82](https://github.com/hamlet-io/engine-core/issues/82))
* changelog bump ([#71](https://github.com/hamlet-io/engine-core/issues/71))
* (deps): bump log4j-core in /freemarker-wrapper ([#78](https://github.com/hamlet-io/engine-core/issues/78))
* (deps): bump log4j-api from 2.13.3 to 2.15.0 in /freemarker-wrapper ([#77](https://github.com/hamlet-io/engine-core/issues/77))
* use new package name for hamlet install
* changelog bump ([#65](https://github.com/hamlet-io/engine-core/issues/65))
* remove prebuilt jar versions
* update version
* (deps): bump commons-io from 2.6 to 2.7 in /freemarker-wrapper
* update version
* bump a minor version
* version bump
* rename getPlugins() to getPluginLayers()
* (deps): bump jackson-databind in /freemarker-wrapper
* (dist): add 1.12.1-rc version and clean up old ones
* (freemarker): bump freemarker version
* remove verbose output from createTemplate exeuction
