# AWS::Timestream::ScheduledQuery NotificationConfiguration

Notification configuration for the scheduled query. A notification is sent by Timestream when a query run finishes, when the state is updated or when you delete it.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#snsconfiguration" title="SnsConfiguration">SnsConfiguration</a>" : <i><a href="snsconfiguration.md">SnsConfiguration</a></i>
}
</pre>

### YAML

<pre>
<a href="#snsconfiguration" title="SnsConfiguration">SnsConfiguration</a>: <i><a href="snsconfiguration.md">SnsConfiguration</a></i>
</pre>

## Properties

#### SnsConfiguration

SNS configuration for notification upon scheduled query execution.

_Required_: Yes

_Type_: <a href="snsconfiguration.md">SnsConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

