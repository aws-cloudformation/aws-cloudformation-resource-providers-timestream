package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.model.ScheduleConfiguration;
import com.amazonaws.services.timestreamquery.model.SnsConfiguration;
import com.amazonaws.services.timestreamquery.model.NotificationConfiguration;
import com.amazonaws.services.timestreamquery.model.TargetConfiguration;
import com.amazonaws.services.timestreamquery.model.TimestreamConfiguration;
import com.amazonaws.services.timestreamquery.model.DimensionMapping;
import com.amazonaws.services.timestreamquery.model.ErrorReportConfiguration;
import com.amazonaws.services.timestreamquery.model.S3Configuration;

import com.amazonaws.services.timestreamquery.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamquery.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamquery.model.Endpoint;
import com.amazonaws.services.timestreamquery.model.DescribeScheduledQueryResult;
import com.amazonaws.services.timestreamquery.model.DescribeScheduledQueryRequest;
import com.amazonaws.services.timestreamquery.model.ScheduledQueryDescription;
import com.amazonaws.services.timestreamquery.model.ListTagsForResourceResult;
import com.amazonaws.services.timestreamquery.model.ListTagsForResourceRequest;
import com.amazonaws.services.timestreamquery.model.Tag;

import com.amazonaws.services.timestreamquery.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamquery.model.ValidationException;
import com.amazonaws.services.timestreamquery.model.InvalidEndpointException;
import com.amazonaws.services.timestreamquery.model.AccessDeniedException;
import com.amazonaws.services.timestreamquery.model.ThrottlingException;
import com.amazonaws.services.timestreamquery.model.InternalServerException;

import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private static final String TEST_ARN = "TestArn";
    private static final String TEST_NAME = "TestName";
    private static final String TEST_QUERY_STRING = "TestQueryString";
    private static final ScheduleConfiguration TEST_SCHEDULE_CONFIGURATION = new ScheduleConfiguration()
            .withScheduleExpression("TestScheduleExpression");
    private static final NotificationConfiguration TEST_NOTIFICATION_CONFIGURATION = new NotificationConfiguration()
            .withSnsConfiguration(new SnsConfiguration()
                    .withTopicArn("TestTopicArn"));
    private static final String TEST_SCHEDULED_QUERY_EXECUTION_ROLE_ARN = "TestScheduledQueryExecutionRoleArn";
    private static final TargetConfiguration TEST_TARGET_CONFIGURATION = new TargetConfiguration()
            .withTimestreamConfiguration(new TimestreamConfiguration()
                    .withDatabaseName("TestDatabaseName")
                    .withTableName("TestTableName")
                    .withTimeColumn("TestTimeColumn")
                    .withDimensionMappings(Arrays.asList(
                            new DimensionMapping()
                                    .withName("TestName")
                                    .withDimensionValueType("TestDimensionValueType"),
                            new DimensionMapping()
                                    .withName("TestName")
                                    .withDimensionValueType("TestDimensionValueType"))));
    private static final ErrorReportConfiguration TEST_ERROR_REPORT_CONFIGURATION = new ErrorReportConfiguration()
            .withS3Configuration(new S3Configuration()
                    .withBucketName("TestBucketName")
                    .withObjectKeyPrefix("TestObjectKeyPrefix")
                    .withEncryptionOption("SSE_S3"));
    private static final String TEST_KMS_KEY_ID = "TestKmsKeyId";
    private static final List<software.amazon.timestream.scheduledquery.Tag> TEST_TAGS = Arrays.asList(
            software.amazon.timestream.scheduledquery.Tag.builder()
                    .key("TestKey1")
                    .value("TestValue1")
                    .build(),
            software.amazon.timestream.scheduledquery.Tag.builder()
                    .key("TestKey2")
                    .value("TestValue2")
                    .build());
    private static final String TEST_TAG_KEY = "TestKey";
    private static final String TEST_TAG_VALUE = "TestValue";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private final ReadHandler handler = new ReadHandler();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        doReturn(new DescribeEndpointsResult().withEndpoints(new Endpoint().withAddress("endpoint")))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeEndpointsRequest.class), any(Function.class));
        logger = mock(Logger.class);
    }

    @Test
    public void readScheduledQueryTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        final DescribeScheduledQueryResult describeScheduledQueryResult = new DescribeScheduledQueryResult()
                .withScheduledQuery(new ScheduledQueryDescription()
                        .withArn(TEST_ARN)
                        .withName(TEST_NAME)
                        .withQueryString(TEST_QUERY_STRING)
                        .withScheduleConfiguration(TEST_SCHEDULE_CONFIGURATION)
                        .withNotificationConfiguration(TEST_NOTIFICATION_CONFIGURATION)
                        .withScheduledQueryExecutionRoleArn(TEST_SCHEDULED_QUERY_EXECUTION_ROLE_ARN)
                        .withTargetConfiguration(TEST_TARGET_CONFIGURATION)
                        .withErrorReportConfiguration(TEST_ERROR_REPORT_CONFIGURATION)
                        .withKmsKeyId(TEST_KMS_KEY_ID));
        final ListTagsForResourceResult listTagsForResourceResult = givenAListTagsForResourceResultWithTags();

        doReturn(describeScheduledQueryResult).when(proxy)
                .injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));
        doReturn(listTagsForResourceResult).when(proxy)
                .injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class));

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class));
    }

    @Test
    public void readScheduledQueryWithoutTargetConfigurationTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        final DescribeScheduledQueryResult describeScheduledQueryResult = new DescribeScheduledQueryResult()
                .withScheduledQuery(new ScheduledQueryDescription()
                        .withArn(TEST_ARN)
                        .withName(TEST_NAME)
                        .withQueryString(TEST_QUERY_STRING)
                        .withScheduleConfiguration(TEST_SCHEDULE_CONFIGURATION)
                        .withNotificationConfiguration(TEST_NOTIFICATION_CONFIGURATION)
                        .withScheduledQueryExecutionRoleArn(TEST_SCHEDULED_QUERY_EXECUTION_ROLE_ARN)
                        .withErrorReportConfiguration(TEST_ERROR_REPORT_CONFIGURATION)
                        .withKmsKeyId(TEST_KMS_KEY_ID));
        final ListTagsForResourceResult listTagsForResourceResult = givenAListTagsForResourceResultWithTags();

        doReturn(describeScheduledQueryResult).when(proxy)
                .injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));
        doReturn(listTagsForResourceResult).when(proxy)
                .injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class));

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class));
    }

    @Test
    public void readScheduledQueryThrowWhenResourceNotFoundTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readScheduledQueryThrowWhenInvalidRequestTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readScheduledQueryThrowWhenInvalidEndpointTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readScheduledQueryThrowWhenAccessDeniedTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readScheduledQueryThrowWhenThrottlingTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readScheduledQueryThrowWhenInternalServerFailureTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder()
                        .arn(TEST_ARN)
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ListTagsForResourceResult givenAListTagsForResourceResultWithTags() {
        final ListTagsForResourceResult mockListTagsForResourceResult = mock(ListTagsForResourceResult.class);
        final List<Tag> tags =
                Collections.singletonList(
                        new Tag().withKey(TEST_TAG_KEY).withValue(TEST_TAG_VALUE));
        when(mockListTagsForResourceResult.getTags()).thenReturn(tags);
        return mockListTagsForResourceResult;
    }
}
