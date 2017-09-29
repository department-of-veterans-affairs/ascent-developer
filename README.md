# ascent-developer-vm
Repository that contains the vagrant & shell scripts for building a linux VM with the ascent-platform technology stack

### Preface
This document walks through the steps involved in rolling out a developers workstation installed with all the core technology stack needed for platform development, on a VM box with ubuntu/trusty64 image.

### Prerequisites
Users should have the below softwares installed on their machine
  * Vagrant (1.9.6 - the latest version 1.9.7 has issues)
  * Oracle VM VirtualBox(latest version)
  * Git
  * GitHub account and read only access to **ascent-ext-configs** repository. Other repositories are publicly available
  * Personal Access Token to connect to GitHub: [Creating Personal Access Token](#creating-personal-access-token-to-connect-to-github)

### Software installed at the end of vagrant run
  * Apache Maven 3.3.9
  * JDK 1.8
  * GIT
  * STS 3.8.4
  * Docker 17.03.1
  * Docker-Compose 1.11.2

### To Use

* To check out ascent-developer-vm repository from GitHub
  * git clone https://github.com/department-of-veterans-affairs/ascent-developer-vm
  * Make sure to change the line endings in cloneascentrepo.sh,cloneascentsamplerepo.sh and githubuserconfiguration.sh to **LF** from **CRLF**.(Linux interprets CR as an unknown character & you will see issues when running the cloneascentrepo.sh scripts) - THIS STEP MAY LIKELY NOT BE NEEDED.
* Run the command **vagrant up**. This should take a few minutes to get all the software installed on the VM box.
* Creating a vagrant image of VM (this will take few minutes): **vagrant package --base platform-base --output <specific directory>/platform-base.box**
* run the command: **vagrant up** or go to Oracle VM Virtual Box Manager and double click on pltaform-base VM

* Default credentials to login to VM box:vagrant/vagrant
* Upon logging into VM, open the terminal and run command **sudo passwd root** to set your root password
* Run the command **sudo su root** and **cd** to root directory
* In case you have issue running command "mvn -version" then set up your path to run maven: Append /usr/local/apache-maven/apache-maven-3.3.9/bin to PATH in /etc/environment

* To validate the installation: 
  * Run: java -version to confirm version 1.8.0_131
  * Run: git --version to confirm git installation
  * Run: /opt/sts-bundle/sts-3.8.4.RELEASE/STS to bring up STS IDE
  * Run: docker & docker-compose to see the Usage
  * Confirm maven installation under /usr/local/apache-maven/apache-maven-3.3.9

* Incase you don't see ascent-platform and ascent-sample repos cloned under /home/vagrant/projects/workspace, run the following scripts cloneascentrepo.sh, cloneascentsamplerepo.sh and githubuserconfiguration.sh under /home/vagrant 

* Creating Personal Access Token To Connect To GitHub

    Creating Personal Access Token is required for 2 purposes

    1. Token required for Read Only access to GitHub private repo "ascent-ext-configs", connect via Ascent Config 
      service
    2. To perform GIT operations using HTTPS for any private repositories

    Follow the steps on page [Create Personal Access Token](https://github.com/department-of-veterans-affairs/ascent-platform/wiki/Ascent-Quick-Start-Guide#creating-personal-access-token-to-connect-to-github)

* Caching the personal access token in Linux 

   From the terminal run the below command 

      git config --global credential.helper cache
     Caches for 15 mins by default

      git config --global credential.helper 'cache --timeout=3600'
     Caches for 60 mins

## Build and Test

Follow all the instructions on this page: [Build and Test Projects](https://github.com/department-of-veterans-affairs/ascent-sample/wiki/Ascent-Sample-Quick-Start-Guide#build-and-test)

**The below steps don't have to be followed anymore** because we are using https connection to github. Follow these steps 
## ONLY IF YOU WANT TO USE GIT COMMAND TO CLONE REPOSITORIES using SSH Connection

  * Generating a new SSH key & adding the SSH key to the ssh-agent
    * From the terminal, run the following script: **sh /home/vagrant/setupsshkey-github.sh**
    * Press Enter when you are prompted to "Enter a file in which to save the key". 
       The file would be saved under <root_home>/.ssh/id_rsa
    * Enter a passphrase & re-enter the same again(Make sure you remember the passphrase)
    * Enter the passphrase if prompted
  * Adding the generated SSH key to your GitHub Account
    * From the terminal, run the command to copy the SSH public key to the clipboard: "*clip < ~/.ssh/id_rsa.pub*"
      (If the clip command does not work, cat the file and copy the content to clipboard)
    * Login into your GitHub account and click on the settings from the upper right corner
    * Click "*SSH and GPG keys*"
    * Click "*New SSH key or Add SSH key*"
    * In the Title field add a descriptive label for the key. For example "Developer's workstation ssh key"
    * Paste your key into the "Key" field
    * Click Add SSH key
    * If prompted, confirm your GitHub password.
