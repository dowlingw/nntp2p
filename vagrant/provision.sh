#!/bin/bash

PACKAGES="
software-properties-common
"

PPAS="
ppa:jcfp/ppa
ppa:webupd8team/java
"

PPA_PACKAGES="
sabnzbdplus
oracle-java8-installer
oracle-java8-set-default
"

# Pre-accept the java license
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections

# Sab Options
cat > /etc/default/sabnzbdplus << EOL
USER=vagrant
HOST=0.0.0.0
PORT=
EXTRAOPTS=
EOL

#------------------------------------------------
# No need to edit anything from here on in

# Define some useful functions
function apt_update { apt-get update -qq ;}
function apt_install { apt-get -o Dpkg::Options::="--force-confold" install -y -qq $1 ;}
function add_ppa { add-apt-repository -y $1 ;}

# First round of package installations
apt_update
apt_install $PACKAGES

# Let's add a usenet client
for ppa in $PPAS; do
    add_ppa $ppa
done
apt_update
apt_install $PPA_PACKAGES