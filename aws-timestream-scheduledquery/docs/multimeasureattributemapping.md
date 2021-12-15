# AWS::Timestream::ScheduledQuery MultiMeasureAttributeMapping

An attribute mapping to be used for mapping query results to ingest data for multi-measure attributes.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#sourcecolumn" title="SourceColumn">SourceColumn</a>" : <i>String</i>,
    "<a href="#measurevaluetype" title="MeasureValueType">MeasureValueType</a>" : <i>String</i>,
    "<a href="#targetmultimeasureattributename" title="TargetMultiMeasureAttributeName">TargetMultiMeasureAttributeName</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#sourcecolumn" title="SourceColumn">SourceColumn</a>: <i>String</i>
<a href="#measurevaluetype" title="MeasureValueType">MeasureValueType</a>: <i>String</i>
<a href="#targetmultimeasureattributename" title="TargetMultiMeasureAttributeName">TargetMultiMeasureAttributeName</a>: <i>String</i>
</pre>

## Properties

#### SourceColumn

Source measure value column in the query result where the attribute value is to be read.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MeasureValueType

Value type of the measure value column to be read from the query result.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>BIGINT</code> | <code>BOOLEAN</code> | <code>DOUBLE</code> | <code>VARCHAR</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetMultiMeasureAttributeName

Custom name to be used for attribute name in derived table. If not provided, source column name would be used.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

