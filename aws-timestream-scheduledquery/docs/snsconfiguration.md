# AWS::Timestream::ScheduledQuery SnsConfiguration

SNS configuration for notification upon scheduled query execution.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#topicarn" title="TopicArn">TopicArn</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#topicarn" title="TopicArn">TopicArn</a>: <i>String</i>
</pre>

## Properties

#### TopicArn

SNS topic ARN that the scheduled query status notifications will be sent to.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2048</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

