# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure("2") do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  config.vm.box = "ubuntu/trusty64"
  #config.ssh.username = 'root'
  #config.ssh.password = 'vagrant'
  #config.ssh.insert_key = 'true'  
  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # NOTE: This will enable public access to the opened port
  # config.vm.network "forwarded_port", guest: 80, host: 8080

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine and only allow access
  # via 127.0.0.1 to disable public access
  # config.vm.network "forwarded_port", guest: 80, host: 8080, host_ip: "127.0.0.1"

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
   config.vm.provider "virtualbox" do |vb|
  #   # Display the VirtualBox GUI when booting the machine
     vb.gui = true
	 vb.name = "platform-base"
  #
  #   # Customize the amount of memory on the VM:
     vb.memory = "8192"
   end
  #
  # View the documentation for the provider you are using for more
  # information on available options.

  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
   #config.vm.provision "shell", inline: <<-SHELL
  #config.vm.provision "apt-update", :type => "shell", :path => "apt-update.sh"
  config.vm.provision "jdk8-install", :type => "shell", :path => "jdk8-install.sh"
  config.vm.provision "maven339-install", :type => "shell", :path => "maven339-install.sh"
  #config.vm.provision "setmavenpath", :type => "shell", :path => "setmavenpath.sh"
  config.vm.provision "git-install", :type => "shell", :path => "git-install.sh"
  config.vm.provision "docker-install", :type => "shell", :path => "docker-install.sh"
  config.vm.provision "docker-compose-install", :type => "shell", :path => "docker-compose-install.sh"
  config.vm.provision "sts338-install", :type => "shell", :path => "sts338-install.sh"
#  config.vm.provision "file", source: "setupsshkey-github.sh", destination: "/home/vagrant/setupsshkey-github.sh"
  config.vm.provision "file", source: "githubuserconfiguration.sh", destination: "/home/vagrant/githubuserconfiguration.sh"
  config.vm.provision "file", source: "cloneascentrepo.sh", destination: "/home/vagrant/cloneascentrepo.sh"
  config.vm.provision "file", source: "cloneascentsamplerepo.sh", destination: "/home/vagrant/cloneascentsamplerepo.sh"
  config.vm.provision "vm-base", :type => "shell", :path => "vm-base.sh"  
  #config.vm.provision "xfce4-install", :type => "shell", :path => "xfce4-install.sh"  
  #config.vm.provision "start-xfce4", :type => "shell", :path => "start-xfce4.sh"
  #SHELL
end
