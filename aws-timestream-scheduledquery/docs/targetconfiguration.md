# AWS::Timestream::ScheduledQuery TargetConfiguration

Configuration of target store where scheduled query results are written to.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#timestreamconfiguration" title="TimestreamConfiguration">TimestreamConfiguration</a>" : <i><a href="timestreamconfiguration.md">TimestreamConfiguration</a></i>
}
</pre>

### YAML

<pre>
<a href="#timestreamconfiguration" title="TimestreamConfiguration">TimestreamConfiguration</a>: <i><a href="timestreamconfiguration.md">TimestreamConfiguration</a></i>
</pre>

## Properties

#### TimestreamConfiguration

Configuration needed to write data into the Timestream database and table.

_Required_: Yes

_Type_: <a href="timestreamconfiguration.md">TimestreamConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

