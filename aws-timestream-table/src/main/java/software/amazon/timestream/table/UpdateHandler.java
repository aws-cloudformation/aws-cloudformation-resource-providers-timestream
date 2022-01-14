package software.amazon.timestream.table;

import java.util.Map;
import java.util.Set;
import java.util.Comparator;
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
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.RetentionProperties;
import com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties;
import com.amazonaws.services.timestreamwrite.model.TagResourceRequest;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.UntagResourceRequest;
import com.amazonaws.services.timestreamwrite.model.UpdateTableRequest;
import com.amazonaws.services.timestreamwrite.model.UpdateTableResult;
import com.amazonaws.services.timestreamwrite.model.ValidationException;
import com.google.common.collect.Sets;

/**
 * Timestream table resource update handler. CloudFormation invokes this handler when the
 * resource is updated as part of a stack update operation.
 */
public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final String UPDATE_TABLE = "UpdateTable";
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
        final ResourceModel existingModel = request.getPreviousResourceState();

        final Map<String, String> desiredTags = TagHelper.getNewDesiredTags(model, request);
        final Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);

        final Set<Tag> tagsToAdd = TagHelper.convertToSet(
                TagHelper.generateTagsToAdd(previousTags, desiredTags));
        final Set<String> tagsToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);

        try {
            RetentionProperties retentionProperties;

            if (model.getRetentionProperties() == null) {
                // TODO there are cases when Uluru not keeping RetentionProperties in the existing model properly
                // here will fetch from TS directly for the current RetentionProperties if not defined in the CFN
                // template
                final DescribeTableRequest describeTableRequest =
                        new DescribeTableRequest()
                                .withDatabaseName(model.getDatabaseName())
                                .withTableName(model.getTableName());
                final DescribeTableResult describeTableResult = this.proxy.injectCredentialsAndInvoke(describeTableRequest, timestreamClient::describeTable);
                retentionProperties = describeTableResult.getTable().getRetentionProperties();
            } else {
                retentionProperties = RetentionPropertiesModelConverter.convert(model.getRetentionProperties());
            }

            MagneticStoreWriteProperties magneticStoreWriteProperties = MagneticStoreWritePropertiesModelConverter.convert(model.getMagneticStoreWriteProperties());

            final UpdateTableRequest updateTableRequest =
                    new UpdateTableRequest()
                            .withDatabaseName(model.getDatabaseName())
                            .withTableName(model.getTableName())
                            .withRetentionProperties(retentionProperties)
                            .withMagneticStoreWriteProperties(magneticStoreWriteProperties);

            final UpdateTableResult updateTableResult =
                    this.proxy.injectCredentialsAndInvoke(updateTableRequest, timestreamClient::updateTable);
            /*
             * Update tags
             *
             * Here we first remove the tags no long exist, this includes tags whose values are modified.
             * New tags are added afterwards, including tags with updated values.
             */
            removeTags(updateTableResult.getTable().getArn(), tagsToRemove);
            addTags(updateTableResult.getTable().getArn(), tagsToAdd);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(UPDATE_TABLE, ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (ResourceNotFoundException ex) {
            // could be either database does not exist or table does not exist.
            throw new CfnNotFoundException(ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(UPDATE_TABLE, ex);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private void removeTags(final String arn, final Set<String> tagsToRemove) {
        if (!tagsToRemove.isEmpty()) {
            final UntagResourceRequest untagResourceRequest = new UntagResourceRequest()
                    .withResourceARN(arn)
                    .withTagKeys(tagsToRemove);
            this.proxy.injectCredentialsAndInvoke(untagResourceRequest, timestreamClient::untagResource);
        }
    }

    private void addTags(final String arn, final Set<Tag> tagsToAdd) {
        if (!tagsToAdd.isEmpty()) {
            final TagResourceRequest tagResourceRequest = new TagResourceRequest().withResourceARN(arn).withTags(
                    tagsToAdd.stream().map(
                                    tag -> new com.amazonaws.services.timestreamwrite.model.Tag()
                                            .withKey(tag.getKey())
                                            .withValue(tag.getValue()))
                            // sort with decisive order for reliable testing
                            .sorted(Comparator.comparing(com.amazonaws.services.timestreamwrite.model.Tag::getKey))
                            .collect(Collectors.toList()));
            this.proxy.injectCredentialsAndInvoke(tagResourceRequest, timestreamClient::tagResource);
        }
    }
}
