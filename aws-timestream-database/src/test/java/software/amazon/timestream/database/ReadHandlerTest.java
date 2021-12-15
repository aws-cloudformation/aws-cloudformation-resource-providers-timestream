package software.amazon.timestream.database;

import java.util.Collections;
import java.util.List;
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
import com.amazonaws.services.timestreamwrite.model.Database;
import com.amazonaws.services.timestreamwrite.model.DescribeDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeDatabaseResult;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamwrite.model.Endpoint;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ListTagsForResourceRequest;
import com.amazonaws.services.timestreamwrite.model.ListTagsForResourceResult;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private static final String TEST_DATABASE_NAME = "TestDatabaseName";
    private static final String TEST_ARN = "TestArn";
    private static final String TEST_TAG_KEY = "TestKey";
    private static final String TEST_TAG_VALUE = "TestValue";
    private static final String TEST_KMS_KEY_ID = "TestKMSKeyId";

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
    public void readDatabaseShouldSucceed() {
        final ResourceModel resourceModel =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .tags(Collections.singletonList(
                                Tag.builder().key(TEST_TAG_KEY).value(TEST_TAG_VALUE).build()))
                        .arn(TEST_ARN)
                        .build();

        final ResourceHandlerRequest<ResourceModel> request = givenAResourceReadHandlerRequest();
        final DescribeDatabaseResult describeDatabaseResult = givenADescribeDatabaseResult();
        final ListTagsForResourceResult listTagsForResourceResult = givenAListTagsForResourceResultWithTags();
        doReturn(describeDatabaseResult).when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));
        doReturn(listTagsForResourceResult).when(proxy).injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class),any(Function.class));

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(resourceModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        final DescribeDatabaseRequest expectedDescribeDatabaseRequest =
                new DescribeDatabaseRequest().withDatabaseName(TEST_DATABASE_NAME);
        verify(proxy).injectCredentialsAndInvoke(eq(expectedDescribeDatabaseRequest), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(eq(new ListTagsForResourceRequest().withResourceARN(TEST_ARN)), any(Function.class));
        verifyNoMoreInteractions(proxy);
    }

    /*
     *  Tests for error handling.
     */
    @Test
    public void readDatabaseShouldThrowWhenResourceNotFound() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceReadHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readDatabaseShouldThrowWhenInvalidRequest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceReadHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readDatabaseShouldThrowWhenMissingPermissions() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceReadHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(CfnAccessDeniedException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readDatabaseShouldThrowWhenThrottled() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceReadHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(CfnThrottlingException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readDatabaseShouldThrowWhenGenericException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceReadHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(CfnInternalFailureException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readDatabaseShouldThrowWhenInvalidEndpointException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceReadHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeDatabaseRequest.class), any(Function.class));

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    private DescribeDatabaseResult givenADescribeDatabaseResult() {
        final DescribeDatabaseResult mockDescribeDatabaseResult = mock(DescribeDatabaseResult.class);
        final Database mockDatabaseRecord = mock(Database.class);
        when(mockDatabaseRecord.getArn()).thenReturn(TEST_ARN);
        when(mockDescribeDatabaseResult.getDatabase()).thenReturn(mockDatabaseRecord);
        return mockDescribeDatabaseResult;
    }

    private ListTagsForResourceResult givenAListTagsForResourceResultWithTags() {
        final ListTagsForResourceResult mockListTagsForResourceResult = mock(ListTagsForResourceResult.class);
        final List<com.amazonaws.services.timestreamwrite.model.Tag> tags =
                Collections.singletonList(
                        new com.amazonaws.services.timestreamwrite.model.Tag().withKey(TEST_TAG_KEY).withValue(TEST_TAG_VALUE));
        when(mockListTagsForResourceResult.getTags()).thenReturn(tags);
        return mockListTagsForResourceResult;
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceReadHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .build();

        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }
}
