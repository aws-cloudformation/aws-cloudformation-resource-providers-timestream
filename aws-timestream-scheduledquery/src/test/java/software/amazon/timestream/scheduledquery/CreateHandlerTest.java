package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamquery.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamquery.model.Endpoint;
import com.amazonaws.services.timestreamquery.model.CreateScheduledQueryResult;
import com.amazonaws.services.timestreamquery.model.CreateScheduledQueryRequest;
import com.amazonaws.services.timestreamquery.model.ConflictException;
import com.amazonaws.services.timestreamquery.model.ValidationException;
import com.amazonaws.services.timestreamquery.model.InvalidEndpointException;
import com.amazonaws.services.timestreamquery.model.AccessDeniedException;
import com.amazonaws.services.timestreamquery.model.ThrottlingException;
import com.amazonaws.services.timestreamquery.model.InternalServerException;
import com.amazonaws.services.timestreamquery.model.ServiceQuotaExceededException;

import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
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
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final String TEST_ARN = "TestArn";
    private static final String TEST_NAME = "TestName";
    private static final String TEST_QUERY_STRING = "TestQueryString";
    private static final ScheduleConfiguration TEST_SCHEDULE_CONFIGURATION = ScheduleConfiguration.builder()
            .scheduleExpression("TestScheduleExpression")
            .build();
    private static final NotificationConfiguration TEST_NOTIFICATION_CONFIGURATION = NotificationConfiguration.builder()
            .snsConfiguration(SnsConfiguration.builder()
                    .topicArn("TestTopicArn")
                    .build())
            .build();
    private static final String TEST_CLIENT_TOKEN = "TestClientToken";
    private static final String TEST_SCHEDULED_QUERY_EXECUTION_ROLE_ARN = "TestScheduledQueryExecutionRoleArn";
    private static final TargetConfiguration TEST_TARGET_CONFIGURATION = TargetConfiguration.builder()
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
    private static final ErrorReportConfiguration TEST_ERROR_REPORT_CONFIGURATION = ErrorReportConfiguration.builder()
            .s3Configuration(S3Configuration.builder()
                    .bucketName("TestBucketName")
                    .objectKeyPrefix("TestObjectKeyPrefix")
                    .encryptionOption("SSE_S3")
                    .build())
            .build();
    private static final String TEST_KMS_KEY_ID = "TestKmsKeyId";
    private static final List<Tag> TEST_TAGS = Arrays.asList(
            Tag.builder()
                    .key("TestKey1")
                    .value("TestValue1")
                    .build(),
            Tag.builder()
                    .key("TestKey2")
                    .value("TestValue2")
                    .build());

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private final CreateHandler handler = new CreateHandler();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        doReturn(new DescribeEndpointsResult().withEndpoints(new Endpoint().withAddress("endpoint")))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeEndpointsRequest.class), any(Function.class));
        logger = mock(Logger.class);
    }

    @Test
    public void createScheduledQueryTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(new CreateScheduledQueryResult().withArn(TEST_ARN))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

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

        verify(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));
    }

    @Test
    public void createScheduledQueryWithoutClientTokenTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequestWithoutClientToken();

        doReturn(new CreateScheduledQueryResult().withArn(TEST_ARN))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

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

        verify(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));
    }

    @Test
    public void createScheduledQueryWithoutTargetConfigurationTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequestWithoutTargetConfiguration();

        doReturn(new CreateScheduledQueryResult().withArn(TEST_ARN))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

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

        verify(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));
    }

    @Test
    public void createScheduledQueryWithoutKmsKeyIdTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequestWithoutKmsKeyId();

        doReturn(new CreateScheduledQueryResult().withArn(TEST_ARN))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

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

        verify(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));
    }

    @Test
    public void createScheduledQueryThrowWhenResourceExistsTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ConflictException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnAlreadyExistsException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createScheduledQueryThrowWhenInvalidRequestTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createScheduledQueryThrowWhenInvalidEndpointTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createScheduledQueryThrowWhenAccessDeniedTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createScheduledQueryThrowWhenServiceQuotaExceededTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ServiceQuotaExceededException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnServiceLimitExceededException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createScheduledQueryThrowWhenThrottlingTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createScheduledQueryThrowWhenInternalServerFailureTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder()
                        .scheduledQueryName(TEST_NAME)
                        .queryString(TEST_QUERY_STRING)
                        .scheduleConfiguration(TEST_SCHEDULE_CONFIGURATION)
                        .notificationConfiguration(TEST_NOTIFICATION_CONFIGURATION)
                        .clientToken(TEST_CLIENT_TOKEN)
                        .scheduledQueryExecutionRoleArn(TEST_SCHEDULED_QUERY_EXECUTION_ROLE_ARN)
                        .targetConfiguration(TEST_TARGET_CONFIGURATION)
                        .errorReportConfiguration(TEST_ERROR_REPORT_CONFIGURATION)
                        .kmsKeyId(TEST_KMS_KEY_ID)
                        .tags(TEST_TAGS)
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithoutClientToken() {
        final ResourceModel model =
                ResourceModel.builder()
                        .scheduledQueryName(TEST_NAME)
                        .queryString(TEST_QUERY_STRING)
                        .scheduleConfiguration(TEST_SCHEDULE_CONFIGURATION)
                        .notificationConfiguration(TEST_NOTIFICATION_CONFIGURATION)
                        .scheduledQueryExecutionRoleArn(TEST_SCHEDULED_QUERY_EXECUTION_ROLE_ARN)
                        .targetConfiguration(TEST_TARGET_CONFIGURATION)
                        .errorReportConfiguration(TEST_ERROR_REPORT_CONFIGURATION)
                        .kmsKeyId(TEST_KMS_KEY_ID)
                        .tags(TEST_TAGS)
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithoutTargetConfiguration() {
        final ResourceModel model =
                ResourceModel.builder()
                        .scheduledQueryName(TEST_NAME)
                        .queryString(TEST_QUERY_STRING)
                        .scheduleConfiguration(TEST_SCHEDULE_CONFIGURATION)
                        .notificationConfiguration(TEST_NOTIFICATION_CONFIGURATION)
                        .clientToken(TEST_CLIENT_TOKEN)
                        .scheduledQueryExecutionRoleArn(TEST_SCHEDULED_QUERY_EXECUTION_ROLE_ARN)
                        .errorReportConfiguration(TEST_ERROR_REPORT_CONFIGURATION)
                        .kmsKeyId(TEST_KMS_KEY_ID)
                        .tags(TEST_TAGS)
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithoutKmsKeyId() {
        final ResourceModel model =
                ResourceModel.builder()
                        .scheduledQueryName(TEST_NAME)
                        .queryString(TEST_QUERY_STRING)
                        .scheduleConfiguration(TEST_SCHEDULE_CONFIGURATION)
                        .notificationConfiguration(TEST_NOTIFICATION_CONFIGURATION)
                        .clientToken(TEST_CLIENT_TOKEN)
                        .scheduledQueryExecutionRoleArn(TEST_SCHEDULED_QUERY_EXECUTION_ROLE_ARN)
                        .targetConfiguration(TEST_TARGET_CONFIGURATION)
                        .errorReportConfiguration(TEST_ERROR_REPORT_CONFIGURATION)
                        .tags(TEST_TAGS)
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }
}
