#!/bin/sh

## turn on to assist debugging ##
#export PS4='[$LINENO] '
#set -x
##

echo ""
echo "========================================================================="
# useful variables
cwd=`pwd`
thisScript="$0"
args="$@"
orderedProjects="ascent-libraries-parent"
dirProjects=""
lstProjUrls=""
platformProjectsDir="ascent-platform"
platformProjectsFile="$platformProjectsDir/build-projects.txt"
allProjectsFile="maven-build-projects.txt"
githubBaseUrl="https://github.com/department-of-veterans-affairs/"
gitExtension=".git"
mvnBuildLog="mvn_build.log"
# commands are set in get_args() based on command-line arguments
cmdGitCloneAll=""
cmdGitCloneAllMsg="-a :incl: git clone <>                   › ✗"
cmdSkipGitPull=""
cmdSkipGitPullMsg="-p :skip: git pull                       › ✗"
cmdSkipMvnBuild=true
cmdSkipMvnBuildMsg="-m :skip: mvn <>                         › ✗"
cmdForceUpdate=" -U"
cmdForceUpdateMsg="-u :skip: mvn validate -U                › ✗"
cmdSkipInstall=false
cmdSkipInstallMsg="-2 :skip: mvn install                    › ✗"
cmdUnitTests=""
cmdUnitTestsMsg="-t :incl: mvn <> -skiptTests=true        › ✗"
cmdDockerClean=""
cmdDockerCleanMsg="-r :incl: docker rmi <build-images>      › ✗"
cmdDockerBuild=""
cmdDockerBuildMsg="-d :incl: mvn <> -DskipDockerBuild       › ✗"
cmdSonar=""
cmdSonarMsg="-s :incl: mvn sonar:sonar                › ✗"
cmdFortifyScan=""
cmdFortifyScanMsg="-f :incl: mvn antrun:run@fortify-scan -N › ✗"

## get argument options off of the command line        ##
## required parameter: array of command-line arguments ##
## scope: private (internal calls only)                ##
function get_args() {
	#  echo "args: $@"
	# while getopts ":hapmu2trdsf" opt; do
	while getopts ":hapmu2trdsf" opt; do
		case $opt in
			h)
				# echo "-h \> show help" >&2
				show_help
				;;
			a)
				# echo "-a \> cloning all projects" >&2
				cmdGitCloneAll=true
				cmdGitCloneAllMsg="$cmdGitCloneAllMsg\b✔︎ all"
				;;
			p)
				# echo "-p \> skipping git pulls" >&2
				cmdSkipGitPull=true
				cmdSkipGitPullMsg="$cmdSkipGitPullMsg\b✔︎ skip"
				;;
			m)
				# echo "-m \> skipping maven builds" >&2
				cmdSkipMvnBuild=false
				cmdSkipMvnBuildMsg="$cmdSkipMvnBuildMsg\b✔︎ skip"
				# also turn off related build activities
				cmdForceUpdate=""
				cmdForceUpdateMsg="$cmdForceUpdateMsg\b﹅ n/a"
				cmdSkipInstall=true
				cmdSkipInstallMsg="$cmdSkipInstallMsg\b﹅ n/a"
				cmdUnitTests=" -DskipTests=true"
				cmdUnitTestsMsg="$cmdUnitTestsMsg\b﹅ n/a"
				cmdDockerBuild=" -DskipDockerBuild"
				cmdDockerBuildMsg="$cmdDockerBuildMsg\b﹅ n/a"
				;;
			u)
				# echo "-g \> skipping maven forced update" >&2
				cmdForceUpdate=""
				cmdForceUpdateMsg="$cmdForceUpdateMsg\b✔︎ skip"
				;;
			2)
				# echo "-2 \> skipping recompile after forced update" >&2
				cmdSkipInstall=true
				cmdSkipInstallMsg="$cmdSkipInstallMsg\b✔︎ skip"
				;;
			t)
				# echo "-t \> skipping unit tests" >&2
				cmdUnitTests=" -DskipTests=true"
				cmdUnitTestsMsg="$cmdUnitTestsMsg\b✔︎ as shown"
				;;
			r)
				# echo "-r \> remove docker images" >&2
				cmdDockerClean=true
				cmdDockerCleanMsg="$cmdDockerCleanMsg\b✔︎ run"
				;;
			d)
				# echo "-d \> skipping docker builds" >&2
				cmdDockerBuild=" -DskipDockerBuild"
				cmdDockerBuildMsg="$cmdDockerBuildMsg\b✔︎ as shown"
				;;
			s)
				# echo "-d \> running sonar analysis" >&2
				cmdSonar=true
				cmdSonarMsg="$cmdSonarMsg\b✔︎ run"
				;;
			f)
				# echo "-d \> running fortify analysis" >&2
				cmdFortifyScan=true
				cmdFortifyScanMsg="$cmdFortifyScanMsg\b✔︎ run"
				;;
			\?)
				echo "Invalid option: -$OPTARG" >&2
				echo "Press Ctrl+C to abort, Enter to continue "
				read
				;;
		esac
	done
}

