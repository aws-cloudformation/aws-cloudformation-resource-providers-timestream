package software.amazon.timestream.table;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.timestreamwrite.AmazonTimestreamWrite;
import com.amazonaws.services.timestreamwrite.AmazonTimestreamWriteClient;
import com.amazonaws.services.timestreamwrite.AmazonTimestreamWriteClientBuilder;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsResult;

/**
 * Factory class that provides Timestream client instance.
 */
class TimestreamClientFactory {

    private static final String DEFAULT_AWS_REGION = "us-east-1";

    /**
     *
     *  @return  Timestream write client with corresponding endpoint discovered.
     */
    static AmazonTimestreamWrite get(final AmazonWebServicesClientProxy proxy, final Logger logger) {
        String region = System.getenv("AWS_REGION");
        if (region == null) {
            region = DEFAULT_AWS_REGION;
        }
        logger.log("Creating AmazonTimestreamWriteClient in region " + region + "\n");

        /*
         * 1. Create a client and describe the endpoints. The credentials from users will be used and
         *   injected to the request. Note here the credentials from users should be used for discovering endpoints
         *   (instead of the credentials of the lambda handlers)
         */
        final DescribeEndpointsRequest discoveryRequest = new DescribeEndpointsRequest();
        final AmazonTimestreamWrite timestreamClient = buildWriteClientWithDisco(region);
        final DescribeEndpointsResult result =
                proxy.injectCredentialsAndInvoke(discoveryRequest, timestreamClient::describeEndpoints);

        final String endpoint = result.getEndpoints().get(0).getAddress();
        logger.log("Creating AmazonTimestreamWriteClient with endpoint " + endpoint + "\n");

        /*
         * 2. Create the actual client to use with the endpoint obtained
         */
        return AmazonTimestreamWriteClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build();
    }

    private static AmazonTimestreamWrite buildWriteClientWithDisco(final String region) {
        final AmazonTimestreamWriteClientBuilder builder
                = AmazonTimestreamWriteClientBuilder.standard().withRegion(region);

        final ClientConfiguration config = new ClientConfiguration();
        config.setProtocol(Protocol.HTTPS);
        builder.withClientConfiguration(config);
        return builder.build();
    }
}
