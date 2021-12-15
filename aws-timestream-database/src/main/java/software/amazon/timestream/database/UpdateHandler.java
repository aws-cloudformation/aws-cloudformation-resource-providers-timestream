package software.amazon.timestream.database;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;import java.util.stream.Collectors;

import com.amazonaws.services.timestreamwrite.model.TagResourceRequest;
import com.amazonaws.services.timestreamwrite.model.UntagResourceRequest;
import org.apache.commons.lang3.StringUtils;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.services.timestreamwrite.AmazonTimestreamWrite;
import com.amazonaws.services.timestreamwrite.model.AccessDeniedException;
import com.amazonaws.services.timestreamwrite.model.DescribeDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.DescribeDatabaseResult;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ResourceNotFoundException;
import com.amazonaws.services.timestreamwrite.model.ServiceQuotaExceededException;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.UpdateDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.UpdateDatabaseResult;
import com.amazonaws.services.timestreamwrite.model.ValidationException;
import com.google.common.collect.Sets;


/**
 * Timestream database resource update handler. CloudFormation invokes this handler when the
 * resource is updated as part of a stack update operation.
 *
 * Only updates on tagging is allowed for the existing Databases. Otherwise, since Timestream does not support UpdateDatabase
 * operation, this handler should fail. CF will try deleting and recreating the resource instead.
 *
 * https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement
 */
public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final String UPDATE_DATABASE = "UpdateDatabase";
    private static final String QUOTE_MESSAGE = "Limit for number of grants for this KMS key exceeded.";

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

        // Step 1: Update the DB with the new KMS CMK.
        if (!StringUtils.equals(model.getKmsKeyId(), existingModel.getKmsKeyId())) {
            try {
                final UpdateDatabaseRequest updateDatabaseRequest =
                        new UpdateDatabaseRequest()
                                .withDatabaseName(model.getDatabaseName())
                                .withKmsKeyId(model.getKmsKeyId());

                this.proxy.injectCredentialsAndInvoke(updateDatabaseRequest, timestreamClient::updateDatabase);
            } catch (InternalServerException ex) {
                throw new CfnInternalFailureException(ex);
            } catch (ThrottlingException ex) {
                throw new CfnThrottlingException(UPDATE_DATABASE, ex);
            } catch (ValidationException | InvalidEndpointException ex) {
                throw new CfnInvalidRequestException(request.toString(), ex);
            } catch (ResourceNotFoundException ex) {
                throw new CfnNotFoundException(ex);
            } catch (AccessDeniedException ex) {
                throw new CfnAccessDeniedException(UPDATE_DATABASE, ex);
            } catch (ServiceQuotaExceededException ex) {
                throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, QUOTE_MESSAGE, ex);
            }
        }

        // Step 2: Update the tags

        final Map<String, String> desiredTags = TagHelper.getNewDesiredTags(model, request);
        final Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);

        final Set<Tag> tagsToAdd = TagHelper.convertToSet(
                TagHelper.generateTagsToAdd(previousTags, desiredTags));
        final Set<String> tagsToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);

        try {
            final DescribeDatabaseRequest describeDatabaseRequest =
                    new DescribeDatabaseRequest().withDatabaseName(model.getDatabaseName());
            final DescribeDatabaseResult describeDatabaseResult =
                    this.proxy.injectCredentialsAndInvoke(describeDatabaseRequest, timestreamClient::describeDatabase);

            /*
             * Update tags
             *
             * Here we first remove the tags no long exist, this includes tags whose values are modified.
             * New tags are added afterwards, including tags with updated values.
             */
            removeTags(describeDatabaseResult.getDatabase().getArn(), tagsToRemove);
            addTags(describeDatabaseResult.getDatabase().getArn(), tagsToAdd);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(UPDATE_DATABASE, ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(UPDATE_DATABASE, ex);
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