## function to exit immediately         ##
## required parameter: exit code to use ##
## scope: private (internal calls only) ##
function exit_now() {
	code=$1
	if [ -z $code ]; then
		code="0"
	else
		# check reserved exit codes
		if [ "$code" -eq "126" ]; then
			# Permission problem or command is not an executable
			echo " *** ERROR: Invalid permissions or non-executable command ... aborting immediately" >&2
		elif [ "$code" -eq "127" ]; then
			# Possible problem with $PATH or a typo
			echo " *** ERROR: 'mvn' command not found (check \$PATH) ... aborting immediately" >&2
		elif [ "$code" -eq "130" ]; then
			# Ctrl+C was pressed
			echo " *** Interrupted (Ctrl_C) ... aborting immediately" >&2
		elif [ "$code" -ge "128" ]; then
			# Fatal error signal 128+n
			echo " *** FATAL error (signal $code) ... aborting immediately" >&2
		fi
	fi
	exit $code
}

## function to display help             ##
## scope: private (internal calls only) ##
function show_help() {
	echo "" >&2
	echo "Build cloned ascent maven projects:  $thisScript [-h|p|m|u|t|r|d|s|f]" >&2
	echo "  -a   clone all ascent & vetservices runtime projects, not just platform" >&2
	echo "  -p   skip 'git pull' before building" >&2
	echo "  -m   skip 'mvn clean install [...]' (do not build)" >&2
	echo "  -u   skip forced update of snapshot dependencies during maven build" >&2
	echo "  -t   skip unit tests after maven build" >&2
	echo "  -r   remove all ascent-related docker images" >&2
	echo "  -d   skip re-building docker images" >&2
	echo "  -s   run sonar static code analysis" >&2
	echo "  -f   run fortify static code analysis" >&2
	echo "  -h   this help" >&2
	echo "" >&2
	echo "Notes:" >&2
	echo "• This script operates on local cloned projects (-a will clone all for you)." >&2
	echo "• This script must be located in the git directory that represents" >&2
	echo "  the root for all ascent projects, e.g. ~/.git/ascent or ~/.git" >&2
	echo "• It is recommended that a git credential helper be utilized to" >&2
	echo "  eliminate authentication requests while executing. For more info see" >&2
	echo "  https://help.github.com/articles/caching-your-github-password-in-git/"
	echo "• This is an interactive script, and must run in a console or terminal." >&2
	echo "• Output from the pulls and builds can be found in ./$mvnBuildLog" >&2
	echo "  Each run of this script will overwrite any previous logs." >&2
	echo "• Remember to refresh your eclipse workspace after running this script." >&2
	echo "• To view output logs:   \$ less $mvnBuildLog" >&2
	echo "" >&2
	echo "Examples:" >&2
	echo "  \$ $thisScript -putd" >&2
	echo "  \$ $thisScript -p -d" >&2
	echo "" >&2
	# force exit
	exit_now
}

