# We select the base image from. Locally available or from https://hub.docker.com/
FROM openjdk:8-jre-alpine

# We define the user we will use in this instance to prevent using root that even in a container, can be a security risk.
ENV APPLICATION_USER caeldev
ENV KAFKA_BROKER localhost:9092
ENV CONSUMER_TOPIC test-topic
ENV ACK_CONSUMER_CLIENT_ID ack-consumerT-test1
ENV ACK_CONSUMER_TOPIC ack-consumer-topic

# Then we add the user, create the /app folder and give permissions to our user.
RUN adduser -D -g '' $APPLICATION_USER
RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

# Marks this container to use the specified $APPLICATION_USER
USER $APPLICATION_USER

# We copy the FAT Jar we built into the /app folder and sets that folder as the working directory.
COPY ./build/libs/consumer.jar /app/consumer.jar
WORKDIR /app

# We launch java to execute the jar, with good defauls intended for containers.
CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "consumer.jar"]
