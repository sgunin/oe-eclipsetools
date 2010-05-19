#!/bin/bash
# Installation script for OpenEmbedded dev branch.
# Adapted from instructions available at http://wiki.openembedded.net/index.php/Getting_started
# This script should be executable after substituting ${} variables with real values.
# 4/3/2009 Ken Gilmer

# These are the variables that are queried in the UI.  The following lines are parsed by the install wizard.
# {|D|Install Directory|R|${HOME}/oe||}
# {|D|Build Directory|R|${HOME}/oe_build||}
# {|T|Repository URL|R|git://git.openembedded.net/openembedded||}
# {|T|Init Script|R|init.sh||}
# {|T|Distribution|R|angstrom-2008.1||}
# {|T|Machine|R|om-gta01||}
# {|T|Package Cache Directory|R|${HOME}/sources||}

# System Check
which git
which svn
which python

# Directory Setup
[ -d ${Install Directory} ] || mkdir -p ${Install Directory} 
cd ${Install Directory}
mkdir -p build/conf
[ -d ${Build Directory} ] || mkdir -p ${Build Directory}

# Bitbake Setup
wget http://download.berlios.de/bitbake/bitbake-1.8.18.tar.gz
tar xfzv bitbake-1.8.18.tar.gz
mv bitbake-1.8.18 bitbake
rm bitbake-1.8.18.tar.gz


# OpenEmbedded Setup
git clone ${Repository URL}
echo "BBFILES = \"${Install Directory}/openembedded/recipes/*/*.bb\"" > build/conf/local.conf
echo "DISTRO = \"${Distribution}\"" >> build/conf/local.conf
echo "MACHINE = \"${Machine}\"" >> build/conf/local.conf
echo "DL_DIR = \"${Package Cache Directory}\"" >> build/conf/local.conf
echo "TMPDIR = \"${Build Directory}\"" >> build/conf/local.conf

# Environment Setup Script
echo "export BBPATH=${Install Directory}/build:${Install Directory}/openembedded" > ${Init Script}
echo "export PATH=${Install Directory}/bitbake/bin:$PATH" >> ${Init Script}
chmod u+x ${Init Script}
