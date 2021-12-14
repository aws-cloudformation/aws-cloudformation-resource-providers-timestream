package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamquery.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamquery.model.Endpoint;
import com.amazonaws.services.timestreamquery.model.UntagResourceRequest;
import com.amazonaws.services.timestreamquery.model.TagResourceRequest;
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
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private static final String TEST_SQ_ARN = "TestArn";
    private static final String TEST_SQ_NAME = "TestName";
    private static final String TEST_SQ_QUERY_STRING = "TestQueryString";
    private static final ScheduleConfiguration TEST_SQ_SCHEDULE_CONFIGURATION = ScheduleConfiguration.builder()
            .scheduleExpression("TestScheduleExpression")
            .build();
    private static final NotificationConfiguration TEST_SQ_NOTIFICATION_CONFIGURATION = NotificationConfiguration.builder()
            .snsConfiguration(SnsConfiguration.builder()
                    .topicArn("TestTopicArn")
                    .build())
            .build();
    private static final String TEST_SQ_CLIENT_TOKEN = "TestClientToken";
    private static final String TEST_SQ_SCHEDULED_QUERY_EXECUTION_ROLE_ARN = "TestScheduledQueryExecutionRoleArn";
    private static final TargetConfiguration TEST_SQ_TARGET_CONFIGURATION = TargetConfiguration.builder()
            .timestreamConfiguration(TimestreamConfiguration.builder()
                    .databaseName("TestDatabaseName")
                    .tableName("TestTableName")
                    .timeColumn("TestTimeColumn")
                    .dimensionMappings(Arrays.asList(
                            DimensionMapping.builder()
                                    .name("TestName")
                                    .dimensionValueType("TestDimensionValueType")
                                    .build(),
                            DimensionMapping.builder()
                                    .name("TestName")
                                    .dimensionValueType("TestDimensionValueType")
                                    .build()))
                    .build())
            .build();
    private static final ErrorReportConfiguration TEST_SQ_ERROR_REPORT_CONFIGURATION = ErrorReportConfiguration.builder()
            .s3Configuration(S3Configuration.builder()
                    .bucketName("TestBucketName")
                    .objectKeyPrefix("TestObjectKeyPrefix")
                    .encryptionOption("SSE_S3")
                    .build())
            .build();
    private static final String TEST_SQ_KMS_KEY_ID = "TestKmsKeyId";
    private static final String TEST_TAG_KEY_1 = "TestTagKey1";
    private static final String TEST_TAG_KEY_2 = "TestTagKey2";
    private static final String TEST_TAG_KEY_3 = "TestTagKey3";
    private static final String TEST_TAG_VALUE_1 = "TestTagValue1";
    private static final String TEST_TAG_VALUE_2 = "TestTagValue2";
    private static final String TEST_TAG_VALUE_2_NEW = "TestTagValue2New";
    private static final String TEST_TAG_VALUE_3 = "TestTagValue3";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private final UpdateHandler handler = new UpdateHandler();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        doReturn(new DescribeEndpointsResult().withEndpoints(new Endpoint().withAddress("endpoint")))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeEndpointsRequest.class), any(Function.class));
        logger = mock(Logger.class);
    }

    @Test
    public void updateScheduledQueriesTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null).when(proxy).injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class));
        doReturn(null).when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));

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

        verify(proxy).injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));
        verifyNoMoreInteractions(proxy);
    }

    @Test
    public void updateScheduledQueryThrowWhenTagAndResourceNotFoundTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));
        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenTagAndInvalidRequestTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));
        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenTagAndInvalidEndpointTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));
        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenTagAndAccessDeniedTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));
        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenTagAndThrottlingTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));
        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenTagAndInternalServerFailureTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));
        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(TagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenUntagAndResourceNotFoundTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenUntagAndInvalidRequestTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenUntagAndInvalidEndpointTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenUntagAndAccessDeniedTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenUntagAndThrottlingTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateScheduledQueryThrowWhenUntagAndInternalServerFailureTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UntagResourceRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder()
                        .arn(TEST_SQ_ARN)
                        .scheduledQueryName(TEST_SQ_NAME)
                        .queryString(TEST_SQ_QUERY_STRING)
                        .scheduleConfiguration(TEST_SQ_SCHEDULE_CONFIGURATION)
                        .notificationConfiguration(TEST_SQ_NOTIFICATION_CONFIGURATION)
                        .clientToken(TEST_SQ_CLIENT_TOKEN)
                        .scheduledQueryExecutionRoleArn(TEST_SQ_SCHEDULED_QUERY_EXECUTION_ROLE_ARN)
                        .targetConfiguration(TEST_SQ_TARGET_CONFIGURATION)
                        .errorReportConfiguration(TEST_SQ_ERROR_REPORT_CONFIGURATION)
                        .kmsKeyId(TEST_SQ_KMS_KEY_ID)
                        .tags(Arrays.asList(
                                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2_NEW).build(),
                                Tag.builder().key(TEST_TAG_KEY_3).value(TEST_TAG_VALUE_3).build()))
                        .build();

        final ResourceModel existingModel =
                ResourceModel.builder()
                        .arn(TEST_SQ_ARN)
                        .scheduledQueryName(TEST_SQ_NAME)
                        .queryString(TEST_SQ_QUERY_STRING)
                        .scheduleConfiguration(TEST_SQ_SCHEDULE_CONFIGURATION)
                        .notificationConfiguration(TEST_SQ_NOTIFICATION_CONFIGURATION)
                        .clientToken(TEST_SQ_CLIENT_TOKEN)
                        .scheduledQueryExecutionRoleArn(TEST_SQ_SCHEDULED_QUERY_EXECUTION_ROLE_ARN)
                        .targetConfiguration(TEST_SQ_TARGET_CONFIGURATION)
                        .errorReportConfiguration(TEST_SQ_ERROR_REPORT_CONFIGURATION)
                        .kmsKeyId(TEST_SQ_KMS_KEY_ID)
                        .tags(Arrays.asList(
                                Tag.builder().key(TEST_TAG_KEY_1).value(TEST_TAG_VALUE_1).build(),
                                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2).build()))
                        .build();

        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model).previousResourceState(existingModel)
                .build();
    }
}
