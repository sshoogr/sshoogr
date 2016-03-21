
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "hashicorp/precise64"
  config.vm.network "private_network", ip: "192.168.33.144"
  config.vm.provision "fix-no-tty", type: "shell" do |s|
    s.privileged = false
    s.inline = "sudo sed -i '/tty/!s/mesg n/tty -s \\&\\& mesg n/' /root/.profile"
  end
  config.vm.provision "shell", inline: "apt-get update"
  config.vm.provision "shell", inline: "apt-get install -y software-properties-common python-software-properties"
  config.vm.provision "shell", inline: "add-apt-repository ppa:openjdk-r/ppa"
  config.vm.provision "shell", inline: "apt-get update"
  config.vm.provision "shell", inline: "apt-get install -y openjdk-6-jdk"
  config.vm.provision "shell", inline: "apt-get install -y openjdk-7-jdk"
  config.vm.provision "shell", inline: "apt-get install -y openjdk-8-jdk"
  config.vm.provision "shell", inline: "apt-get install -y curl"
  config.vm.provision "shell", inline: "apt-get install -y zip"
  config.vm.provision "shell", privileged: false, inline: "curl -s http://get.sdkman.io | bash"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.4.6"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.3.9"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.2.2"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.1.9"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 2.0.8"
  config.vm.provision "shell", privileged: false, inline: "source '/home/vagrant/.sdkman/bin/sdkman-init.sh' && yes | sdk install groovy 1.8.9"
end
                                                                                    