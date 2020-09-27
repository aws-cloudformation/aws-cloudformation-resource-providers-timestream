package software.amazon.timestream.database;

import java.util.List;
import java.util.stream.Collectors;

import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.services.timestreamwrite.AmazonTimestreamWrite;
import com.amazonaws.services.timestreamwrite.model.AccessDeniedException;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.ListDatabasesRequest;
import com.amazonaws.services.timestreamwrite.model.ListDatabasesResult;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

/**
 * Timestream database resource list handler. CloudFormation invokes this handler when summary
 * information about multiple resources of this resource provider is required.
 */
public class ListHandler extends BaseHandler<CallbackContext> {

    private static final int MAX_ITEMS = 10;
    private static final String LIST_DATABASES = "ListDatabases";
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

        ListDatabasesResult result;
        final ListDatabasesRequest listDatabasesRequest =
                new ListDatabasesRequest().withNextToken(request.getNextToken()).withMaxResults(MAX_ITEMS);
        try {
            result = this.proxy.injectCredentialsAndInvoke(listDatabasesRequest, timestreamClient::listDatabases);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(LIST_DATABASES, ex);
        } catch (ValidationException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(LIST_DATABASES, ex);
        }

        final List<ResourceModel> models =
                result.getDatabases()
                        .stream()
                        .map(record -> ResourceModel.builder()
                                .databaseName(record.getDatabaseName())
                                .build())
                        .collect(Collectors.toList());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(result.getNextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}