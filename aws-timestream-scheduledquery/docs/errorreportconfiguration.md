# AWS::Timestream::ScheduledQuery ErrorReportConfiguration

Configuration for error reporting. Error reports will be generated when a problem is encountered when writing the query results.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#s3configuration" title="S3Configuration">S3Configuration</a>" : <i><a href="s3configuration.md">S3Configuration</a></i>
}
</pre>

### YAML

<pre>
<a href="#s3configuration" title="S3Configuration">S3Configuration</a>: <i><a href="s3configuration.md">S3Configuration</a></i>
</pre>

## Properties

#### S3Configuration

Details on S3 location for error reports that result from running a query.

_Required_: Yes

_Type_: <a href="s3configuration.md">S3Configuration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

