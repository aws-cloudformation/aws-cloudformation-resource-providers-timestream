# AWS::Timestream::Table

The AWS::Timestream::Table resource creates a Timestream Table.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Timestream::Table",
    "Properties" : {
        "<a href="#databasename" title="DatabaseName">DatabaseName</a>" : <i>String</i>,
        "<a href="#tablename" title="TableName">TableName</a>" : <i>String</i>,
        "<a href="#retentionproperties" title="RetentionProperties">RetentionProperties</a>" : <i><a href="retentionproperties.md">RetentionProperties</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Timestream::Table
Properties:
    <a href="#databasename" title="DatabaseName">DatabaseName</a>: <i>String</i>
    <a href="#tablename" title="TableName">TableName</a>: <i>String</i>
    <a href="#retentionproperties" title="RetentionProperties">RetentionProperties</a>: <i><a href="retentionproperties.md">RetentionProperties</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### DatabaseName

The name for the database which the table to be created belongs to.

_Required_: Yes

_Type_: String

_Pattern_: <code>^[a-zA-Z0-9_.-]{3,64}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### TableName

The name for the table. If you don't specify a name, AWS CloudFormation generates a unique physical ID and uses that ID for the table name.

_Required_: No

_Type_: String

_Pattern_: <code>^[a-zA-Z0-9_.-]{3,64}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

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

#### Name

The table name exposed as a read-only attribute.

