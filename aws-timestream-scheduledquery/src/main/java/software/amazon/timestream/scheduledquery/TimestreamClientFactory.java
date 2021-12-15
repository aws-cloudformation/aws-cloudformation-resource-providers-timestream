package software.amazon.timestream.scheduledquery;

import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.timestreamquery.AmazonTimestreamQuery;
import com.amazonaws.services.timestreamquery.AmazonTimestreamQueryClient;
import com.amazonaws.services.timestreamquery.AmazonTimestreamQueryClientBuilder;
import com.amazonaws.services.timestreamquery.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamquery.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamquery.model.InternalServerException;
import com.amazonaws.services.timestreamquery.model.ValidationException;

import java.util.function.Function;

/**
 * Factory class that provides Timestream client instance.
 */
class TimestreamClientFactory {

    private static final String DEFAULT_AWS_REGION = "us-east-1";

    /**
     *
     *  @return  Timestream query client with corresponding endpoint discovered.
     */
    static AmazonTimestreamQuery get(final AmazonWebServicesClientProxy proxy, final Logger logger) {
        /*
         * From CFN team, it is recommended to use the production environment for all stages of resource handlers.
         *
         * Actually there is no way to distinguish prod and non-prod in resource lambda handlers during runtime
         * https://issues.amazon.com/issues/ULURU-1178. As done by most of the other teams, should simply use the
         * prod stage/region when creating client.
         *
         */
        String region = System.getenv("AWS_REGION");
        if (region == null) {
            region = DEFAULT_AWS_REGION;
        }
        logger.log("Creating AmazonTimestreamQueryClient in region " + region + "\n");

        /*
         * 1. Create a client and describe the endpoints. The credentials from users will be used and
         *   injected to the request. Note here the credentials from users should be used for discovering endpoints
         *   (instead of the credentials of the lambda handlers)
         */
        final DescribeEndpointsResult result;
        final DescribeEndpointsRequest discoveryRequest = new DescribeEndpointsRequest();
        try {
            final AmazonTimestreamQuery timestreamClient = buildQueryClientWithDisco(region);
            result = proxy.injectCredentialsAndInvoke(discoveryRequest, timestreamClient::describeEndpoints);
        } catch (final InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        } catch (final ValidationException ex) {
            throw new CfnInvalidRequestException(discoveryRequest.toString(), ex);
        }

        final String endpoint = result.getEndpoints().get(0).getAddress();
        logger.log("Creating AmazonTimestreamQueryClient with endpoint " + endpoint + "\n");

        /*
         * 2. Create the actual client to use with the endpoint obtained
         */
        return AmazonTimestreamQueryClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build();
    }

    /**
     * Refer to code https://tiny.amazon.com/15ejvwt7/codeamazpackPyrablobe13asrc
     */
    private static AmazonTimestreamQuery buildQueryClientWithDisco(final String region) {
        final AmazonTimestreamQueryClientBuilder builder
                = AmazonTimestreamQueryClientBuilder.standard().withRegion(region);

        final ClientConfiguration config = new ClientConfiguration();
        config.setProtocol(Protocol.HTTPS);
        builder.withClientConfiguration(config);
        return builder.build();
    }
}

