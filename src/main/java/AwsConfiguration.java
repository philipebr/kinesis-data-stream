import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

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

    public Region getRegion() {
        return this.region;
    }

}
