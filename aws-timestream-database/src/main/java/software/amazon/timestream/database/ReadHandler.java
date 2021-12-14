package software.amazon.timestream.database;

import java.util.List;
import java.util.stream.Collectors;

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

import com.amazonaws.services.timestreamwrite.AmazonTimestreamWrite;
import com.amazonaws.services.timestreamwrite.model.AccessDeniedException;
import com.amazonaws.services.timestreamwrite.model.Database;
import com.amazonaws.services.timestreamwrite.model.DescribeDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeDatabaseResult;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ListTagsForResourceRequest;
import com.amazonaws.services.timestreamwrite.model.ListTagsForResourceResult;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

/**
 * Timestream database resource description handler. CloudFormation invokes this handler as part of a
 * stack update operation when detailed information about the resource's current state is required.
 */
public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final String DESCRIBE_DATABASE = "DescribeDatabase";
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

        ResourceModel result;
        final ResourceModel model = request.getDesiredResourceState();
        final DescribeDatabaseRequest describeDatabaseRequest =
                new DescribeDatabaseRequest().withDatabaseName(model.getDatabaseName());

        try {
            final DescribeDatabaseResult describeDatabaseResult =
                    this.proxy.injectCredentialsAndInvoke(describeDatabaseRequest, timestreamClient::describeDatabase);
            final Database databaseRecord = describeDatabaseResult.getDatabase();
            final List<Tag> tags = getTags(databaseRecord.getArn());
            model.setTags(tags == null || tags.isEmpty() ? null : tags);
            model.setArn(databaseRecord.getArn());
            result = model;
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getDatabaseName(), ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(DESCRIBE_DATABASE, ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(DESCRIBE_DATABASE, ex);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(result)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private List<Tag> getTags(final String arn) {
        final ListTagsForResourceRequest listTagsForResourceRequest =
                new ListTagsForResourceRequest().withResourceARN(arn);
        final ListTagsForResourceResult listTagsForResourceResult =
                this.proxy.injectCredentialsAndInvoke(
                        listTagsForResourceRequest, timestreamClient::listTagsForResource);

        if (listTagsForResourceResult.getTags() == null) {
            return null;
        }

        return listTagsForResourceResult.getTags().stream()
                .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                .collect(Collectors.toList());
    }
}
