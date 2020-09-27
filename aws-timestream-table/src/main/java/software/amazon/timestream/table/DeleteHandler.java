package software.amazon.timestream.table;

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

import com.amazonaws.services.timestreamwrite.AmazonTimestreamWrite;
import com.amazonaws.services.timestreamwrite.model.AccessDeniedException;
import com.amazonaws.services.timestreamwrite.model.DeleteTableRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeTableRequest;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

/**
 * Timestream table resource deletion handler. CloudFormation invokes this handler
 * when the resource is deleted, either when the resource is deleted from the stack as
 * part of a stack update operation, or the stack itself is deleted.
 */
public class DeleteHandler extends BaseHandler<CallbackContext> {

    private static final String DELETE_TABLE = "DeleteTable";
    private static final String DESCRIBE_TABLE = "DescribeTable";
    private static final int CALLBACK_DELAY_SECONDS = 15;
    private AmazonWebServicesClientProxy proxy;
    private AmazonTimestreamWrite timestreamClient;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            Logger logger) {

        timestreamClient = TimestreamClientFactory.get(proxy, logger);
        this.proxy = proxy;

        final ResourceModel model = request.getDesiredResourceState();

        if (callbackContext == null) {
            callbackContext = CallbackContext.builder()
                    .deleteTableStarted(false)
                    .deleteTableStabilized(false)
                    .build();
        }

        if (! callbackContext.isDeleteTableStarted()) {
            initiateDeleteTable(request, callbackContext, model);
        }

        if (callbackContext.isDeleteTableStarted() && ! callbackContext.isDeleteTableStabilized()) {
            return checkDeleteTableStatus(model, callbackContext);
        } else {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .build();
        }
    }

    private void initiateDeleteTable(
            ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, ResourceModel model) {
        final DeleteTableRequest deleteTableRequest =
                new DeleteTableRequest()
                        .withDatabaseName(model.getDatabaseName())
                        .withTableName(model.getTableName());

        try {
            this.proxy.injectCredentialsAndInvoke(deleteTableRequest, timestreamClient::deleteTable);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(DELETE_TABLE, ex);
        } catch (ValidationException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (ResourceNotFoundException ex) {
            // could be either database does not exist or table does not exist.
            throw new CfnNotFoundException(ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(DELETE_TABLE, ex);
        }

        callbackContext.setDeleteTableStarted(true);
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkDeleteTableStatus(ResourceModel model,
            CallbackContext callbackContext) {
        DescribeTableRequest describeTableRequest = new DescribeTableRequest()
                .withDatabaseName(model.getDatabaseName())
                .withTableName(model.getTableName());

        try {
            this.proxy.injectCredentialsAndInvoke(describeTableRequest, timestreamClient::describeTable);
        } catch (ResourceNotFoundException ex) {
            // could be either database does not exist or table does not exist.
            // In both cases table is gone and we can return success.
            return  ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .build();
        }

        return  ProgressEvent.<ResourceModel, CallbackContext>builder()
                .callbackContext(callbackContext)
                .resourceModel(model)
                .status(OperationStatus.IN_PROGRESS)
                .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                .build();

    }
}