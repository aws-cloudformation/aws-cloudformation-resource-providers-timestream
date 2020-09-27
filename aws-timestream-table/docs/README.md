# AWS::Timestream::Table

The AWS::Timestream::Table resource creates a Timestream Table.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Timestream::Table",
    "Properties" : {
        "<a href="#retentionproperties" title="RetentionProperties">RetentionProperties</a>" : <i><a href="retentionproperties.md">RetentionProperties</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Timestream::Table
Properties:
    <a href="#retentionproperties" title="RetentionProperties">RetentionProperties</a>: <i><a href="retentionproperties.md">RetentionProperties</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### RetentionProperties

The retention duration of the memory store and the magnetic store.

_Required_: No

_Type_: <a href="retentionproperties.md">RetentionProperties</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.

#### DatabaseName

The name for the database which the table to be created belongs to.

#### TableName

The name for the table. If you don't specify a name, AWS CloudFormation generates a unique physical ID and uses that ID for the table name.

