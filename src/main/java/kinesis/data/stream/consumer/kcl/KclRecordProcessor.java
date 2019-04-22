package kinesis.data.stream.consumer.kcl;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.model.Record;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

public class KclRecordProcessor implements IRecordProcessor {

    // Backoff and retry settings
    private static final long BACKOFF_TIME_IN_MILLIS = 3000L; // 3 seconds
    private static final int NUM_RETRIES = 3; //Try three times before discard the record

    private String kinesisShardId;

    @Override
    public void initialize(InitializationInput initializationInput) {
        this.kinesisShardId = initializationInput.getShardId();
    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        List<Record> records = processRecordsInput.getRecords();

        Instant startTime = Instant.now();

        processRecords(records);

        Instant endTime = Instant.now();
        System.out.println("Processed: " + records.size() + " records in " + Duration.between(startTime, endTime) + " seconds.");

        //For each record block
        checkpoint(processRecordsInput.getCheckpointer());
    }

    @Override
    public void shutdown(ShutdownInput shutdownInput) {
        System.out.println("Shutting down record processor for shard: " + this.kinesisShardId);

        //Prevent from failures
        if (shutdownInput.getShutdownReason() == ShutdownReason.TERMINATE) {
            checkpoint(shutdownInput.getCheckpointer());
        }
    }

    private void processRecords(List<Record> records) {

        Iterator<Record> recordIterator = records.iterator();

        while (recordIterator.hasNext()) {

            Record record = recordIterator.next();
            Boolean processedSuccessfully = Boolean.FALSE;

            for (int i = 0; i < NUM_RETRIES; i++) {
                try {
                    System.out.println("Process record: " + new String(record.getData().array()));
                    processedSuccessfully = Boolean.TRUE;
                    break;
                } catch (Throwable t) {
                    System.out.println(" Caught throwable while processing record " + record + " " + t.getMessage());
                }

                try {// backoff if encounter an exception.
                    Thread.sleep(BACKOFF_TIME_IN_MILLIS);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted sleep. " + e.getMessage());
                }
            }

            if (!processedSuccessfully) {
                System.out.println("Couldn't process record " + record + ". Skipping the record.");
            }
        }

    }

    /** Checkpoint with retries.
     * @param checkpointer
     */
    private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
        System.out.println(" Checkpointing shard " + this.kinesisShardId);
        for (int i = 0; i < NUM_RETRIES; i++) {
            try {
                checkpointer.checkpoint();
                break;
            } catch (ShutdownException se) {
                // Ignore checkpoint if the processor instance has been shutdown (fail over).
                System.out.println("Caught shutdown exception, skipping checkpoint. " + se.getMessage());
                break;
            } catch (ThrottlingException e) {
                // Backoff and re-attempt checkpoint upon transient failures
                if (i >= (NUM_RETRIES - 1)) {
                    System.out.println("Checkpoint failed after " + (i + 1) + "attempts. " + e.getMessage());
                    break;
                } else {
                    System.out.println("Transient issue when checkpointing - attempt " + (i + 1) + " of "+ NUM_RETRIES + " " + e.getMessage());
                }
            } catch (InvalidStateException e) {
                // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
                System.out.println("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library. " + e.getMessage());
                break;
            }

            try {
                Thread.sleep(BACKOFF_TIME_IN_MILLIS);
            } catch (InterruptedException e) {
                System.out.println(" Interrupted sleep " + e.getMessage());
            }
        }
    }

}
