# proto-1

Prototype to test inter connectivity and high availability between two apps using kafka.
Also to see how to scale up when there is high concurrency of requests.

To accomplish this there are two application, producer and consumer, producer will create
messages and send it to kafka and on the other end the consumer app will start dequeuing
messages. The project nft will execute stress tests and have scenarios to scale up the consumers
and see what is the effect on the metrics.

Both apps are going to expose basic metrics to monitor the resources.


## How to use it

install all platform by executing

```./install.sh```

this will install kafka, zookeeper, 3 consumers and 1 producer