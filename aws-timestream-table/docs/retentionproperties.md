# AWS::Timestream::Table RetentionProperties

The retention duration of the memory store and the magnetic store.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#memorystoreretentionperiodinhours" title="MemoryStoreRetentionPeriodInHours">MemoryStoreRetentionPeriodInHours</a>" : <i>String</i>,
    "<a href="#magneticstoreretentionperiodindays" title="MagneticStoreRetentionPeriodInDays">MagneticStoreRetentionPeriodInDays</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#memorystoreretentionperiodinhours" title="MemoryStoreRetentionPeriodInHours">MemoryStoreRetentionPeriodInHours</a>: <i>String</i>
<a href="#magneticstoreretentionperiodindays" title="MagneticStoreRetentionPeriodInDays">MagneticStoreRetentionPeriodInDays</a>: <i>String</i>
</pre>

## Properties

#### MemoryStoreRetentionPeriodInHours

The duration for which data must be stored in the memory store.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MagneticStoreRetentionPeriodInDays

The duration for which data must be stored in the magnetic store.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