## function to prepare the list of project directories ##
## scope: private (internal calls only)                ##
function make_dir_list() {
	cd $cwd

	if [ -z "$allProjectsFile" ]; then
		echo "✶✶✶ FAILURE File $allProjectsFile is missing. Cannot process any projects." >&2
		echo "✶✶✶ FAILURE File $allProjectsFile is missing. Cannot process any projects." >> $cwd/$mvnBuildLog 2>&1
		exit_now 1
		### COULD DO SOMETHING LIKE BELOW, BUT WOULD ONLY INCLUDE PROJECTS ALREADY CLONED
		### AND THEY WOULD BE PROCESSED OUT OF ORDER
		# ls -l $cwd | grep ^d | sed 's:.*\ ::g' > dirProjects.txt # doesn't handle filenames with spaces
		#	find . -name '[!.]*' -type d -maxdepth 1 | sed 's:\.\/::g' > $cwd/dirProjects.txt
		#	while read line
		#	do
		#		dirProjects+=( "$line" )
		#	done < $cwd/dirProjects.txt
		#	rm -f $cwd/dirProjects.txt >> $cwd/$mvnBuildLog 2>&1
	else
		# read file
		while read line
		do
			dirProjects+=( "$line" )
		done < $allProjectsFile
	fi
	### OPTION TO ADD  -i to include any other arbitrary projects already in the git directory
	## SOMETHING LIKE THIS MIGHT WORK ...
	# ls -l $cwd | grep ^d | sed 's:.*\ ::g' > dirProjects.txt # doesn't handle filenames with spaces
	#	find . -name '[!.]*' -type d -maxdepth 1 | sed 's:\.\/::g' > $cwd/dirProjects.txt
	#	while read line
	#	do
	#		if [[ "$line" =~ ^($dirProjects)$ ]]; then
	#			# skip it
	#		else
	#			dirProjects+=( "$line" )
	#		fi
	#	done < $cwd/dirProjects.txt
	#	rm -f $cwd/dirProjects.txt >> $cwd/$mvnBuildLog 2>&1
}

## perform git pull in the current directory ##
## scope: private (internal calls only)      ##
function git_pull() {
	# get the relative directory name
	dirname=$1
	if [ "$dirnam" == "" ]; then
		dirname=`pwd` | sed 's:.*\/::g'
	fi

	# messages
	echo "" >> $cwd/$mvnBuildLog 2>&1
	if [ ! -d .git ]; then
		echo "    • not a git repo, skipping git pull" >&2
		echo "$dirname is not a git repo, skipping git pull" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
	else
		echo "$dirname: git pull" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		printf "    › git pull » " >&2

		# pull
		git pull >> $cwd/$mvnBuildLog 2>&1
		gitStatus="$?"
		if [ "$gitStatus" -eq "0" ]; then
			echo "[OK]" >&2
		else
			echo "✶✶✶ FAILURE, 'git pull' exited with $gitStatus, see $mvnBuildLog for details" >&2
		fi
	fi
}

