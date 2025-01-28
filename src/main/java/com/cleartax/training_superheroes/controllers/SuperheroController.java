package com.cleartax.training_superheroes.controllers;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.cleartax.training_superheroes.config.SqsClientConfig;
import com.cleartax.training_superheroes.config.SqsConfig;
import com.cleartax.training_superheroes.dto.Superhero;
import com.cleartax.training_superheroes.dto.SuperheroRequestBody;
import com.cleartax.training_superheroes.services.SuperheroConsumer;
import com.cleartax.training_superheroes.services.SuperheroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@RestController
public class SuperheroController {

    private SuperheroService superheroService;

    @Autowired
    private SqsConfig sqsconfig;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private SuperheroConsumer superheroConsumer;

    @Autowired
    private AmazonSQS amazonSQS;

    @Autowired
    public SuperheroController(SuperheroService superheroService, AmazonSQS amazonSQS) {
        this.superheroService = superheroService;
        this.amazonSQS = amazonSQS;
    }


    @GetMapping("/hello")
    public String hello(@RequestParam(value = "username", defaultValue = "World") String superHeroName) {
        // Send the message to LocalStack
        amazonSQS.sendMessage(new com.amazonaws.services.sqs.model.SendMessageRequest()
                .withQueueUrl("http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/superhero-queue") // Your LocalStack queue URL
                .withMessageBody(superHeroName)); // Message body


        return String.format("The superHeroName  %s!", superHeroName);
    }

    @GetMapping("/update_superhero_async")
    public String updateSuperhero(@RequestParam(value = "superHeroName", defaultValue = "ironMan") String superHeroName) {
        AmazonSQS amazonSQS = AmazonSQSClient.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "ap-south-1"))  // LocalStack endpoint
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("accessKey", "secretKey")))  // LocalStack credentials
                .build();

        // Create SendMessageRequest using SDK v1 syntax
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl("http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/superhero-queue")  // Your LocalStack queue URL
                .withMessageBody(superHeroName);  // Message body is the superhero name

        // Send the message to the queue
        SendMessageResult result = amazonSQS.sendMessage(sendMessageRequest);

        System.out.println("Message Sent Response:");
        System.out.println("Message ID: " + result.getMessageId());
        System.out.println("MD5 of Message Body: " + result.getMD5OfMessageBody());
        System.out.println("superHeroName: " + superHeroName);


        return String.format("Message sent to queue with message id %s and superHero %s", result.getMessageId(), superHeroName);
    }
    /*
      for(int i = 0;i<100;i++){
            ReceiveMessageResponse res = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(sqsconfig.getQueueUrl()).build());
            res.messages().forEach(message -> System.out.println("message " + message.body()));

        }
    */

    @GetMapping("/superhero")
    public Superhero getSuperhero(@RequestParam(value = "name", defaultValue = "Batman") String name,
                                  @RequestParam(value = "universe", defaultValue = "DC") String universe){
        System.out.println("Fetching superhero with name: " + name + ", universe: " + universe);
        if (name == null && universe == null) {
            throw new IllegalArgumentException("At least one of 'name' or 'universe' must be provided");
        }

        return superheroService.getSuperhero(name, universe);
    }
    @GetMapping("/get_message_from_queue")
    public String getMessage() {
        return superheroConsumer.consumeSuperhero();
    }
    @PostMapping("/superhero")
    public Superhero persistSuperhero(@RequestBody SuperheroRequestBody superherorequestBody){
        System.out.println("Superhero " + superherorequestBody.getName() + " added in " + superherorequestBody.getUniverse() + " universe");
        return superheroService.persistSuperhero(superherorequestBody);
    }

    @DeleteMapping("/superhero")
    public String deleteSuperhero(@RequestParam(value = "name") String name,
                                  @RequestParam(value = "universe") String universe) {
        System.out.println("Deleting superhero: " + name + " from " + universe);
        boolean isDeleted = superheroService.deleteSuperhero(name, universe);

        if (isDeleted) {
            return String.format("Superhero %s from %s has been deleted successfully.", name, universe);
        } else {
            return String.format("Superhero %s from %s not found.", name, universe);
        }
    }
    @PutMapping("/superhero")
    public Superhero updateSuperhero(@RequestParam(value = "name") String name,
                                     @RequestParam(value = "universe") String universe,
                                     @RequestBody SuperheroRequestBody updatedDetails) {
        System.out.println("Updating superhero: " + name + " in universe: " + universe);
        return superheroService.updateSuperhero(name, universe, updatedDetails);
    }


}
