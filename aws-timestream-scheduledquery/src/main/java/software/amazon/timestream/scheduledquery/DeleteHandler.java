package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.AmazonTimestreamQuery;
import com.amazonaws.services.timestreamquery.model.DescribeScheduledQueryRequest;
import com.amazonaws.services.timestreamquery.model.DeleteScheduledQueryRequest;
import com.amazonaws.services.timestreamquery.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamquery.model.ValidationException;
import com.amazonaws.services.timestreamquery.model.InvalidEndpointException;
import com.amazonaws.services.timestreamquery.model.AccessDeniedException;
import com.amazonaws.services.timestreamquery.model.ThrottlingException;
import com.amazonaws.services.timestreamquery.model.InternalServerException;

import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class DeleteHandler extends BaseHandler<CallbackContext> {

    private static final String DELETE_SCHEDULED_QUERY = "DeleteScheduledQuery";
    private static final String DESCRIBE_SCHEDULED_QUERY = "DescribeScheduledQuery";
    private static final int CALLBACK_DELAY_SECONDS = 15;
    private AmazonWebServicesClientProxy proxy;
    private AmazonTimestreamQuery timestreamClient;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        timestreamClient = TimestreamClientFactory.get(proxy, logger);
        this.proxy = proxy;

        CallbackContext returnCallbackContext;
        if (callbackContext == null) {
            returnCallbackContext = CallbackContext.builder()
                    .deleteScheduledQueryStarted(false)
                    .build();
        } else {
            returnCallbackContext = callbackContext;
        }

        if (!returnCallbackContext.isDeleteScheduledQueryStarted()) {
            initiateDeleteScheduledQuery(request, returnCallbackContext, model);
        }

        return checkDeleteScheduledQueryStatus(request, returnCallbackContext, model);
    }

    private void initiateDeleteScheduledQuery(
            ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, ResourceModel model) {
        final DeleteScheduledQueryRequest deleteScheduledQueryRequest =
                new DeleteScheduledQueryRequest()
                        .withScheduledQueryArn(model.getArn());

        try {
            this.proxy.injectCredentialsAndInvoke(deleteScheduledQueryRequest, timestreamClient::deleteScheduledQuery);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(DELETE_SCHEDULED_QUERY, ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(DELETE_SCHEDULED_QUERY, ex);
        }

        callbackContext.setDeleteScheduledQueryStarted(true);
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkDeleteScheduledQueryStatus(
            ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, ResourceModel model) {

        DescribeScheduledQueryRequest describeScheduledQueryRequest =
                new DescribeScheduledQueryRequest()
                        .withScheduledQueryArn(model.getArn());

        try {
            this.proxy.injectCredentialsAndInvoke(describeScheduledQueryRequest, timestreamClient::describeScheduledQuery);
        } catch (ResourceNotFoundException ex) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(DESCRIBE_SCHEDULED_QUERY, ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(DESCRIBE_SCHEDULED_QUERY, ex);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .callbackContext(callbackContext)
                .resourceModel(model)
                .status(OperationStatus.IN_PROGRESS)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();
    }
}