## function to perform one maven build                    ##
# optional parameters:                                    ##
#     1 relative directory name                           ##
#     2 maven goals                                       ##
function maven_build() {
	# abort if command line arg prohibits git pulls
	if ! $cmdSkipMvnBuild ; then
		echo "Skipping 'mvn clean install [...]'" >> $cwd/$mvnBuildLog 2>&1
		return
	fi

	dirname=$1
	mvnGoals=$2
	mvnStatus="0"
	if [ "$dirnam" == "" ]; then
		dirname=`pwd` | sed 's:.*\/::g'
	fi
	if [ "$mvnGoals" == "" ]; then
		mvnGoals="clean install"
	fi

	if [ -f pom.xml ]; then
		#######  minimalist maven activity to force artifact update  #######
		if [ "$cmdForceUpdate" != "" ]; then
			echo "" >> $cwd/$mvnBuildLog 2>&1
			echo "$dirname: mvn clean validate$cmdForceUpdate " >> $cwd/$mvnBuildLog 2>&1
			echo "" >> $cwd/$mvnBuildLog 2>&1
			printf "    › mvn clean validate$cmdForceUpdate » " >&2

			# run maven -U
			mvn clean validate $cmdForceUpdate >> $cwd/$mvnBuildLog 2>&1
			mvnStatus="$?"
			if [ "$mvnStatus" -eq "0" ]; then
				echo "[OK]" >&2
			elif [ "$mvnStatus" -ge "126" ]; then
				echo ""
				exit_now $mvnStatus
			else
				echo "*** FAILURE, 'mvn' exited with $mvnStatus - see $mvnBuildLog for details" >&2
			fi
		fi

		# run maven goals
		if [ "cmdSkipInstall" != "true" ]; then
			echo "" >> $cwd/$mvnBuildLog 2>&1
			echo "$dirname: mvn $mvnGoals$tmpNexusDeploy$cmdUnitTests$cmdDockerBuild" >> $cwd/$mvnBuildLog 2>&1
			echo "" >> $cwd/$mvnBuildLog 2>&1
			printf "    › mvn $mvnGoals$tmpNexusDeploy$cmdUnitTests$cmdDockerBuild » " >&2

			# run maven
			mvn $mvnGoals$tmpNexusDeploy$cmdUnitTests$cmdDockerBuild >> $cwd/$mvnBuildLog 2>&1
			mvnStatus="$?"
			if [ "$mvnStatus" -eq "0" ]; then
				echo "[OK]" >&2
			elif [ "$mvnStatus" -ge "126" ]; then
				echo ""
				exit_now $mvnStatus
			else
				echo "*** FAILURE, 'mvn' exited with $mvnStatus - see $mvnBuildLog for details" >&2
			fi
		fi

	else
		echo "    • pom.xml not found, skipping maven build *** you must compile this project yourself" >&2
	fi
	return "$mvnStatus"
}

function docker_clean() {
	if [ $cmdDockerClean ]; then
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "Cleaning Docker Images ... " >> $cwd/$mvnBuildLog 2>&1
		echo "" >&2
		printf "Cleaning Docker Images ." >&2
		#		echo "▶︎ docker images -a --format \"{{.ID}}:\t{{.Repository}}\" | grep "ascent\|rabbitmq\|localstack\|fluentd\|consul\|vault\|redis" | grep -Eiv \"sonarqube\" | grep -Eiv \"alpine\" |cut -f 1 -d \":\"";
		TMP_DEL="ascent\|rabbitmq\|localstack\|fluentd\|consul\|vault\|redis\|jenkins\|kibana\|elasticsearch";
		TMP_KEEP="sonarqube|alpine";
		printf "." >&2
		docker rmi -fq $(docker images -a --format "{{.ID}}:\t{{.Repository}}" | grep "$TMP_DEL" | grep -Eiv "$TMP_KEEP" | cut -f 1 -d ":");  >> $cwd/$mvnBuildLog 2>&1
		printf "." >&2
		docker rmi -fq $(docker images -f dangling=true -q);  >> $cwd/$mvnBuildLog 2>&1
		printf "." >&2
		cd $cwd >> $cwd/$mvnBuildLog 2>&1
		echo "[OK]" >&2
		#		echo "  Remaining images ..." >&2
		#		docker images >&2
		echo "" >&2
		echo "  Remaining images ..." >> $cwd/$mvnBuildLog 2>&1
		docker images >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
	fi
}

## function to run sonar                ##
## scope: private (internal calls only) ##
function run_sonar() {
	if [ $cmdSonar ]; then
		# logfile message
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "mvn sonar:sonar" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		# console message
		printf "    › mvn sonar:sonar » " >&2

		if [ -f pom.xml ]; then
			# run sonar
			mvn sonar:sonar >> $cwd/$mvnBuildLog 2>&1
			mvnStatus="$?"
			if [ "$mvnStatus" -eq "0" ]; then
				echo "[OK]" >&2
			elif [ "$mvnStatus" -ge "126" ]; then
				echo ""
				exit_now $mvnStatus
			else
				echo "*** FAILURE, 'mvn' exited with $mvnStatus - see $mvnBuildLog for details" >&2
			fi
		else
			echo "    • pom.xml not found, skipping" >&2
		fi
	fi
}

