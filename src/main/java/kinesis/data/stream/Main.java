package kinesis.data.stream;

import kinesis.data.stream.consumer.kcl.ConsumerKcl;
import kinesis.data.stream.consumer.sdk.ConsumerSdk;
import kinesis.data.stream.producer.kpl.ProducerKpl;
import kinesis.data.stream.producer.sdk.ProducerSdk;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String args[]) throws ExecutionException, InterruptedException {

        AwsConfiguration awsConfiguration = new AwsConfiguration("", "", "");

        ProducerSdk producerSdk = new ProducerSdk(awsConfiguration);
        producerSdk.sendDataToStream("WORKSHOP_SDK");

        ProducerKpl producerKpl = new ProducerKpl(awsConfiguration);
        producerKpl.sendDataAsync(Arrays.asList("DATA ASYNC"), "WORKSHOP_LIB", "WORKSHOP_LIB");
        producerKpl.sendDataSync(Arrays.asList("DATA SYNC") ,"WORKSHOP_LIB", "WORKSHOP_LIB");
        producerKpl.sendDataBarebones(Arrays.asList("DATA BAREBONE"), "WORKSHOP_LIB", "WORKSHOP_LIB");

        ConsumerSdk consumerSdk = new ConsumerSdk(awsConfiguration);
        consumerSdk.getDataFromStream("WORKSHOP_SDK");

        ConsumerKcl consumerKcl = new ConsumerKcl(awsConfiguration, "WORKSHOP_LIB", "WORKSHOP_LIB");
        consumerKcl.run();

    }

}
