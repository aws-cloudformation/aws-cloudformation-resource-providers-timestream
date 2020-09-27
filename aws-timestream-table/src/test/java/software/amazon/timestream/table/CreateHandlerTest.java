package software.amazon.timestream.table;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.services.timestreamwrite.model.AccessDeniedException;
import com.amazonaws.services.timestreamwrite.model.ConflictException;
import com.amazonaws.services.timestreamwrite.model.CreateTableRequest;
import com.amazonaws.services.timestreamwrite.model.CreateTableResult;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamwrite.model.Endpoint;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.ServiceQuotaExceededException;
import com.amazonaws.services.timestreamwrite.model.Table;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final String TEST_DATABASE_NAME = "TestDatabaseName";
    private static final String TEST_TABLE_NAME = "TestTableName";
    private static final String TEST_TAG_KEY_1 = "TestTagKey1";
    private static final String TEST_TAG_KEY_2 = "TestTagKey2";
    private static final String TEST_TAG_KEY_3 = "TestTagKey3";
    private static final String TEST_TAG_VALUE_1 = "TestTagValue1";
    private static final String TEST_TAG_VALUE_2 = "TestTagValue2";
    private static final String TEST_TAG_VALUE_3 = "TestTagValue3";
    private static final RetentionProperties TEST_RETENTION_PROPERTIES = RetentionProperties.builder()
            .memoryStoreRetentionPeriodInHours("12")
            .magneticStoreRetentionPeriodInDays("7")
            .build();
    private static final String TEST_CLIENT_REQUEST_TOKEN = "00000000-0000-0000-0000-000000000000";
    private static final String TEST_RESOURCE_ID = "MyResource";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private final CreateHandler handler = new CreateHandler();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        doReturn(new DescribeEndpointsResult().withEndpoints(new Endpoint().withAddress("endpoint")))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeEndpointsRequest.class), any());
        logger = mock(Logger.class);
    }

    @Test
    public void createTableShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(new CreateTableResult().withTable(
                new Table().withDatabaseName(TEST_DATABASE_NAME)
                        .withTableName(TEST_TABLE_NAME)
                        .withRetentionProperties(
                                RetentionPropertiesModelConverter.convert(TEST_RETENTION_PROPERTIES))))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

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

        final CreateTableRequest expectedCreateTableRequest =
                new CreateTableRequest()
                        .withDatabaseName(TEST_DATABASE_NAME)
                        .withTableName(TEST_TABLE_NAME)
                        .withRetentionProperties(new com.amazonaws.services.timestreamwrite.model.RetentionProperties()
                                .withMagneticStoreRetentionPeriodInDays(7L)
                                .withMemoryStoreRetentionPeriodInHours(12L));
        verify(proxy).injectCredentialsAndInvoke(eq(expectedCreateTableRequest), any());
    }

    @Test
    public void createTableWithTagsShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequestWithTags();

        doReturn(new CreateTableResult().withTable(
                new Table().withDatabaseName(TEST_DATABASE_NAME)
                        .withTableName(TEST_TABLE_NAME)
                        .withRetentionProperties(
                                RetentionPropertiesModelConverter.convert(TEST_RETENTION_PROPERTIES))))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

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

        final CreateTableRequest expectedCreateTableRequest =
                new CreateTableRequest()
                        .withDatabaseName(TEST_DATABASE_NAME)
                        .withTableName(TEST_TABLE_NAME)
                        .withRetentionProperties(new com.amazonaws.services.timestreamwrite.model.RetentionProperties()
                                .withMagneticStoreRetentionPeriodInDays(7L)
                                .withMemoryStoreRetentionPeriodInHours(12L))
                        .withTags(
                                new com.amazonaws.services.timestreamwrite.model.Tag()
                                        .withKey(TEST_TAG_KEY_1).withValue(TEST_TAG_VALUE_1),
                                new com.amazonaws.services.timestreamwrite.model.Tag()
                                        .withKey(TEST_TAG_KEY_2).withValue(TEST_TAG_VALUE_2),
                                new com.amazonaws.services.timestreamwrite.model.Tag()
                                        .withKey(TEST_TAG_KEY_3).withValue(TEST_TAG_VALUE_3));
        verify(proxy).injectCredentialsAndInvoke(eq(expectedCreateTableRequest), any());
    }

    @Test
    public void createTableShouldSucceedWhenRetentionPropertiesNotProvided() {
        final ResourceHandlerRequest<ResourceModel> request =
                givenAResourceHandlerRequestWithoutRetentionProperties();

        doReturn(new CreateTableResult().withTable(
                new Table().withDatabaseName(TEST_DATABASE_NAME)
                        .withTableName(TEST_TABLE_NAME)
                        .withRetentionProperties(
                                RetentionPropertiesModelConverter.convert(TEST_RETENTION_PROPERTIES))))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

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

        final CreateTableRequest expectedCreateTableRequest =
                new CreateTableRequest()
                        .withDatabaseName(TEST_DATABASE_NAME)
                        .withTableName(TEST_TABLE_NAME);
        verify(proxy).injectCredentialsAndInvoke(eq(expectedCreateTableRequest), any());
    }

    @Test
    public void createTableShouldSucceedWhenTableNameNotProvided() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequestWithoutTableName();

        doReturn(new CreateTableResult().withTable(
                new Table().withDatabaseName(TEST_DATABASE_NAME)
                        .withTableName(TEST_TABLE_NAME)
                        .withRetentionProperties(
                                RetentionPropertiesModelConverter.convert(TEST_RETENTION_PROPERTIES))))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

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

        final CreateTableRequest expectedCreateTableRequest =
                new CreateTableRequest()
                        .withDatabaseName(TEST_DATABASE_NAME)
                        // table name is generated by CloudFormation when not provided
                        .withTableName(request.getDesiredResourceState().getTableName())
                        .withRetentionProperties(new com.amazonaws.services.timestreamwrite.model.RetentionProperties()
                                .withMagneticStoreRetentionPeriodInDays(7L)
                                .withMemoryStoreRetentionPeriodInHours(12L));
        verify(proxy).injectCredentialsAndInvoke(eq(expectedCreateTableRequest), any());
    }

    /*
     * Tests for error handling.
     */
    @Test
    public void createTableShouldThrowWhenResourceExists() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ConflictException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

        assertThrows(
                CfnAlreadyExistsException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createTableShouldThrowWhenResourceNotFound() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createTableShouldThrowWhenInvalidRequest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createTableShouldThrowWhenMissingPermissions() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createTableShouldThrowWhenTableLimitExceeded() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ServiceQuotaExceededException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

        assertThrows(
                CfnServiceLimitExceededException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createTableShouldThrowWhenThrottled() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createTableShouldThrowWhenGenericException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateTableRequest.class), any());

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .tableName(TEST_TABLE_NAME)
                        .retentionProperties(TEST_RETENTION_PROPERTIES)
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithTags() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .tableName(TEST_TABLE_NAME)
                        .retentionProperties(TEST_RETENTION_PROPERTIES)
                        .tags(Arrays.asList(
                                Tag.builder().key(TEST_TAG_KEY_1).value(TEST_TAG_VALUE_1).build(),
                                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2).build(),
                                Tag.builder().key(TEST_TAG_KEY_3).value(TEST_TAG_VALUE_3).build()))
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithoutTableName() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .retentionProperties(TEST_RETENTION_PROPERTIES)
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(TEST_CLIENT_REQUEST_TOKEN)
                .logicalResourceIdentifier(TEST_RESOURCE_ID)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithoutRetentionProperties() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .tableName(TEST_TABLE_NAME)
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(TEST_CLIENT_REQUEST_TOKEN)
                .logicalResourceIdentifier(TEST_RESOURCE_ID)
                .build();
    }

}
