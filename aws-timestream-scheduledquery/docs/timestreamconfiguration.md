# AWS::Timestream::ScheduledQuery TimestreamConfiguration

Configuration needed to write data into the Timestream database and table.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#databasename" title="DatabaseName">DatabaseName</a>" : <i>String</i>,
    "<a href="#tablename" title="TableName">TableName</a>" : <i>String</i>,
    "<a href="#timecolumn" title="TimeColumn">TimeColumn</a>" : <i>String</i>,
    "<a href="#dimensionmappings" title="DimensionMappings">DimensionMappings</a>" : <i>[ <a href="dimensionmapping.md">DimensionMapping</a>, ... ]</i>,
    "<a href="#multimeasuremappings" title="MultiMeasureMappings">MultiMeasureMappings</a>" : <i><a href="multimeasuremappings.md">MultiMeasureMappings</a></i>,
    "<a href="#mixedmeasuremappings" title="MixedMeasureMappings">MixedMeasureMappings</a>" : <i>[ <a href="mixedmeasuremapping.md">MixedMeasureMapping</a>, ... ]</i>,
    "<a href="#measurenamecolumn" title="MeasureNameColumn">MeasureNameColumn</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#databasename" title="DatabaseName">DatabaseName</a>: <i>String</i>
<a href="#tablename" title="TableName">TableName</a>: <i>String</i>
<a href="#timecolumn" title="TimeColumn">TimeColumn</a>: <i>String</i>
<a href="#dimensionmappings" title="DimensionMappings">DimensionMappings</a>: <i>
      - <a href="dimensionmapping.md">DimensionMapping</a></i>
<a href="#multimeasuremappings" title="MultiMeasureMappings">MultiMeasureMappings</a>: <i><a href="multimeasuremappings.md">MultiMeasureMappings</a></i>
<a href="#mixedmeasuremappings" title="MixedMeasureMappings">MixedMeasureMappings</a>: <i>
      - <a href="mixedmeasuremapping.md">MixedMeasureMapping</a></i>
<a href="#measurenamecolumn" title="MeasureNameColumn">MeasureNameColumn</a>: <i>String</i>
</pre>

## Properties

#### DatabaseName

Name of Timestream database to which the query result will be written.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TableName

Name of Timestream table that the query result will be written to. The table should be within the same database that is provided in Timestream configuration.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TimeColumn

Column from query result that should be used as the time column in destination table. Column type for this should be TIMESTAMP.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DimensionMappings

This is to allow mapping column(s) from the query result to the dimension in the destination table.

_Required_: Yes

_Type_: List of <a href="dimensionmapping.md">DimensionMapping</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MultiMeasureMappings

Only one of MixedMeasureMappings or MultiMeasureMappings is to be provided. MultiMeasureMappings can be used to ingest data as multi measures in the derived table.

_Required_: No

_Type_: <a href="multimeasuremappings.md">MultiMeasureMappings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MixedMeasureMappings

Specifies how to map measures to multi-measure records.

_Required_: No

_Type_: List of <a href="mixedmeasuremapping.md">MixedMeasureMapping</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MeasureNameColumn

Name of the measure name column from the query result.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

