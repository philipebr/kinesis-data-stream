import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.amazonaws.services.kinesis.model.PutRecordsResultEntry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ProducerSdk {

    private AwsConfiguration awsConfiguration;

    public ProducerSdk(AwsConfiguration awsConfiguration) {
        this.awsConfiguration = awsConfiguration;
    }

    private AmazonKinesis getAmazonKinesis() {
        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard()
                .withRegion(this.awsConfiguration.getRegion().getName())
                .withCredentials(this.awsConfiguration.getCredentialProvider());
        return clientBuilder.build();
    }

    public void sendDataToStream(String streamName) {
        AmazonKinesis amazonKinesis = getAmazonKinesis();
        PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();
        putRecordsRequest.setStreamName(streamName);
        List<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            PutRecordsRequestEntry putRecordsRequestEntry  = new PutRecordsRequestEntry();
            putRecordsRequestEntry.setData(ByteBuffer.wrap(String.valueOf(i).getBytes()));
            putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", i));
            putRecordsRequestEntryList.add(putRecordsRequestEntry);
        }
        putRecordsRequest.setRecords(putRecordsRequestEntryList);
        PutRecordsResult putRecordsResult  = amazonKinesis.putRecords(putRecordsRequest);
        System.out.println("Put Result" + putRecordsResult);
        handleFailure(putRecordsResult, putRecordsRequestEntryList, streamName, amazonKinesis);
    }

    private void handleFailure(PutRecordsResult putRecordsResult, List<PutRecordsRequestEntry> putRecordsRequestEntryList, String streamName, AmazonKinesis amazonKinesis) {
        PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();
        putRecordsRequest.setStreamName(streamName);
        while (putRecordsResult.getFailedRecordCount() > 0) {
            final List<PutRecordsRequestEntry> failedRecordsList = new ArrayList<>();
            final List<PutRecordsResultEntry> putRecordsResultEntryList = putRecordsResult.getRecords();
            for (int i = 0; i < putRecordsResultEntryList.size(); i++) {
                final PutRecordsRequestEntry putRecordRequestEntry = putRecordsRequestEntryList.get(i);
                final PutRecordsResultEntry putRecordsResultEntry = putRecordsResultEntryList.get(i);
                if (putRecordsResultEntry.getErrorCode() != null) {
                    failedRecordsList.add(putRecordRequestEntry);
                }
            }
            putRecordsRequestEntryList = failedRecordsList;
            putRecordsRequest.setRecords(putRecordsRequestEntryList);
            putRecordsResult = amazonKinesis.putRecords(putRecordsRequest);
        }
    }

}
