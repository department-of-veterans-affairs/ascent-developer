# ascent-developer

This is a repository containing developer tools to assist with Ascent development. 

## /ascent-developer-vm
Provisions a linux VM pre-configured for Ascent development.
See [Ascent Quick Start Guide](https://github.com/department-of-veterans-affairs/ascent-sample/wiki/DEV-:-Quick-Start-Guide-for-Ascent-Sample#build-and-test)

# Shell Scrips

See the README.md files under the `ascent-developer-scripts/**` subdirectories.

## Java Programs
After performing a maven build on the project, `ascent-developer/target/ascent-developer.jar` can be used to run programs that have been coded.

Currently, the following are available:

"Versions" combs the POM files for all explicit artifact versions in all projects.
* Set properties as necessary in `src/main/resources/versions.properties`.
* Your local environment must have a GIT\_HOME environment variable (e.g. `GIT_HOME=/Users/MyAccount/git`) in which the Ascent projects have been cloned.
* To run the program from GIT\_HOME directory:<br/>`$ cd ${GIT\_HOME}`<br/>`$ java -cp target/ascent-developer.jar gov.va.ascent.tools.Versions .`