## function to run fortify              ##
## scope: private (internal calls only) ##
function run_fortify() {
	if [ $cmdFortifyScan ]; then
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "mvn antrun:run@fortify-scan" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		printf "    › mvn antrun:run@fortify-scan » " >&2

		if [ -f pom.xml ]; then
			# run fortify
			mvn antrun:run@fortify-scan >> $cwd/$mvnBuildLog 2>&1
			mvnStatus="$?"
			if [ "$mvnStatus" -eq "0" ]; then
				echo "[OK]" >&2
			elif [ "$mvnStatus" -ge "126" ]; then
				echo ""
				exit_now $mvnStatus
			else
				echo "*** FAILURE, 'mvn' exited with $mvnStatus - see $mvnBuildLog for details" >&2
			fi
		else
			echo "    • pom.xml not found, skipping" >&2
		fi
	fi
}

## function to check out projects as necessary using URLs provided in ##
## $platformProjectsFile (one project URL per line)                            ##
## scope: private (internal calls only)                               ##
function check_out_projects() {
	# abort if command line arg prohibits git pulls
	if [ $cmdSkipGitPull ]; then
		echo "Skipping 'git pull'" >> $cwd/$mvnBuildLog 2>&1
		return
	fi

	cd $cwd

	if [ cmdGitCloneAll ]; then
		#### get lstProjUrls from ./maven-build-projects.txt
		if [ -z "$allProjectsFile" ]; then
			echo "*** File $allProjectsFile is missing. Cannot clone any projects." >&2
			echo "*** File $allProjectsFile is missing. Cannot clone any projects." >> $cwd/$mvnBuildLog 2>&1
		else
			# read file
			while read line
			do
				lstProjUrls+=( "$githubBaseUrl$line$gitExtension" )
			done < $allProjectsFile
		fi
	else
		#### get lstProjUrls from ./ascent-platform/build-projects.txt
		if [ ! -d "$platformProjectsDir" ]; then
			echo "Project $platformProjectsDir is not cloned. Aborting checkout of new platform projects." >&2
			echo "Project $platformProjectsDir is not cloned. Aborting checkout of new platform projects." >> $cwd/$mvnBuildLog 2>&1
		else
			# first get latest from ascent-platform so platformProjectsFile is up to date
			cd $cwd/ascent-platform
			echo "Pulling updates for $platformProjectsFile » " >> $cwd/$mvnBuildLog 2>&1
			printf "Pulling updates for $platformProjectsFile » " >&2
			git pull >> $cwd/$mvnBuildLog 2>&1
			gitStatus="$?"
			if [ "$gitStatus" -ne "0" ]; then
				echo "✶✶✶ FAILURE, 'git pull' exited with $gitStatus, see $mvnBuildLog for details" >&2
			else
				echo "[OK]" >&2
			fi
			cd $cwd

			if [ -z "$platformProjectsFile" ]; then
				echo "File $platformProjectsFile is missing. Cannot reliably check out platform projects." >&2
				echo "File $platformProjectsFile is missing. Cannot reliably check out platform projects." >> $cwd/$mvnBuildLog 2>&1
			else
				echo "" >&2
				echo "Checkout projects declared in $platformProjectsFile:" >&2
				echo "" >> $cwd/$mvnBuildLog 2>&1
				echo "Checkout projects declared in $platformProjectsFile:" >> $cwd/$mvnBuildLog 2>&1
				# read file
				while read line
				do
					lstProjUrls+=( "$line" )
				done < $platformProjectsFile
			fi
		fi

		# clone and check out the projects
		if [ "$lstProjUrls" != "" ]; then
			for lstUrl in "${lstProjUrls[@]}"
			do
				:
				lstProjDir=$(echo $lstUrl | awk -F/ '{print $2}')
				# try git@github.com:username/repo-name.git first
				if ! test -n "$lstProjDir"; then
					lstProjDir=$(echo $lstUrl | awk -F/ '{print $5}')
				fi
				if [ "$lstProjDir" = "" ]; then
					continue
				fi
				# try https://github.com/username/repo-name.git
				if ! test -n "$lstProjDir"; then
					if [ "$lstProjDir" != "" ]; then
						echo "*** Cannot check out, unable to parse directory: \[$lstUrl\]" >&2
						echo "Cannot check out, unable to parse directory: \[$lstUrl\]" >> $cwd/$mvnBuildLog 2>&1
					fi
					# loop
					continue
				fi

				lstProjDir="${lstProjDir%.git}"
				echo "  $lstProjDir: " >&2
				# echo $lstProjDir >&2
				cd $cwd
				# if the directory does not exist, clone the repos and run maven
				if [ ! -d "$lstProjDir" ]; then
					# log
					echo "" >> $cwd/$mvnBuildLog 2>&1
					echo "  $lstProjDir:" >> $cwd/$mvnBuildLog 2>&1
					#console
					printf "    › git clone » " >&2
					# clone the repo specified in URL
					git clone $lstUrl $cwd/$lstProjDir >> $cwd/$mvnBuildLog 2>&1
					gitStatus="$?"
					if [ "$gitStatus" -ne "0" ]; then
						echo "✶✶✶ FAILURE, 'git clone' exited with $gitStatus, see $mvnBuildLog for details" >&2
					else
						# checkout the repo
						cd $cwd/$lstProjDir >> $cwd/$mvnBuildLog 2>&1
						if [ "$?" -ne "0" ]; then
							echo "✶✶✶ Could not '\$ cd $cwd/$lstProjDir' ... did 'git clone' fail?" >&2
						else
							echo "[OK]" >&2
							printf "    › git checkout development » " >&2
							git checkout development >> $cwd/$mvnBuildLog 2>&1
							gitStatus="$?"
							if [ "$gitStatus" -ne "0" ]; then
								echo "✶✶✶ FAILURE, 'git checkout development' exited with $gitStatus, see $mvnBuildLog for details" >&2
							else
								echo "[OK]" >&2
								#              # build once to get the artifact generated
								#              maven_build $lstProjDir "install"
							fi
						fi
					fi
					#git pull
					#echo "\nBuilding the project $lstProjDir for $project\n"
					#mvn clean install -DskipTests=true
				else
					echo "    • skipping checkout, already exists" >&2
					echo "$lstProjDir: skipping checkout, already exists" >> $cwd/$mvnBuildLog 2>&1
				fi
			done
		fi
	fi
	cd $cwd
	echo "" >&2
}

