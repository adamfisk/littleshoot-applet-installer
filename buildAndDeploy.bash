#!/usr/bin/env bash

function die() {
  echo $*
  exit 1
}

mvn clean package -Dmaven.test.skip=true || die "Could not package"
cp target/*with-dependencies.jar littleShootInstaller.jar || die "Could not copy jar"
jarsigner -storetype pkcs12 -keystore /Users/afisk/secure/bns_keystore.p12 -tsa http://tsa.starfieldtech.com/ littleShootInstaller.jar "bns" || die "Could not sign jar"
tar czvf install.tgz install.html littleShootInstaller.jnlp littleShootInstaller.jar 
scp -i ~/.ec2/id_rsa-gsg-keypair install.tgz root@dev.littleshoot.org:/root 
