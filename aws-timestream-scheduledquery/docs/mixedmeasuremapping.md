# AWS::Timestream::ScheduledQuery MixedMeasureMapping

MixedMeasureMappings are mappings that can be used to ingest data into a mixture of narrow and multi measures in the derived table.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#measurename" title="MeasureName">MeasureName</a>" : <i>String</i>,
    "<a href="#sourcecolumn" title="SourceColumn">SourceColumn</a>" : <i>String</i>,
    "<a href="#targetmeasurename" title="TargetMeasureName">TargetMeasureName</a>" : <i>String</i>,
    "<a href="#measurevaluetype" title="MeasureValueType">MeasureValueType</a>" : <i>String</i>,
    "<a href="#multimeasureattributemappings" title="MultiMeasureAttributeMappings">MultiMeasureAttributeMappings</a>" : <i>[ <a href="multimeasureattributemapping.md">MultiMeasureAttributeMapping</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#measurename" title="MeasureName">MeasureName</a>: <i>String</i>
<a href="#sourcecolumn" title="SourceColumn">SourceColumn</a>: <i>String</i>
<a href="#targetmeasurename" title="TargetMeasureName">TargetMeasureName</a>: <i>String</i>
<a href="#measurevaluetype" title="MeasureValueType">MeasureValueType</a>: <i>String</i>
<a href="#multimeasureattributemappings" title="MultiMeasureAttributeMappings">MultiMeasureAttributeMappings</a>: <i>
      - <a href="multimeasureattributemapping.md">MultiMeasureAttributeMapping</a></i>
</pre>

## Properties

#### MeasureName

Refers to the value of the measure name in a result row. This field is required if MeasureNameColumn is provided.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SourceColumn

This field refers to the source column from which the measure value is to be read for result materialization.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetMeasureName

Target measure name to be used. If not provided, the target measure name by default would be MeasureName if provided, or SourceColumn otherwise.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MeasureValueType

Type of the value that is to be read from SourceColumn. If the mapping is for MULTI, use MeasureValueType.MULTI.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>BIGINT</code> | <code>BOOLEAN</code> | <code>DOUBLE</code> | <code>VARCHAR</code> | <code>MULTI</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MultiMeasureAttributeMappings

_Required_: No

_Type_: List of <a href="multimeasureattributemapping.md">MultiMeasureAttributeMapping</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