## primary function to maven build all projects ##
## scope: private (internal calls only)         ##
function build_all() {
	# remove any previous maven build log
	rm -f $cwd/$mvnBuildLog
	# put environment in log file for troubleshooting
	printf '*%.0s' {1..100} >> $cwd/$mvnBuildLog 2>&1
	echo "" >> $cwd/$mvnBuildLog 2>&1
	echo "$mvnBuildLog : produced by $thisScript $args" >> $cwd/$mvnBuildLog 2>&1
	echo "" >> $cwd/$mvnBuildLog 2>&1
	printf '*%.0s' {1..100} >> $cwd/$mvnBuildLog 2>&1
	echo "" >> $cwd/$mvnBuildLog 2>&1
	echo "ENVIRONMENT:  \$ **/printenv⏎" >> $cwd/$mvnBuildLog 2>&1
	echo "" >> $cwd/$mvnBuildLog 2>&1
	printenv >> $cwd/$mvnBuildLog 2>&1

	# checkout core projects as declared in ascent-platform/build-projects.txt
	if [ ! $cmdSkipGitPull ]; then
		# log file project header
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		printf '*%.0s' {1..100} >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "New git checkouts as declared in $platformProjectsFile" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		# check out any repos declared in $platformProjectsFile that are new
		check_out_projects
	fi

	# prepare the list of directories to process
	make_dir_list

	# make sure sonar server is running
	if [ $cmdSonar ]; then
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "Starting SonarQube ... " >> $cwd/$mvnBuildLog 2>&1
		echo "" >&2
		printf "Starting SonarQube ... " >&2
		cd ~/git/ascent-platform/ascent-platform-docker-build/run-docker >> $cwd/$mvnBuildLog 2>&1
		./run-docker start sonarqube >> $cwd/$mvnBuildLog 2>&1
		cd $cwd >> $cwd/$mvnBuildLog 2>&1
		if [ "$?" -ne "0" ]; then
			echo "status $?"
			echo "  ✶✶✶ Could not start SonarQube. Please see output logs." >&2
			exit_now
		else
			echo "[OK]" >&2
			echo "  Running containers ..." >&2
			docker ps -a | grep sonar >&2
			echo "" >&2
			echo "  Running containers ..." >> $cwd/$mvnBuildLog 2>&1
			docker ps -a | grep sonar >> $cwd/$mvnBuildLog 2>&1
			echo "" >> $cwd/$mvnBuildLog 2>&1
		fi
	fi

	# loop through the directory list and perform pulls and builds
	echo "" >&2
	echo "Perform build actions on all projects in $cwd:" >&2
	for project in "${dirProjects[@]}"
	do
		:
		if [ -z "$project" ]; then
			echo "*** WARN directory \"$project\" does not exist." >&2
			echo "*** WARN directory \"$project\" does not exist." >> $cwd/$mvnBuildLog 2>&1
			continue
		fi

		# log file project header
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		echo "" >> $cwd/$mvnBuildLog 2>&1
		printf '*%.0s' {1..100} >> $cwd/$mvnBuildLog 2>&1

		# change into the current project directory
		cd $cwd/$project >> $cwd/$mvnBuildLog 2>&1
		if [ "$?" -ne "0" ]; then
			echo "*** WARN Could not '\$ cd $cwd/$project' ... trying next directory" >&2
			echo "*** WARN Could not '\$ cd $cwd/$project' ... trying next directory" >> $cwd/$mvnBuildLog 2>&1
		else
			echo "  $project: " >&2
			if [ ! $cmdSkipGitPull ]; then
				# perform git pull on the current project
				git_pull $project
			fi
			# build the project
			maven_build # $project "mvn clean install [deploy etc]"
			run_sonar # $project "mvn sonar:sonar"
			run_fortify # $project "antrun:@fortify-scan"
		fi
	done
}

## run this script                             ##
## parameters: optional command-line arguments ##
## scope: public                               ##

# get args from the command line
get_args $args
# tell user
echo "Project actions (start with -h for help):" >&2
printf "    • $cmdSkipGitPullMsg\n" >&2
printf "    • $cmdSkipMvnBuildMsg\n" >&2
printf "    • $cmdForceUpdateMsg\n" >&2
printf "    • $cmdSkipInstallMsg\n" >&2
printf "    • $cmdUnitTestsMsg\n" >&2
printf "    • $cmdDockerCleanMsg\n" >&2
printf "    • $cmdDockerBuildMsg\n" >&2
# printf "    • $cmdNexusDeployMsg\n" >&2
printf "    • $cmdSonarMsg\n" >&2
printf "    • $cmdFortifyScanMsg\n" >&2
echo "" >&2

#echo "Press Ctrl+C to cancel, or Enter key to continue ... "
#read

docker_clean # $project docker rmi [ascent related images]

# execute
build_all
# end in same directory we started in
cd $cwd
# final message
echo "" >&2
echo "To view output logs:  \$ less $mvnBuildLog" >&2
echo "REMEMBER to refresh your eclipse workspace!" >&2
echo ""
