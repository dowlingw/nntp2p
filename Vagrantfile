VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.network "private_network", ip: "172.16.13.37"
  config.vm.network "forwarded_port", guest: 8080, host: 8080

  config.vm.provision "shell", path: "vagrant/provision.sh"
end
