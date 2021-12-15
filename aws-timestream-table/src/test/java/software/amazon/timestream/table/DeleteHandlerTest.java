package software.amazon.timestream.table;

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
import com.amazonaws.services.timestreamwrite.model.DeleteTableRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamwrite.model.DescribeTableRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeTableResult;
import com.amazonaws.services.timestreamwrite.model.Endpoint;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.RetentionProperties;
import com.amazonaws.services.timestreamwrite.model.Table;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {
    private static final String TEST_DATABASE_NAME = "TestDatabaseName";
    private static final String TEST_TABLE_NAME = "TestTableName";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private final DeleteHandler handler = new DeleteHandler();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        doReturn(new DescribeEndpointsResult().withEndpoints(new Endpoint().withAddress("endpoint")))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeEndpointsRequest.class), any(Function.class));
        logger = mock(Logger.class);
    }

    @Test
    public void deleteTableShouldSucceedSimply() {
        doReturn(null).when(proxy).injectCredentialsAndInvoke(any(DeleteTableRequest.class), any(Function.class));
        doThrow(new ResourceNotFoundException("Test Message")).when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class), any(Function.class));
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertNull(response.getResourceModel());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        final DeleteTableRequest expectedDeleteTableRequest =
                new DeleteTableRequest().withDatabaseName(TEST_DATABASE_NAME).withTableName(TEST_TABLE_NAME);
        verify(proxy).injectCredentialsAndInvoke(eq(expectedDeleteTableRequest), any(Function.class));
    }

    @Test
    public void deleteTableShouldSucceedAfterWait() {
        doReturn(null).when(proxy).injectCredentialsAndInvoke(any(DeleteTableRequest.class), any(Function.class));
        DescribeTableResult tableResponse = new DescribeTableResult()
                .withTable(new Table()
                          .withDatabaseName(TEST_DATABASE_NAME)
                          .withTableName(TEST_TABLE_NAME)
                          .withRetentionProperties(new RetentionProperties()
                                                  .withMemoryStoreRetentionPeriodInHours(1L)
                                                  .withMagneticStoreRetentionPeriodInDays(2L))
                          .withTableStatus("DELETE_IN_PROGRESS"));

        doReturn(tableResponse).doThrow(new ResourceNotFoundException("Test Message"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeTableRequest.class),any(Function.class));
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();


        ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(15);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        final DeleteTableRequest expectedDeleteTableRequest =
                new DeleteTableRequest().withDatabaseName(TEST_DATABASE_NAME).withTableName(TEST_TABLE_NAME);

        verify(proxy).injectCredentialsAndInvoke(eq(expectedDeleteTableRequest), any(Function.class));

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertNull(response.getResourceModel());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }

    /*
     * Tests for error handling.
     */
    @Test
    public void deleteTableShouldThrowWhenResourceNotFound() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteTableRequest.class), any(Function.class));

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteTableShouldThrowWhenInvalidRequest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteTableRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteTableShouldThrowWhenMissingPermissions() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteTableRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteTableShouldThrowWhenGenericException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteTableRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteTableShouldThrowWhenThrottled() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteTableRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteTableShouldThrowWhenInvalidEndpointException() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteTableRequest.class), any(Function.class));

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
}
