package kinesis.data.stream.producer.kpl;

import com.amazonaws.services.kinesis.producer.Attempt;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import kinesis.data.stream.AwsConfiguration;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProducerKpl {

    private AwsConfiguration awsConfiguration;
    private KinesisProducer producer;

    public ProducerKpl(AwsConfiguration awsConfiguration) {
        this.awsConfiguration = awsConfiguration;
        this.producer = this.awsConfiguration.getKinesisProducer();
    }

    public void sendDataAsync(List<String> dataList, String streamName, String partitionKey) {
        dataList.forEach( item ->{
            ListenableFuture<UserRecordResult> future = this.producer.addUserRecord(streamName, partitionKey, ByteBuffer.wrap(item.getBytes()));
            Futures.addCallback(future, new FutureCallback<>() {
                @Override
                public void onSuccess(UserRecordResult userRecordResult) {
                    System.out.println("Send data to stream: " + streamName);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println("An error occurred to send data to stream: " + streamName);
                }
            });
        });

    }

    public void sendDataSync(List<String> dataList, String streamName, String partitionKey) throws ExecutionException, InterruptedException {
        List<Future<UserRecordResult>> putFutures = new LinkedList<>();
        dataList.forEach( item ->{
            this.producer.addUserRecord(streamName, partitionKey, ByteBuffer.wrap(item.getBytes()));
        });
        for (Future<UserRecordResult> f : putFutures) {
            UserRecordResult result = f.get(); // this does block
            if (result.isSuccessful()) {
                System.out.println("Put record into shard " + result.getShardId());
            } else {
                for (Attempt attempt : result.getAttempts()) {
                    // Analyze and respond to the failure
                    System.out.println("An error occurred to send data to stream: " + streamName);
                    System.out.println("Error code: " + attempt.getErrorCode());
                    System.out.println("Error message: " + attempt.getErrorMessage());
                }
            }
        }
    }

    public void sendDataBarebones(List<String> dataList, String streamName, String partitionKey) {
        dataList.forEach( item ->{
            this.producer.addUserRecord(streamName, partitionKey, ByteBuffer.wrap(item.getBytes()));
        });
    }

}
