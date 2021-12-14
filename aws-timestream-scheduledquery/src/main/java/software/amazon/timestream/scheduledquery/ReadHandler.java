package software.amazon.timestream.scheduledquery;

import com.amazonaws.services.timestreamquery.AmazonTimestreamQuery;
import com.amazonaws.services.timestreamquery.model.DescribeScheduledQueryRequest;
import com.amazonaws.services.timestreamquery.model.DescribeScheduledQueryResult ;
import com.amazonaws.services.timestreamquery.model.ScheduledQueryDescription ;
import com.amazonaws.services.timestreamquery.model.ListTagsForResourceRequest;
import com.amazonaws.services.timestreamquery.model.ListTagsForResourceResult;
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

import java.util.List;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final String DESCRIBE_SCHEDULED_QUERY = "DescribeScheduledQuery";
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

        final DescribeScheduledQueryRequest describeScheduledQueryRequest =
                new DescribeScheduledQueryRequest().withScheduledQueryArn(model.getArn());

        try {
            final DescribeScheduledQueryResult describeScheduledQueryResult =
                    this.proxy.injectCredentialsAndInvoke(describeScheduledQueryRequest, timestreamClient::describeScheduledQuery);

            final ScheduledQueryDescription scheduledQueryDescription = describeScheduledQueryResult.getScheduledQuery();
            model.setArn(scheduledQueryDescription.getArn());
            model.setSQName(scheduledQueryDescription.getName());
            model.setSQQueryString(scheduledQueryDescription.getQueryString());
            model.setSQScheduleConfiguration(scheduledQueryDescription.getScheduleConfiguration().toString());
            model.setSQNotificationConfiguration(scheduledQueryDescription.getNotificationConfiguration().toString());
            model.setSQScheduledQueryExecutionRoleArn(scheduledQueryDescription.getScheduledQueryExecutionRoleArn());
            model.setSQTargetConfiguration(scheduledQueryDescription.getTargetConfiguration() != null ?
                    scheduledQueryDescription.getTargetConfiguration().toString() : null);
            model.setSQErrorReportConfiguration(scheduledQueryDescription.getErrorReportConfiguration().toString());
            model.setSQKmsKeyId(scheduledQueryDescription.getKmsKeyId());
            model.setTags(getTags(scheduledQueryDescription.getArn()));
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(DESCRIBE_SCHEDULED_QUERY, ex);
        } catch (ThrottlingException ex) {
            throw new CfnThrottlingException(DESCRIBE_SCHEDULED_QUERY, ex);
        } catch (InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private List<Tag> getTags(final String arn) {
        final ListTagsForResourceRequest listTagsForResourceRequest =
                new ListTagsForResourceRequest().withResourceARN(arn);

        final ListTagsForResourceResult listTagsForResourceResult =
                this.proxy.injectCredentialsAndInvoke(
                        listTagsForResourceRequest, timestreamClient::listTagsForResource);

        return ScheduledQueryModelConverter
                .convertToModelTags(listTagsForResourceResult.getTags());
    }
}
