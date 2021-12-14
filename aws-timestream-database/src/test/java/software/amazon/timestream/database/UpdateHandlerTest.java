package software.amazon.timestream.database;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.services.timestreamwrite.model.AccessDeniedException;
import com.amazonaws.services.timestreamwrite.model.Database;
import com.amazonaws.services.timestreamwrite.model.DescribeDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeDatabaseResult;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamwrite.model.Endpoint;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.TagResourceRequest;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.UntagResourceRequest;
import com.amazonaws.services.timestreamwrite.model.UpdateDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.UpdateDatabaseResult;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {
    private static final String TEST_DATABASE_NAME = "TestDatabaseName";
    private static final String TEST_KMS_KEY_ID = "TestKmsKeyId";
    private static final String TEST_KMS_KEY_ID_2 = "TestKmsKeyId2";
    private static final String TEST_TAG_KEY_1 = "TestTagKey1";
    private static final String TEST_TAG_KEY_2 = "TestTagKey2";
    private static final String TEST_TAG_KEY_3 = "TestTagKey3";
    private static final String TEST_TAG_VALUE_1 = "TestTagValue1";
    private static final String TEST_TAG_VALUE_2 = "TestTagValue2";
    private static final String TEST_TAG_VALUE_2_NEW = "TestTagValue2New";
    private static final String TEST_TAG_VALUE_3 = "TestTagValue3";
    private static final String TEST_ARN = "TestArn";

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
    void updateKMSKeyIdShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request =
                givenAResourceHandlerRequestWithAttributes(
                        TEST_DATABASE_NAME, TEST_DATABASE_NAME, TEST_KMS_KEY_ID_2, TEST_KMS_KEY_ID, getTags(), getTags());

        final Database database = new Database()
                .withDatabaseName(TEST_DATABASE_NAME)
                .withKmsKeyId(TEST_KMS_KEY_ID)
                .withArn(TEST_ARN);

        final Database modifiedDatabase = new Database()
                .withDatabaseName(TEST_DATABASE_NAME)
                .withKmsKeyId(TEST_KMS_KEY_ID_2)
                .withArn(TEST_ARN);

        UpdateDatabaseResult updateDatabaseResult = new UpdateDatabaseResult().withDatabase(modifiedDatabase);
        doReturn(updateDatabaseResult).when(proxy).injectCredentialsAndInvoke(any(UpdateDatabaseRequest.class), any(Function.class));
        doReturn(new DescribeDatabaseResult().withDatabase(database))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

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

        UpdateDatabaseRequest expectedUpdateDatabaseRequest = new UpdateDatabaseRequest()
                .withDatabaseName(TEST_DATABASE_NAME)
                .withKmsKeyId(TEST_KMS_KEY_ID_2);

        verify(proxy).injectCredentialsAndInvoke(eq(expectedUpdateDatabaseRequest), any(Function.class));
        verifyNoMoreInteractions(proxy);
    }

    @Test
    void newKMSKeyIdShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request =
                givenAResourceHandlerRequestWithAttributes(
                        TEST_DATABASE_NAME, TEST_DATABASE_NAME, TEST_KMS_KEY_ID_2, null, getTags(), getTags());

        final Database database = new Database()
                .withDatabaseName(TEST_DATABASE_NAME)
                .withKmsKeyId(null)
                .withArn(TEST_ARN);
        final Database modifiedDatabase = new Database()
                .withDatabaseName(TEST_DATABASE_NAME)
                .withKmsKeyId(TEST_KMS_KEY_ID_2)
                .withArn(TEST_ARN);

        UpdateDatabaseResult updateDatabaseResult = new UpdateDatabaseResult().withDatabase(modifiedDatabase);
        doReturn(updateDatabaseResult).when(proxy).injectCredentialsAndInvoke(any(UpdateDatabaseRequest.class), any(Function.class));
        doReturn(new DescribeDatabaseResult().withDatabase(database))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

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

        UpdateDatabaseRequest expectedUpdateDatabaseRequest = new UpdateDatabaseRequest()
                .withDatabaseName(TEST_DATABASE_NAME)
                .withKmsKeyId(TEST_KMS_KEY_ID_2);

        verify(proxy).injectCredentialsAndInvoke(eq(expectedUpdateDatabaseRequest), any(Function.class));
        verifyNoMoreInteractions(proxy);
    }

    @Test
    public void updateTaggingForDatabaseShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();
        final Database database = new Database().withDatabaseName(TEST_DATABASE_NAME).withArn(TEST_ARN);

        doReturn(new DescribeDatabaseResult().withDatabase(database))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

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

        final TagResourceRequest expectedTagResourceRequest =
                new TagResourceRequest().withResourceARN(TEST_ARN).withTags(
                        new com.amazonaws.services.timestreamwrite.model.Tag()
                                .withKey(TEST_TAG_KEY_2).withValue(TEST_TAG_VALUE_2_NEW),
                        new com.amazonaws.services.timestreamwrite.model.Tag()
                                .withKey(TEST_TAG_KEY_3).withValue(TEST_TAG_VALUE_3));
        final UntagResourceRequest expectedUntagResourceRequest =
                new UntagResourceRequest().withResourceARN(TEST_ARN).withTagKeys(TEST_TAG_KEY_1);

        verify(proxy).injectCredentialsAndInvoke(eq(expectedTagResourceRequest), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(eq(expectedUntagResourceRequest), any(Function.class));
        verifyNoMoreInteractions(proxy);
    }

    /*
     *  Tests for error handling.
     */
    @Test
    public void updateDatabaseShouldThrowWhenInternalServerException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(CfnInternalFailureException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateDatabaseShouldThrowWhenThrottled() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateDatabaseShouldThrowWhenInvalidRequest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateDatabaseShouldThrowWhenResourceNotFound() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateDatabaseShouldThrowWhenAccessDenied() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateDatabaseShouldThrowWhenInvalidEndpointException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .kmsKeyId(TEST_KMS_KEY_ID)
                        .tags(Arrays.asList(
                                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2_NEW).build(),
                                Tag.builder().key(TEST_TAG_KEY_3).value(TEST_TAG_VALUE_3).build()))
                        .build();

        final ResourceModel existingModel =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .kmsKeyId(TEST_KMS_KEY_ID)
                        .tags(Arrays.asList(
                                Tag.builder().key(TEST_TAG_KEY_1).value(TEST_TAG_VALUE_1).build(),
                                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2).build()))
                        .build();

        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(existingModel)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequestWithAttributes(
            final String desiredDBName, final String existingDBName, final String desiredKMSKeyId, final String existingKMSKeyId,
            final List<Tag> desiredTagList, final List<Tag> existingTagList) {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName(desiredDBName)
                        .kmsKeyId(desiredKMSKeyId)
                        .tags(desiredTagList)
                        .build();

        final ResourceModel existingModel =
                ResourceModel.builder()
                        .databaseName(existingDBName)
                        .kmsKeyId(existingKMSKeyId)
                        .tags(existingTagList)
                        .build();

        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(existingModel)
                .build();
    }

    private List<Tag> getTags() {
        return Arrays.asList(
                Tag.builder().key(TEST_TAG_KEY_1).value(TEST_TAG_VALUE_1).build(),
                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2).build());
    }

    private List<Tag> getModifiedTags() {
        return Arrays.asList(
                Tag.builder().key(TEST_TAG_KEY_1).value(TEST_TAG_VALUE_1).build(),
                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2_NEW).build(),
                Tag.builder().key(TEST_TAG_KEY_3).value(TEST_TAG_VALUE_3).build());
    }
}
