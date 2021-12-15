package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.AmazonTimestreamQuery;
import com.amazonaws.services.timestreamquery.model.CreateScheduledQueryRequest;
import com.amazonaws.services.timestreamquery.model.CreateScheduledQueryResult;
import com.amazonaws.services.timestreamquery.model.ConflictException;
import com.amazonaws.services.timestreamquery.model.ValidationException;
import com.amazonaws.services.timestreamquery.model.InvalidEndpointException;
import com.amazonaws.services.timestreamquery.model.AccessDeniedException;
import com.amazonaws.services.timestreamquery.model.ServiceQuotaExceededException;
import com.amazonaws.services.timestreamquery.model.ThrottlingException;
import com.amazonaws.services.timestreamquery.model.InternalServerException;

import com.amazonaws.util.StringUtils;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.Set;
import java.util.UUID;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final String NO_TARGET_CONFIGURATION = "NO TARGET CONFIGURATION AVAILABLE";
    private static final String NO_KMS_KEY_ID = "NO KMS KEY ID AVAILABLE";
    private static final String CREATE_SCHEDULED_QUERY = "CreateScheduledQuery";
    private static final String QUOTE_MESSAGE = "Limit for number of scheduled queries per account exceeded.";
    private static final int SCHEDULED_QUERY_NAME_MAX_LENGTH = 64;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        AmazonTimestreamQuery timestreamClient = TimestreamClientFactory.get(proxy, logger);

        if (StringUtils.isNullOrEmpty(model.getScheduledQueryName())) {
            model.setScheduledQueryName(
                    IdentifierUtils.generateResourceIdentifier(
                            request.getLogicalResourceIdentifier(),
                            request.getClientRequestToken(),
                            SCHEDULED_QUERY_NAME_MAX_LENGTH
                    ));
        }

        if (StringUtils.isNullOrEmpty(model.getClientToken())) {
            model.setClientToken(UUID.randomUUID().toString());
        }

        final CreateScheduledQueryRequest createScheduledQueryRequest =
                new CreateScheduledQueryRequest()
                        .withName(model.getScheduledQueryName())
                        .withQueryString(model.getQueryString())
                        .withScheduleConfiguration(ScheduledQueryModelConverter
                                .convertToTimestreamScheduleConfiguration(model.getScheduleConfiguration()))
                        .withNotificationConfiguration(ScheduledQueryModelConverter
                                .convertToTimestreamNotificationConfiguration(model.getNotificationConfiguration()))
                        .withClientToken(model.getClientToken())
                        .withScheduledQueryExecutionRoleArn(model.getScheduledQueryExecutionRoleArn())
                        .withErrorReportConfiguration(ScheduledQueryModelConverter
                                .convertToTimestreamErrorReportConfiguration(model.getErrorReportConfiguration()));

        final TargetConfiguration targetConfiguration = model.getTargetConfiguration();
        if (targetConfiguration != null) {
            createScheduledQueryRequest.withTargetConfiguration(ScheduledQueryModelConverter
                    .convertToTimestreamTargetConfiguration(targetConfiguration));
        }

        if (!StringUtils.isNullOrEmpty(model.getKmsKeyId())) {
            createScheduledQueryRequest.withKmsKeyId(model.getKmsKeyId());
        }

        final Set<Tag> tags = TagHelper.convertToSet(
                TagHelper.generateTagsForCreate(model, request));
        if (tags != null && !tags.isEmpty()) {
            createScheduledQueryRequest.withTags(ScheduledQueryModelConverter
                    .convertToTimestreamTags(tags));
        }

        try {
            final CreateScheduledQueryResult result =
                    proxy.injectCredentialsAndInvoke(createScheduledQueryRequest, timestreamClient::createScheduledQuery);
            model.setArn(result.getArn());
            model.setSQName(model.getScheduledQueryName());
            model.setSQQueryString(model.getQueryString());
            model.setSQScheduleConfiguration(model.getScheduleConfiguration().toString());
            model.setSQNotificationConfiguration(model.getNotificationConfiguration().toString());
            model.setSQScheduledQueryExecutionRoleArn(model.getScheduledQueryExecutionRoleArn());
            model.setSQErrorReportConfiguration(model.getErrorReportConfiguration().toString());

            if (model.getTargetConfiguration() != null) {
                model.setSQTargetConfiguration(model.getTargetConfiguration().toString());
            } else {
                model.setSQTargetConfiguration(NO_TARGET_CONFIGURATION);
            }

            if (!StringUtils.isNullOrEmpty(model.getKmsKeyId())) {
                model.setSQKmsKeyId(model.getKmsKeyId());
            } else {
                model.setSQKmsKeyId(NO_KMS_KEY_ID);
            }
        } catch (ConflictException ex) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getScheduledQueryName(), ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(CREATE_SCHEDULED_QUERY, ex);
        } catch (ServiceQuotaExceededException ex) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, QUOTE_MESSAGE, ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(CREATE_SCHEDULED_QUERY, ex);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
