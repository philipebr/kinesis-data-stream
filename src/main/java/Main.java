public class Main {

    public static void main(String args[]) {
        AwsConfiguration awsConfiguration = new AwsConfiguration("", "", "");
        ProducerSdk producerSdk = new ProducerSdk(awsConfiguration);
        producerSdk.sendDataToStream("");
    }

}
