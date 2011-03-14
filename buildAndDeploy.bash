#!/usr/bin/env bash

function die() {
  echo $*
  exit 1
}

#zippedInstaller=$1
pass=$1
#cp $zippedInstaller src/main/resources
mvn clean package -Dmaven.test.skip=true || die "Could not package"
cp target/*with-dependencies.jar littleShootInstaller.jar || die "Could not copy jar"
echo "Signing jar: littleShootInstaller.jar"
jarsigner -storepass $pass -keypass $pass -storetype pkcs12 -keystore /Users/afisk/secure/bns_keystore.p12 -tsa http://tsa.starfieldtech.com/ littleShootInstaller.jar "bns" || die "Could not sign jar"
echo "Finished signing jar"
tar czvf install.tgz css js main.html install.html littleShootInstaller.jnlp littleShootInstaller.jar 
echo "Uploading installer package"
scp -i ~/.ec2/id_rsa-gsg-keypair install.tgz root@dev.littleshoot.org:/root
echo "Running remote bash..."
ssh -i ~/.ec2/id_rsa-gsg-keypair root@dev.littleshoot.org "./install.bash"
echo "Done!"
