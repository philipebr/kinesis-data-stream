package kinesis.data.stream.consumer.sdk;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.*;
import kinesis.data.stream.AwsConfiguration;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConsumerSdk {

    private AwsConfiguration awsConfiguration;

    public ConsumerSdk(AwsConfiguration awsConfiguration) {
        this.awsConfiguration = awsConfiguration;
    }

    public void getDataFromStream(String streamName) {

        AmazonKinesis amazonKinesis = this.awsConfiguration.getKinesis();

        Date date = new Date();
        date.setMinutes(date.getMinutes()-10);

        String shardId = getStartShartdId(streamName, amazonKinesis);

        GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
        getShardIteratorRequest.setStreamName(streamName);
        getShardIteratorRequest.setShardId(shardId);
        getShardIteratorRequest.setShardIteratorType(ShardIteratorType.AT_TIMESTAMP);

        getShardIteratorRequest.setTimestamp(date);
        GetShardIteratorResult getShardIteratorResult = amazonKinesis.getShardIterator(getShardIteratorRequest);
        String shardIterator= getShardIteratorResult.getShardIterator();

        GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
        getRecordsRequest.setShardIterator(shardIterator);
        getRecordsRequest.setLimit(300);

        GetRecordsResult getRecordsResult = amazonKinesis.getRecords(getRecordsRequest);
        List<Record> returnedRecords = getRecordsResult.getRecords();

        ByteBuffer byteBuffer = returnedRecords.get(returnedRecords.size()-1).getData();
        System.out.println("Consumer SDK: " + new String(byteBuffer.array()));
    }

    private String getStartShartdId(String streamName, AmazonKinesis amazonKinesis) {
        DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
        describeStreamRequest.setStreamName( streamName );
        List<Shard> shards = new ArrayList<>();
        DescribeStreamResult describeStreamResult = amazonKinesis.describeStream( describeStreamRequest );
        shards.addAll( describeStreamResult.getStreamDescription().getShards() );
        return shards.get(0).getShardId();
    }

}
