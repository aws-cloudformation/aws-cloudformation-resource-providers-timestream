package software.amazon.timestream.table;

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
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamwrite.model.DescribeTableRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeTableResult;
import com.amazonaws.services.timestreamwrite.model.Endpoint;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ListTagsForResourceRequest;
import com.amazonaws.services.timestreamwrite.model.ListTagsForResourceResult;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private static final String TEST_DATABASE_NAME = "TestDatabaseName";
    private static final String TEST_TABLE_NAME = "TestTableName";
    private static final String TEST_ARN = "TestArn";
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
    public void readTableShouldSucceed() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        final Table record = new Table().withDatabaseName(TEST_DATABASE_NAME).withTableName(TEST_TABLE_NAME).withArn(TEST_ARN);

        final DescribeTableResult describeTableResult = new DescribeTableResult().withTable(record);
        final ListTagsForResourceResult listTagsForResourceResult = givenAListTagsForResourceResultWithTags();

        doReturn(describeTableResult).when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class), any(Function.class));
        doReturn(listTagsForResourceResult).when(proxy).injectCredentialsAndInvoke(any(ListTagsForResourceRequest.class), any(Function.class));

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        final ResourceModel expectedResponseResourceModel =
                ResourceModel.builder()
                        .databaseName(TEST_DATABASE_NAME)
                        .tableName(TEST_TABLE_NAME)
                        .arn(TEST_ARN)
                        .name(TEST_TABLE_NAME)
                        .tags(Collections.singletonList(Tag.builder().key(TEST_TAG_KEY).value(TEST_TAG_VALUE).build()))
                        .build();
        assertThat(response.getResourceModel()).isEqualTo(expectedResponseResourceModel);

        final DescribeTableRequest expectedDescribeTableRequest =
                new DescribeTableRequest().withDatabaseName(TEST_DATABASE_NAME).withTableName(TEST_TABLE_NAME);
        verify(proxy).injectCredentialsAndInvoke(eq(expectedDescribeTableRequest), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(eq(new ListTagsForResourceRequest().withResourceARN(TEST_ARN)),
                any(Function.class));
        verifyNoMoreInteractions(proxy);
    }

    /*
     * Tests for error handling.
     */
    @Test
    public void readTableShouldThrowWhenResourceNotFound() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class), any(Function.class));

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readTableShouldThrowWhenInvalidRequest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readTableShouldThrowWhenMissingPermissions() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readTableShouldThrowWhenThrottled() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readTableShouldThrowWhenGenericException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void readTableShouldThrowWhenInvalidEndpointException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model =
                ResourceModel.builder().databaseName(TEST_DATABASE_NAME).tableName(TEST_TABLE_NAME).build();

        return ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
    }

    private ListTagsForResourceResult givenAListTagsForResourceResultWithTags() {
        final ListTagsForResourceResult mockListTagsForResourceResult = mock(ListTagsForResourceResult.class);
        final List<com.amazonaws.services.timestreamwrite.model.Tag> tags =
                Collections.singletonList(
                        new com.amazonaws.services.timestreamwrite.model.Tag().withKey(TEST_TAG_KEY).withValue(TEST_TAG_VALUE));
        when(mockListTagsForResourceResult.getTags()).thenReturn(tags);
        return mockListTagsForResourceResult;
    }
}
