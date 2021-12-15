package software.amazon.timestream.table;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
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
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamwrite.model.Endpoint;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.Table;
import com.amazonaws.services.timestreamwrite.model.TagResourceRequest;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.UntagResourceRequest;
import com.amazonaws.services.timestreamwrite.model.UpdateTableRequest;
import com.amazonaws.services.timestreamwrite.model.UpdateTableResult;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
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
    private static final String TEST_TABLE_NAME = "TestTableName";
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
    public void updateTableShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();
        final Table record = new Table().withDatabaseName(TEST_DATABASE_NAME).withTableName(TEST_TABLE_NAME).withArn(TEST_ARN);
        final UpdateTableResult updateTableResult = new UpdateTableResult().withTable(record);
        doReturn(updateTableResult).when(proxy).injectCredentialsAndInvoke(any(UpdateTableRequest.class), any(Function.class));

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

        final UpdateTableRequest expectedUpdateTableRequest =
                new UpdateTableRequest()
                        .withDatabaseName(TEST_DATABASE_NAME)
                        .withTableName(TEST_TABLE_NAME)
                        .withRetentionProperties(new com.amazonaws.services.timestreamwrite.model.RetentionProperties()
                                .withMemoryStoreRetentionPeriodInHours(9L)
                                .withMagneticStoreRetentionPeriodInDays(14L));

        final TagResourceRequest expectedTagResourceRequest =
                new TagResourceRequest().withResourceARN(TEST_ARN).withTags(
                        new com.amazonaws.services.timestreamwrite.model.Tag()
                                .withKey(TEST_TAG_KEY_2).withValue(TEST_TAG_VALUE_2_NEW),
                        new com.amazonaws.services.timestreamwrite.model.Tag()
                                .withKey(TEST_TAG_KEY_3).withValue(TEST_TAG_VALUE_3));
        final UntagResourceRequest expectedUntagResourceRequest =
                new UntagResourceRequest().withResourceARN(TEST_ARN).withTagKeys(TEST_TAG_KEY_1);

        verify(proxy).injectCredentialsAndInvoke(eq(expectedUpdateTableRequest), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(eq(expectedTagResourceRequest), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(eq(expectedUntagResourceRequest), any(Function.class));
        verifyNoMoreInteractions(proxy);
    }

    /*
     * Tests for error handling.
     */
    @Test
    public void updateTableShouldThrowWhenResourceNotFound() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UpdateTableRequest.class), any(Function.class));

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateTableShouldThrowWhenInvalidRequest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UpdateTableRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateTableShouldThrowWhenGenericException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UpdateTableRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateTableShouldThrowWhenThrottled() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UpdateTableRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateTableShouldThrowWhenAccessDenied() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UpdateTableRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void updateTableShouldThrowWhenInvalidEndpointException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(UpdateTableRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .tableName(TEST_TABLE_NAME)
                        .retentionProperties(RetentionProperties.builder()
                                .memoryStoreRetentionPeriodInHours("9")
                                .magneticStoreRetentionPeriodInDays("14")
                                .build())
                        .tags(Arrays.asList(
                                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2_NEW).build(),
                                Tag.builder().key(TEST_TAG_KEY_3).value(TEST_TAG_VALUE_3).build()))
                        .build();

        ResourceModel existingModel =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .tableName(TEST_TABLE_NAME)
                        .retentionProperties(RetentionProperties.builder()
                                .memoryStoreRetentionPeriodInHours("1")
                                .magneticStoreRetentionPeriodInDays("2")
                                .build())
                        .tags(Arrays.asList(
                                Tag.builder().key(TEST_TAG_KEY_1).value(TEST_TAG_VALUE_1).build(),
                                Tag.builder().key(TEST_TAG_KEY_2).value(TEST_TAG_VALUE_2).build()))
                        .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model).previousResourceState(existingModel)
            .build();
    }
}
