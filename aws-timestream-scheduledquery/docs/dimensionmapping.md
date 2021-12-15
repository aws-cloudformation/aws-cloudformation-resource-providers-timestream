# AWS::Timestream::ScheduledQuery DimensionMapping

This type is used to map column(s) from the query result to a dimension in the destination table.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#name" title="Name">Name</a>" : <i>String</i>,
    "<a href="#dimensionvaluetype" title="DimensionValueType">DimensionValueType</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#name" title="Name">Name</a>: <i>String</i>
<a href="#dimensionvaluetype" title="DimensionValueType">DimensionValueType</a>: <i>String</i>
</pre>

## Properties

#### Name

Column name from query result.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DimensionValueType

Type for the dimension.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>VARCHAR</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

