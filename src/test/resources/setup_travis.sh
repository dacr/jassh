#!/bin/sh 

# Create test user
sudo useradd -p `perl -e "print(crypt('testtest', 'AB'));"` test

# Install ssh
sudo apt-get update -qq
sudo apt-get install -qq libssh2-1-dev openssh-client openssh-server

# Generate and Register keys
ssh-keygen -t rsa -f ~/.ssh/id_rsa -N "" -q
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
ssh-keyscan -t rsa localhost >> ~/.ssh/known_hosts
sudo cp src/test/resources/sshconfig ~/.ssh/config
sudo chmod 644 ~/.ssh/config

sudo mkdir -p ~/../test/.ssh
sudo cp ~/.ssh/id_rsa.pub  ~/../test/.ssh/authorized_keys
sudo cp ~/.ssh/known_hosts ~/../test/.ssh/
sudo chown -R test:test ~/../test
sudo chmod 644 ~/../test/.ssh/*
sudo chmod 755 ~/../test/.ssh

sudo restart ssh
