#### Main Window

![Main Window](images/main_window.png)

From the available queues window you can perform a number of actions. Right-click on a queue to see the menu.


#### List Messages

![Message List](images/list_messages.png)

This window displays the messages still in a queue. You can view the details of each message by right-clicking on one.


#### Message Detail

![Message List](images/view_message.png)

In the message detail window you can see the message headers as well as the message body in text form.


#### Send Message

![Message List](images/send_message.png)

The send message window allows you to send a message to a queue and collect the response message.


---


MQConsole is a small JavaFX2 utility application which allows you to interact
with a messaging broker (currently supports WebsphereMQ, ActiveMQ and HornetQ).

From the main window with the list of available queues you can:

- See the list the messages in a queue
- View the details of specific message
- Send a new message to a queue and wait for its response
- Listen to new messages arriving at a queue and display them automatically.


No need to have administrative credentials, just configure the hostname where the broker is, 
the channel and the name of the queue manager.

Please see the [project page](https://github.com/cemartins/mqconsole) at Github for source code and 
support and the [binary packages page](https://bintray.com/cemartins/mqconsole/MQConsole) for downloads.
