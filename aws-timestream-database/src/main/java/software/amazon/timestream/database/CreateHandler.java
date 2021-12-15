package software.amazon.timestream.database;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import com.amazonaws.services.timestreamwrite.AmazonTimestreamWrite;
import com.amazonaws.services.timestreamwrite.model.AccessDeniedException;
import com.amazonaws.services.timestreamwrite.model.ConflictException;
import com.amazonaws.services.timestreamwrite.model.CreateDatabaseRequest;
import com.amazonaws.services.timestreamwrite.model.CreateDatabaseResult;
import com.amazonaws.services.timestreamwrite.model.InternalServerException;
import com.amazonaws.services.timestreamwrite.model.InvalidEndpointException;
import com.amazonaws.services.timestreamwrite.model.ServiceQuotaExceededException;
import com.amazonaws.services.timestreamwrite.model.ThrottlingException;
import com.amazonaws.services.timestreamwrite.model.ValidationException;
import com.amazonaws.util.StringUtils;

/**
 * Timestream database resource creation handler. CloudFormation invokes this handler
 * when the resource is initially created during stack create operations.
 */
public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final String CREATE_DATABASE = "CreateDatabase";
    private static final String QUOTE_MESSAGE = "Limit for number of databases per account exceeded.";

    private static final int DATABASE_NAME_MAX_LENGTH = 256;

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

        // resource can auto-generate a name if not supplied by caller
        // this logic should move up into the CloudFormation engine, but
        // currently exists here for backwards-compatibility with existing models
        if (StringUtils.isNullOrEmpty(model.getDatabaseName())) {
            model.setDatabaseName(
                    IdentifierUtils.generateResourceIdentifier(
                            request.getLogicalResourceIdentifier(),
                            request.getClientRequestToken(),
                            DATABASE_NAME_MAX_LENGTH
                    ));
        }

        final CreateDatabaseRequest createDatabaseRequest = new CreateDatabaseRequest()
                .withDatabaseName(model.getDatabaseName())
                .withKmsKeyId(model.getKmsKeyId());

        final Set<Tag> tags = TagHelper.convertToSet(
                TagHelper.generateTagsForCreate(model, request));
        if (tags != null & !tags.isEmpty()) {
            createDatabaseRequest.withTags(tags.stream().map(
                    tag -> new com.amazonaws.services.timestreamwrite.model.Tag()
                            .withKey(tag.getKey())
                            .withValue(tag.getValue()))
                    .collect(Collectors.toList()));
        }

        try {
            final CreateDatabaseResult result =
                    this.proxy.injectCredentialsAndInvoke(createDatabaseRequest, timestreamClient::createDatabase);
            model.setArn(result.getDatabase().getArn());
        } catch (ConflictException ex) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getDatabaseName(), ex);
        } catch (ValidationException | InvalidEndpointException ex) {
            throw new CfnInvalidRequestException(request.toString(), ex);
        } catch (AccessDeniedException ex) {
            throw new CfnAccessDeniedException(CREATE_DATABASE, ex);
        } catch (ServiceQuotaExceededException ex) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, QUOTE_MESSAGE, ex);
        } catch(ThrottlingException ex) {
            throw new CfnThrottlingException(CREATE_DATABASE, ex);
        } catch(InternalServerException ex) {
            throw new CfnInternalFailureException(ex);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
