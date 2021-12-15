# AWS::Timestream::ScheduledQuery

The AWS::Timestream::ScheduledQuery resource creates a Timestream Scheduled Query.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Timestream::ScheduledQuery",
    "Properties" : {
        "<a href="#scheduledqueryname" title="ScheduledQueryName">ScheduledQueryName</a>" : <i>String</i>,
        "<a href="#querystring" title="QueryString">QueryString</a>" : <i>String</i>,
        "<a href="#scheduleconfiguration" title="ScheduleConfiguration">ScheduleConfiguration</a>" : <i><a href="scheduleconfiguration.md">ScheduleConfiguration</a></i>,
        "<a href="#notificationconfiguration" title="NotificationConfiguration">NotificationConfiguration</a>" : <i><a href="notificationconfiguration.md">NotificationConfiguration</a></i>,
        "<a href="#clienttoken" title="ClientToken">ClientToken</a>" : <i>String</i>,
        "<a href="#scheduledqueryexecutionrolearn" title="ScheduledQueryExecutionRoleArn">ScheduledQueryExecutionRoleArn</a>" : <i>String</i>,
        "<a href="#targetconfiguration" title="TargetConfiguration">TargetConfiguration</a>" : <i><a href="targetconfiguration.md">TargetConfiguration</a></i>,
        "<a href="#errorreportconfiguration" title="ErrorReportConfiguration">ErrorReportConfiguration</a>" : <i><a href="errorreportconfiguration.md">ErrorReportConfiguration</a></i>,
        "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Timestream::ScheduledQuery
Properties:
    <a href="#scheduledqueryname" title="ScheduledQueryName">ScheduledQueryName</a>: <i>String</i>
    <a href="#querystring" title="QueryString">QueryString</a>: <i>String</i>
    <a href="#scheduleconfiguration" title="ScheduleConfiguration">ScheduleConfiguration</a>: <i><a href="scheduleconfiguration.md">ScheduleConfiguration</a></i>
    <a href="#notificationconfiguration" title="NotificationConfiguration">NotificationConfiguration</a>: <i><a href="notificationconfiguration.md">NotificationConfiguration</a></i>
    <a href="#clienttoken" title="ClientToken">ClientToken</a>: <i>String</i>
    <a href="#scheduledqueryexecutionrolearn" title="ScheduledQueryExecutionRoleArn">ScheduledQueryExecutionRoleArn</a>: <i>String</i>
    <a href="#targetconfiguration" title="TargetConfiguration">TargetConfiguration</a>: <i><a href="targetconfiguration.md">TargetConfiguration</a></i>
    <a href="#errorreportconfiguration" title="ErrorReportConfiguration">ErrorReportConfiguration</a>: <i><a href="errorreportconfiguration.md">ErrorReportConfiguration</a></i>
    <a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### ScheduledQueryName

The name of the scheduled query. Scheduled query names must be unique within each Region.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Pattern_: <code>[a-zA-Z0-9_.-]+</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### QueryString

The query string to run. Parameter names can be specified in the query string @ character followed by an identifier. The named Parameter @scheduled_runtime is reserved and can be used in the query to get the time at which the query is scheduled to run. The timestamp calculated according to the ScheduleConfiguration parameter, will be the value of @scheduled_runtime paramater for each query run. For example, consider an instance of a scheduled query executing on 2021-12-01 00:00:00. For this instance, the @scheduled_runtime parameter is initialized to the timestamp 2021-12-01 00:00:00 when invoking the query.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>262144</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ScheduleConfiguration

Configuration for when the scheduled query is executed.

_Required_: Yes

_Type_: <a href="scheduleconfiguration.md">ScheduleConfiguration</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### NotificationConfiguration

Notification configuration for the scheduled query. A notification is sent by Timestream when a query run finishes, when the state is updated or when you delete it.

_Required_: Yes

_Type_: <a href="notificationconfiguration.md">NotificationConfiguration</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ClientToken

Using a ClientToken makes the call to CreateScheduledQuery idempotent, in other words, making the same request repeatedly will produce the same result. Making multiple identical CreateScheduledQuery requests has the same effect as making a single request. If CreateScheduledQuery is called without a ClientToken, the Query SDK generates a ClientToken on your behalf. After 8 hours, any request with the same ClientToken is treated as a new request.

_Required_: No

_Type_: String

_Minimum_: <code>32</code>

_Maximum_: <code>128</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ScheduledQueryExecutionRoleArn

The ARN for the IAM role that Timestream will assume when running the scheduled query.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2048</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### TargetConfiguration

Configuration of target store where scheduled query results are written to.

_Required_: No

_Type_: <a href="targetconfiguration.md">TargetConfiguration</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ErrorReportConfiguration

Configuration for error reporting. Error reports will be generated when a problem is encountered when writing the query results.

_Required_: Yes

_Type_: <a href="errorreportconfiguration.md">ErrorReportConfiguration</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### KmsKeyId

The Amazon KMS key used to encrypt the scheduled query resource, at-rest. If the Amazon KMS key is not specified, the scheduled query resource will be encrypted with a Timestream owned Amazon KMS key. To specify a KMS key, use the key ID, key ARN, alias name, or alias ARN. When using an alias name, prefix the name with alias/. If ErrorReportConfiguration uses SSE_KMS as encryption type, the same KmsKeyId is used to encrypt the error report at rest.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2048</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

A list of key-value pairs to label the scheduled query.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Amazon Resource Name of the scheduled query that is generated upon creation.

#### SQName

The name of the scheduled query. Scheduled query names must be unique within each Region.

#### SQQueryString

The query string to run. Parameter names can be specified in the query string @ character followed by an identifier. The named Parameter @scheduled_runtime is reserved and can be used in the query to get the time at which the query is scheduled to run. The timestamp calculated according to the ScheduleConfiguration parameter, will be the value of @scheduled_runtime paramater for each query run. For example, consider an instance of a scheduled query executing on 2021-12-01 00:00:00. For this instance, the @scheduled_runtime parameter is initialized to the timestamp 2021-12-01 00:00:00 when invoking the query.

#### SQScheduleConfiguration

Configuration for when the scheduled query is executed.

#### SQNotificationConfiguration

Notification configuration for the scheduled query. A notification is sent by Timestream when a query run finishes, when the state is updated or when you delete it.

#### SQScheduledQueryExecutionRoleArn

The ARN for the IAM role that Timestream will assume when running the scheduled query.

#### SQTargetConfiguration

Configuration of target store where scheduled query results are written to.

#### SQErrorReportConfiguration

Configuration for error reporting. Error reports will be generated when a problem is encountered when writing the query results.

#### SQKmsKeyId

The Amazon KMS key used to encrypt the scheduled query resource, at-rest. If the Amazon KMS key is not specified, the scheduled query resource will be encrypted with a Timestream owned Amazon KMS key. To specify a KMS key, use the key ID, key ARN, alias name, or alias ARN. When using an alias name, prefix the name with alias/. If ErrorReportConfiguration uses SSE_KMS as encryption type, the same KmsKeyId is used to encrypt the error report at rest.

