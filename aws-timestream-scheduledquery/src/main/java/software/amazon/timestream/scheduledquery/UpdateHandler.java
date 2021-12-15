package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.AmazonTimestreamQuery;
import com.amazonaws.services.timestreamquery.model.TagResourceRequest;
import com.amazonaws.services.timestreamquery.model.UntagResourceRequest;
import com.amazonaws.services.timestreamquery.model.ValidationException;
import com.amazonaws.services.timestreamquery.model.InvalidEndpointException;
import com.amazonaws.services.timestreamquery.model.AccessDeniedException;
import com.amazonaws.services.timestreamquery.model.ThrottlingException;
import com.amazonaws.services.timestreamquery.model.InternalServerException;
import com.amazonaws.services.timestreamquery.model.ResourceNotFoundException;

import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private static final String UPDATE_SCHEDULED_QUERY = "UpdateScheduledQuery";
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

        final Map<String, String> desiredTags = TagHelper.getNewDesiredTags(model, request);
        final Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);

        final Set<Tag> tagsToAdd = TagHelper.convertToSet(
                TagHelper.generateTagsToAdd(previousTags, desiredTags));
        final Set<String> tagsToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);

        try {
            removeTags(model.getArn(), tagsToRemove);
            addTags(model.getArn(), tagsToAdd);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(UPDATE_SCHEDULED_QUERY, ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(UPDATE_SCHEDULED_QUERY, ex);
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
                                    tag -> ScheduledQueryModelConverter.convertToTimestreamTag(tag))
                            // sort with decisive order for reliable testing
                            .sorted(Comparator.comparing(com.amazonaws.services.timestreamquery.model.Tag::getKey))
                            .collect(Collectors.toList()));
            this.proxy.injectCredentialsAndInvoke(tagResourceRequest, timestreamClient::tagResource);
        }
    }
}
