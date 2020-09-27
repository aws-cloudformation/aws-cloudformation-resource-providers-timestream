package software.amazon.timestream.database;

import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.services.timestreamwrite.AmazonTimestreamWrite;
import com.amazonaws.services.timestreamwrite.model.ConflictException;
import com.amazonaws.services.timestreamwrite.model.DeleteDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

/**
 * Timestream database resource deletion handler. CloudFormation invokes this handler
 * when the resource is deleted, either when the resource is deleted from the stack as
 * part of a stack update operation, or the stack itself is deleted.
 */
public class DeleteHandler extends BaseHandler<CallbackContext> {

    private static final String DELETE_DATABASE = "DeleteDatabase";
    private static final String CONFLICT_REASON =
            "Database deletion conflicts with the resource's availability. E.g. trying to delete the database in CREATING state.";
    private AmazonWebServicesClientProxy proxy;
    private AmazonTimestreamWrite timestreamClient;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        timestreamClient = TimestreamClientFactory.get(proxy, logger);
        this.proxy = proxy;

        final ResourceModel model = request.getDesiredResourceState();

        final DeleteDatabaseRequest deleteDatabaseRequest =
                new DeleteDatabaseRequest().withDatabaseName(model.getDatabaseName());
        try {
            this.proxy.injectCredentialsAndInvoke(deleteDatabaseRequest, timestreamClient::deleteDatabase);
        } catch (final ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getDatabaseName(), ex);
        } catch (ConflictException ex) {
            throw new CfnResourceConflictException(ResourceModel.TYPE_NAME, model.getDatabaseName(), CONFLICT_REASON, ex);
        } catch (ValidationException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch(InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
