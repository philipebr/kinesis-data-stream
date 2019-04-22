package kinesis.data.stream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;

import java.util.UUID;

public class AwsConfiguration {

    private String accessKey;
    private String accessSecret;
    private Region region;

    public AwsConfiguration(String accessKey, String accessSecret, String regionName) {
        this.accessKey = accessKey;
        this.accessSecret = accessSecret;
        this.region = RegionUtils.getRegion(regionName);
    }

    public AWSCredentialsProvider getCredentialProvider() {
        final AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(this.accessKey, this.accessSecret));
        return awsCredentialsProvider;
    }

    public KinesisProducer getKinesisProducer() {
        KinesisProducerConfiguration configuration = new KinesisProducerConfiguration()
                .setCredentialsProvider(getCredentialProvider())
                .setKinesisEndpoint(this.region.getServiceEndpoint(AmazonKinesis.ENDPOINT_PREFIX))
                .setRegion(region.getName());
        return new KinesisProducer(configuration);
    }

    public KinesisClientLibConfiguration getKinesisConsumer(String appName, String streamName) {
        String workerId = UUID.randomUUID().toString();

        KinesisClientLibConfiguration kinesisClientLibConfiguration = new KinesisClientLibConfiguration(appName, streamName,
                getCredentialProvider(), workerId);

        kinesisClientLibConfiguration.withRegionName(this.region.getName());
        kinesisClientLibConfiguration.withKinesisEndpoint(this.region.getServiceEndpoint(AmazonKinesis.ENDPOINT_PREFIX));
        kinesisClientLibConfiguration.withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON);

        return kinesisClientLibConfiguration;
    }

    public AmazonKinesis getKinesis() {
        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard()
                .withRegion(this.region.getName())
                .withCredentials(getCredentialProvider());
        return clientBuilder.build();
    }

}
