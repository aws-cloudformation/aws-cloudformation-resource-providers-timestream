package software.amazon.timestream.database;

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
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.services.timestreamwrite.model.AccessDeniedException;
import com.amazonaws.services.timestreamwrite.model.ConflictException;
import com.amazonaws.services.timestreamwrite.model.CreateDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.CreateDatabaseResult;
import com.amazonaws.services.timestreamwrite.model.Database;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamwrite.model.Endpoint;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.ServiceQuotaExceededException;
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

    private static final String TEST_CLIENT_REQUEST_TOKEN = "00000000-0000-0000-0000-000000000000";
    private static final String TEST_RESOURCE_ID = "MyResource";
    private static final String TEST_TAG_KEY_1 = "TestTagKey1";
    private static final String TEST_TAG_KEY_2 = "TestTagKey2";
    private static final String TEST_TAG_KEY_3 = "TestTagKey3";
    private static final String TEST_TAG_VALUE_1 = "TestTagValue1";
    private static final String TEST_TAG_VALUE_2 = "TestTagValue2";
    private static final String TEST_TAG_VALUE_3 = "TestTagValue3";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private CreateHandler handler = new CreateHandler();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        doReturn(new DescribeEndpointsResult().withEndpoints(new Endpoint().withAddress("endpoint")))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeEndpointsRequest.class), any());
        logger = mock(Logger.class);
    }

    @Test
    public void createDatabaseShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(new CreateDatabaseResult().withDatabase(new Database().withArn("TestArn")))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

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

        final CreateDatabaseRequest expectedCreateDatabaseRequest = new CreateDatabaseRequest()
                .withDatabaseName("TestDatabaseName")
                .withKmsKeyId("TestKmsKeyId");
        verify(proxy).injectCredentialsAndInvoke(eq(expectedCreateDatabaseRequest), any());
    }

    @Test
    public void createDatabaseWithTagsShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequestWithTags();

        doReturn(new CreateDatabaseResult().withDatabase(new Database().withArn("TestArn")))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

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

        final CreateDatabaseRequest expectedCreateDatabaseRequest = new CreateDatabaseRequest()
                .withDatabaseName("TestDatabaseName")
                .withKmsKeyId("TestKmsKeyId")
                .withTags(
                        new com.amazonaws.services.timestreamwrite.model.Tag()
                                .withKey(TEST_TAG_KEY_1).withValue(TEST_TAG_VALUE_1),
                        new com.amazonaws.services.timestreamwrite.model.Tag()
                                .withKey(TEST_TAG_KEY_2).withValue(TEST_TAG_VALUE_2),
                        new com.amazonaws.services.timestreamwrite.model.Tag()
                                .withKey(TEST_TAG_KEY_3).withValue(TEST_TAG_VALUE_3));
        verify(proxy).injectCredentialsAndInvoke(eq(expectedCreateDatabaseRequest), any());
    }

    @Test
    public void createDatabaseWithOnlyDatabaseNameShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequestWithDatabaseNameOnly();

        doReturn(new CreateDatabaseResult().withDatabase(new Database().withArn("TestArn")))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

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

        final CreateDatabaseRequest expectedCreateDatabaseRequest = new CreateDatabaseRequest()
                .withDatabaseName("TestDatabaseName");
        verify(proxy).injectCredentialsAndInvoke(eq(expectedCreateDatabaseRequest), any());
    }

    @Test
    public void createDatabaseShouldSucceedWhenDatabaseNameIsNotProvided() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequestWithoutDatabaseName();

        doReturn(new CreateDatabaseResult().withDatabase(new Database().withArn("TestArn")))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

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

        final CreateDatabaseRequest expectedCreateDatabaseRequest = new CreateDatabaseRequest()
                // database name is generated by CloudFormation when not provided
                .withDatabaseName(request.getDesiredResourceState().getDatabaseName());
        verify(proxy).injectCredentialsAndInvoke(eq(expectedCreateDatabaseRequest), any());
    }

    /*
     * Tests for error handling.
     */
    @Test
    public void createDatabaseShouldThrowWhenDatabaseExists() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ConflictException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

        assertThrows(
                CfnAlreadyExistsException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createDatabaseShouldThrowWhenRequestIsNotValid() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createDatabaseShouldThrowWhenMissingPermission() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createDatabaseShouldThrowWhenDatabaseLimitExceeded() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ServiceQuotaExceededException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

        assertThrows(
                CfnServiceLimitExceededException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createDatabaseShouldThrowWhenThrottled() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void createDatabaseShouldThrowWhenGenericError() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(CreateDatabaseRequest.class), any());

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName("TestDatabaseName")
                        .kmsKeyId("TestKmsKeyId")
                        .build();

        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithTags() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName("TestDatabaseName")
                        .kmsKeyId("TestKmsKeyId")
                        .tags(Arrays.asList(
                                Tag.builder().key(TEST_TAG_KEY_1).value(TEST_TAG_VALUE_1).build(),
                                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2).build(),
                                Tag.builder().key(TEST_TAG_KEY_3).value(TEST_TAG_VALUE_3).build()))
                        .build();

        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithDatabaseNameOnly() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName("TestDatabaseName")
                        .build();

        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithoutDatabaseName() {
        final ResourceModel model =
                ResourceModel.builder()
                        .build();

        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken(TEST_CLIENT_REQUEST_TOKEN)
                .logicalResourceIdentifier(TEST_RESOURCE_ID)
                .build();
    }
}