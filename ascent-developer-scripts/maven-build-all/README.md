# maven-build-all.sh

A MacOS shell script that performs maven builds - and more - on all projects in a git directory.

1. Copy `maven-build-*.*` to your GIT_HOME folder (e.g. `/Users/YourAccountName/git/`).
2. To make the shell script executable:<br/>`$ chmod +x maven-build-all.sh`
3. To see available options:<br/>`$ ./maven-build-all.sh -h`

If new projects are created on the Ascent platform, they should be added to the `maven-build-projects.txt` file.

# Requirements

1. You must be set up for Ascent development, and have access to the git repos.<br/>See [Ascent Quick Start Guide](https://github.com/department-of-veterans-affairs/ascent-sample/wiki/DEV-:-Quick-Start-Guide-for-Ascent-Sample#build-and-test)
2. maven-build-projects.txt must be in the same directory as the script.<br/>
   This file is a simple listing of all ascent runtime project names, in the proper build dependency order,<br/>
   e.g.
   <table style="border-collapse:collapse;border-style:none">
   <tr><td colspan="3">ascent-libraries-parent</td></tr>
   <tr><td width="20%"></td><td colspan="2">ascent-test-framework</td></tr>
   <tr><td width="20%"> </td><td width="20%"> </td><td>ascent-framework</td></tr>
   <tr><td width="20%"> </td><td width="20%"> </td><td>... etc ...</td></tr>
   </table>
				
