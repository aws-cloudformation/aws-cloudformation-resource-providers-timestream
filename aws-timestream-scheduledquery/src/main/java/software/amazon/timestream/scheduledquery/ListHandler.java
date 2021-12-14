package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.AmazonTimestreamQuery;
import com.amazonaws.services.timestreamquery.model.ListScheduledQueriesRequest;
import com.amazonaws.services.timestreamquery.model.ListScheduledQueriesResult;
import com.amazonaws.services.timestreamquery.model.ValidationException;
import com.amazonaws.services.timestreamquery.model.InvalidEndpointException;
import com.amazonaws.services.timestreamquery.model.AccessDeniedException;
import com.amazonaws.services.timestreamquery.model.ThrottlingException;
import com.amazonaws.services.timestreamquery.model.InternalServerException;

import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandler<CallbackContext> {

    private static final int MAX_ITEMS = 30;
    private static final String LIST_SCHEDULED_QUERIES = "ListScheduledQueries";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        AmazonTimestreamQuery timestreamClient = TimestreamClientFactory.get(proxy, logger);

        ListScheduledQueriesResult listScheduledQueriesResult;
        ListScheduledQueriesRequest listScheduledQueriesRequest =
                new ListScheduledQueriesRequest()
                        .withNextToken(request.getNextToken())
                        .withMaxResults(MAX_ITEMS);

        try {
            listScheduledQueriesResult =
                    proxy.injectCredentialsAndInvoke(listScheduledQueriesRequest, timestreamClient::listScheduledQueries);
        } catch(InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(LIST_SCHEDULED_QUERIES, ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(LIST_SCHEDULED_QUERIES, ex);
        }

        final List<ResourceModel> models = listScheduledQueriesResult.getScheduledQueries()
                .stream()
                .map(scheduledQuery ->
                        ResourceModel.builder()
                                .arn(scheduledQuery.getArn())
                                .build())
                .collect(Collectors.toList());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(listScheduledQueriesResult.getNextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
