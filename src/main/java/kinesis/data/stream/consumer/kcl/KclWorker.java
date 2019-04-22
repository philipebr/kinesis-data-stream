package kinesis.data.stream.consumer.kcl;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import kinesis.data.stream.AwsConfiguration;

public class KclWorker {

    private AwsConfiguration awsConfiguration;
    private KinesisClientLibConfiguration kinesisClientLibConfiguration;
    private Worker worker;

    public KclWorker(AwsConfiguration awsConfiguration, String appName, String streamName) {
        this.awsConfiguration = awsConfiguration;
        this.kinesisClientLibConfiguration = this.awsConfiguration.getKinesisConsumer(appName, streamName);
    }

    public void createWorker() {
        IRecordProcessorFactory recordProcessorFactory = new KclRecordProcessorFactory();
        this.worker = new Worker.Builder().recordProcessorFactory(recordProcessorFactory)
                .config(this.kinesisClientLibConfiguration)
                .build();

        System.out.println(String.format("Creating %s as worker %s to process stream %s\n",
                this.kinesisClientLibConfiguration.getApplicationName(),
                this.kinesisClientLibConfiguration.getWorkerIdentifier(),
                this.kinesisClientLibConfiguration.getStreamName()));

    }

    public void run() {
        System.out.println(String.format("Running %s...\n", this.kinesisClientLibConfiguration.getApplicationName()));
        try {
            this.worker.run();
        } catch (Throwable t) {
            System.out.println("Caught throwable while processing data. " + t.getMessage());
        }
        System.exit(0);
    }

    public void stop() {
        this.worker.shutdown();
    }

}
