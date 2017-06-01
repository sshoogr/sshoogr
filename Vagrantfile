
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "aestasit/devops-ubuntu-14.04"
  config.vm.network "private_network", ip: "192.168.33.144"
  config.vm.provider "virtualbox" do |vb|
    vb.name = "sshoogr-integration-server"
    vb.customize ["modifyvm", :id, "--memory", "2048"]
    vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
  end
  config.vm.provision "fix-no-tty", type: "shell" do |s|
    s.privileged = false
    s.inline = "sudo sed -i '/tty/!s/mesg n/tty -s \\&\\& mesg n/' /root/.profile"
  end
  config.vm.provision "shell", inline: "apt-get update"
  config.vm.provision "shell", inline: "apt-get install -y openjdk-6-jdk"
  config.vm.provision "shell", inline: "apt-get install -y openjdk-7-jdk"
  config.vm.provision "shell", inline: "apt-get install -y curl"
  config.vm.provision "shell", inline: "apt-get install -y zip"
  config.vm.provision "shell", privileged: false, inline: "curl -s http://get.sdkman.io | bash"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.4.10"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.4.6"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.3.9"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.2.2"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.1.9"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.0.8"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 1.8.9"
end
                                                                                    