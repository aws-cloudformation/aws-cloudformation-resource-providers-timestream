# AWS::Timestream::ScheduledQuery MultiMeasureMappings

Only one of MixedMeasureMappings or MultiMeasureMappings is to be provided. MultiMeasureMappings can be used to ingest data as multi measures in the derived table.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#targetmultimeasurename" title="TargetMultiMeasureName">TargetMultiMeasureName</a>" : <i>String</i>,
    "<a href="#multimeasureattributemappings" title="MultiMeasureAttributeMappings">MultiMeasureAttributeMappings</a>" : <i>[ <a href="multimeasureattributemapping.md">MultiMeasureAttributeMapping</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#targetmultimeasurename" title="TargetMultiMeasureName">TargetMultiMeasureName</a>: <i>String</i>
<a href="#multimeasureattributemappings" title="MultiMeasureAttributeMappings">MultiMeasureAttributeMappings</a>: <i>
      - <a href="multimeasureattributemapping.md">MultiMeasureAttributeMapping</a></i>
</pre>

## Properties

#### TargetMultiMeasureName

Name of the target multi-measure in the derived table. Required if MeasureNameColumn is not provided. If MeasureNameColumn is provided then the value from that column will be used as the multi-measure name.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MultiMeasureAttributeMappings

Required. Attribute mappings to be used for mapping query results to ingest data for multi-measure attributes.

_Required_: Yes

_Type_: List of <a href="multimeasureattributemapping.md">MultiMeasureAttributeMapping</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

