package kinesis.data.stream.consumer.kcl;

import kinesis.data.stream.AwsConfiguration;

public class ConsumerKcl {

    private KclWorker kclWorker;
    private Thread thread;
    public ConsumerKcl(AwsConfiguration awsConfiguration, String appName, String streamName) {
        this.kclWorker = new KclWorker(awsConfiguration, appName, streamName);
    }

    public void run() {
        Runnable workerRunnable = () ->{
            kclWorker.createWorker();
            kclWorker.run();
        };
        this.thread = new Thread(workerRunnable);
        this.thread.start();
    }

    public void stop() {
        this.kclWorker.stop();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.thread.interrupt();
    }
}
