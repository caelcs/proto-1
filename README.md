# proto-1

Prototype to test high availability between two apps using kafka.
Also to see how to scale up when there is high concurrency of requests.

To accomplish this there are two application, producer and consumer, producer will create
messages by batches and send it to kafka and on the other end the consumer app will start dequeuing
messages. The project nft will execute stress tests and have scenarios to scale up the consumers
and see what is the effect on the metrics.

Both apps are going to expose basic metrics to monitor the resources.


## How to use it

###install all platform by executing

```./install.sh```

this will install kafka, zookeeper, 3 consumers, 1 producer, Grafana and Prometheus

Take the IP address shown at the end of the installation process and use it to access all the apps

eg.

**grafana**: http://192.168.1.100:3000
**Prometheus**: http://192.168.1.100:9090

Each consumer is using a pool thread to consume in parallel 200 messages at the time and because I am
adding a random delay between 500ms and 10000ms you might see some spikes but nothing out of the ordinary.

In grafana there are custom metrics that you can check like

Producer: 
- **batch_batches_total**: is a counter of batches that has been processed.

Consumer:
- **consumer_number_messages_total**: Total number of messages that has been consumed.

###Kill all the pods
execute 

```./killAll.sh```
 
## How to start playing around
To start the process of messages you need to execute a endpoint in the producer app and declaring how many messages will the batch contains.

POST http://192.168.1.100:8080/messages/1000

After that it will start the first batch of 1000 messages and after that finished it will generate another one.
so it is endless process of executing batches.
