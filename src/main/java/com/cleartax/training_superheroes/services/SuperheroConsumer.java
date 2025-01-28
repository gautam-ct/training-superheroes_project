package com.cleartax.training_superheroes.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.cleartax.training_superheroes.config.SqsConfig;
import com.cleartax.training_superheroes.dto.Superhero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SuperheroConsumer {

    @Autowired
    private SqsConfig sqsConfig;

    @Autowired
    private AmazonSQS amazonSQS;

    @Autowired
    private SuperheroService superheroService;

    @Scheduled(fixedRate = 10000) // Automatically runs every 10 seconds
    public void scheduledConsumeSuperhero() {
        // Logic remains the same as the earlier implementation
        consumeSuperhero();
    }

    public void consumeSuperhero() {
        // Same logic as before for receiving, processing, and deleting messages
        try {
            ReceiveMessageResult receiveResult = amazonSQS.receiveMessage(new ReceiveMessageRequest()
                    .withQueueUrl("http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/superhero-queue")
                    .withMaxNumberOfMessages(10)
                    .withWaitTimeSeconds(10));

            if (receiveResult.getMessages().isEmpty()) {
                System.out.println("No messages available in the queue.");
                return;
            }

            receiveResult.getMessages().forEach(message -> {
                try {
                    String messageBody = message.getBody();
                    System.out.println("Received message: " + messageBody);

                    Superhero superhero = superheroService.getSuperhero(messageBody, null);
                    if (superhero != null) {
                        System.out.println("Superhero found in the database: " + superhero.getName());
                    } else {
                        System.out.println("Superhero not found in the database: " + messageBody);
                    }

                    amazonSQS.deleteMessage(new DeleteMessageRequest()
                            .withQueueUrl("http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/superhero-queue")
                            .withReceiptHandle(message.getReceiptHandle()));

                    System.out.println("Deleted message from the queue: " + messageBody);
                } catch (Exception e) {
                    System.err.println("Error processing message: " + message.getBody());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Error consuming messages from the queue.");
            e.printStackTrace();
        }
    }
}
