MQConsole - JavaFX Console for HornetQ, Apache ActiveMQ
=======================================================

requirements
Java 1.8.0_40+

Before running, edit the broker.properties file and fill in the details of your message broker:


	broker_hostname = <ip address or hostname where the broker is running>
	broker_port = <port where the broker listens to (default=1414)>
	broker_channel = <channel name>
	broker_queue_manager = <queue manager name>
	broker_timeout = <timeout in milliseconds when waiting for a messsage response> 


On Mac OSX the broker.properties file is located inside the application package (right click and select "show package contents"), 
in the folder Contents/Java.

On Windows the broker.properties file is located in the app folder.

Binary Downloads
================

Please find Windows and OS X binary distributions at bintray with the following link

[ ![Download](https://api.bintray.com/packages/cemartins/mqconsole/MQConsole/images/download.svg) ](https://bintray.com/cemartins/mqconsole/MQConsole/_latestVersion)


Building
========

The project is configured as a maven project

Deployment to bintray at https://bintray.com/cemartins/mqconsole/MQConsole/view

build requirements
JDK 1.8.0_40+
Maven 3.1+

Common maven goals:

* run the application as a HornetQ client: mvn -f mqc-hornetq/pom.xml exec:java
* run the application as a ActiveMQ client: mvn -f mqc-activemq/pom.xml exec:java

* make an application package as a HornetQ client: mvn -f mqc-hornetq/pom.xml package
* make an application package as a ActiveMQ client: mvn -f mqc-activemq/pom.xml package

Main class is net.sf.juffrou.mq.MQConsole
