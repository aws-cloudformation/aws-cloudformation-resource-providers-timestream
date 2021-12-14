package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.model.DescribeEndpointsResult;
import com.amazonaws.services.timestreamquery.model.DescribeEndpointsRequest;
import com.amazonaws.services.timestreamquery.model.Endpoint;
import com.amazonaws.services.timestreamquery.model.DescribeScheduledQueryRequest;
import com.amazonaws.services.timestreamquery.model.DeleteScheduledQueryRequest;
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

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    private static final String TEST_ARN = "TestArn";
    private static final int CALLBACK_DELAY_SECONDS = 15;

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
    public void deleteScheduledQueryTest() {
        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        doThrow(new ResourceNotFoundException("Test exception")).when(proxy)
                .injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(null);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        verify(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));
    }

    @Test
    public void deleteScheduledQueryAfterWaitTest() {
        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        doReturn(null).doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(CALLBACK_DELAY_SECONDS);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(null);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        verify(proxy, times(2))
                .injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));
    }

    @Test
    public void deleteScheduledQueryThrowWhenResourceNotFoundTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ResourceNotFoundException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteScheduledQueryThrowWhenInvalidRequestTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteScheduledQueryThrowWhenInvalidEndpointTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteScheduledQueryThrowWhenAccessDeniedTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteScheduledQueryThrowWhenThrottlingTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void deleteScheduledQueryThrowWhenInternalServerFailureTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void describeScheduledQueryThrowWhenInvalidRequestTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        doThrow(new ValidationException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void describeScheduledQueryThrowWhenInvalidEndpointTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        doThrow(new InvalidEndpointException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void describeScheduledQueryThrowWhenAccessDeniedTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        doThrow(new AccessDeniedException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnAccessDeniedException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void describeScheduledQueryThrowWhenThrottlingTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        doThrow(new ThrottlingException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnThrottlingException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void describeScheduledQueryThrowWhenInternalServerFailureTest() {
        final ResourceHandlerRequest<ResourceModel> request = givenAResourceHandlerRequest();

        doReturn(null)
                .when(proxy).injectCredentialsAndInvoke(any(DeleteScheduledQueryRequest.class), any(Function.class));
        doThrow(new InternalServerException("Test exception"))
                .when(proxy).injectCredentialsAndInvoke(any(DescribeScheduledQueryRequest.class), any(Function.class));

        assertThrows(
                CfnInternalFailureException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    private ResourceHandlerRequest<ResourceModel> givenAResourceHandlerRequest() {
        final ResourceModel model = ResourceModel.builder()
                .arn(TEST_ARN)
                .build();
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }
}
