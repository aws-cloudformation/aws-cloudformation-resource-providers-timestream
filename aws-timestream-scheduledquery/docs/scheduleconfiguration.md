# AWS::Timestream::ScheduledQuery ScheduleConfiguration

Configuration for when the scheduled query is executed.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#scheduleexpression" title="ScheduleExpression">ScheduleExpression</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#scheduleexpression" title="ScheduleExpression">ScheduleExpression</a>: <i>String</i>
</pre>

## Properties

#### ScheduleExpression

An expression that denotes when to trigger the scheduled query run. This can be a cron expression or a rate expression.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

