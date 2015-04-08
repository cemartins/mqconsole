# Installing jar dependencies for communication with WebsphereMQ

You need to install these third party dependencies to be able to build the mqc-websphere project. Below is a list of the needed
dependencies and the maven command you can execute to install them on your local repository:

## ms0b

`mvn install:install-file -Dfile=mqjars/ms0b-6.1.jar -DgroupId=com.ibm.mq.pcf -DartifactId=ms0b -Dversion=6.1 -Dpackaging=jar`


## com.ibm.mq

`mvn install:install-file -Dfile=mqjars/com.ibm.mq-7.0.1.9.jar -DgroupId=com.ibm -DartifactId=com.ibm.mq -Dversion=7.0.1.9 -Dpackaging=jar`


## connector

`mvn install:install-file -Dfile=mqjars/connector-7.0.1.9.jar -DgroupId=com.ibm.mq -DartifactId=connector -Dversion=7.0.1.9 -Dpackaging=jar`


## commonservices

`mvn install:install-file -Dfile=mqjars/commonservices-7.0.1.9.jar -DgroupId=com.ibm.mq -DartifactId=commonservices -Dversion=7.0.1.9 -Dpackaging=jar`


## com.ibm.mq.jmqi

`mvn install:install-file -Dfile=mqjars/com.ibm.mq.jmqi-7.0.1.9.jar -DgroupId=com.ibm -DartifactId=com.ibm.mq.jmqi -Dversion=7.0.1.9 -Dpackaging=jar`


## mq

`mvn install:install-file -Dfile=mqjars/mq-7.0.1.9.jar -DgroupId=com.ibm.mq -DartifactId=mq -Dversion=7.0.1.9 -Dpackaging=jar`


## com.ibm.mq.headers

`mvn install:install-file -Dfile=mqjars/com.ibm.mq.headers-7.0.1.9.jar -DgroupId=com.ibm.mq -DartifactId=com.ibm.mq.headers -Dversion=7.0.1.9 -Dpackaging=jar`


## com.ibm.mq.pcf

`mvn install:install-file -Dfile=mqjars/com.ibm.mq.pcf-7.0.1.9.jar -DgroupId=com.ibm.mq -DartifactId=com.ibm.mq.pcf -Dversion=7.0.1.9 -Dpackaging=jar`