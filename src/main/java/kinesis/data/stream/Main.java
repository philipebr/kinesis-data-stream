package kinesis.data.stream;

import kinesis.data.stream.consumer.kcl.ConsumerKcl;
import kinesis.data.stream.consumer.sdk.ConsumerSdk;
import kinesis.data.stream.producer.kpl.ProducerKpl;
import kinesis.data.stream.producer.sdk.ProducerSdk;

import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String args[]) throws ExecutionException, InterruptedException {
        AwsConfiguration awsConfiguration = new AwsConfiguration("", "", "");

        ProducerSdk producerSdk = new ProducerSdk(awsConfiguration);
        producerSdk.sendDataToStream("");

        ProducerKpl producerKpl = new ProducerKpl(awsConfiguration);
        producerKpl.sendDataAsync(null, "", "");
        producerKpl.sendDataSync(null ,"", "");
        producerKpl.sendDataBarebones(null, "", "");

        ConsumerSdk consumerSdk = new ConsumerSdk(awsConfiguration);
        consumerSdk.getDataFromStream("");

        ConsumerKcl consumerKcl = new ConsumerKcl(awsConfiguration, "", "");
        consumerKcl.run();

    }

}
