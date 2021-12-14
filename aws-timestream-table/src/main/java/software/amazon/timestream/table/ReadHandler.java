package software.amazon.timestream.table;

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
import com.amazonaws.services.timestreamwrite.model.DescribeTableRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeTableResult;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ListTagsForResourceRequest;
import com.amazonaws.services.timestreamwrite.model.ListTagsForResourceResult;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.Table;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.ValidationException;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final String DESCRIBE_TABLE = "DescribeTable";
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
        final DescribeTableRequest describeTableRequest =
                new DescribeTableRequest().withDatabaseName(model.getDatabaseName()).withTableName(
                        model.getTableName());

        try {
            final DescribeTableResult describeTableResult =
                    this.proxy.injectCredentialsAndInvoke(describeTableRequest, timestreamClient::describeTable);

            final Table tableRecord = describeTableResult.getTable();
            final List<Tag> tags = getTags(tableRecord.getArn());
            model.setTags(tags == null || tags.isEmpty() ? null : tags);
            model.setArn(tableRecord.getArn());
            model.setName(tableRecord.getTableName());
            result = model;
        } catch (ResourceNotFoundException ex) {
            // could be either database does not exist or table does not exist.
            throw new CfnNotFoundException(ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(DESCRIBE_TABLE, ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(DESCRIBE_TABLE, ex);
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
