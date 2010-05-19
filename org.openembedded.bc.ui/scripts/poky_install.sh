#!/bin/bash
# Installation script for Poky Pinky development branch
# Adapted from instructions available at http://wiki.openembedded.net/index.php/Getting_started
# This script can be executable as shell script after substituting {| |} variables with real values.
# 4/3/2009 Ken Gilmer

# These are the variables that are queried in the UI.  The following lines are parsed by the install wizard.
# {|D|Install Directory|R|${HOME}/oe||}
# {|T|Init Script|R|poky-init-build-env||}

# System Check
which git
which svn
which python

# Directory Setup
[ -d ${Install Directory} ] || mkdir -p ${Install Directory}  
cd ${Install Directory}

# Installing from Poky pinky branch
svn export -r HEAD http://svn.o-hand.com/repos/poky/branches/pinky/
mv pinky/* .
rm -Rf pinky

