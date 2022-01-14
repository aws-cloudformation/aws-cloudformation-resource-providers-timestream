# AWS::Timestream::Table MagneticStoreWriteProperties

The properties that determine whether magnetic store writes are enabled.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#enablemagneticstorewrites" title="EnableMagneticStoreWrites">EnableMagneticStoreWrites</a>" : <i>Boolean</i>,
    "<a href="#magneticstorerejecteddatalocation" title="MagneticStoreRejectedDataLocation">MagneticStoreRejectedDataLocation</a>" : <i><a href="magneticstorewriteproperties.md">MagneticStoreWriteProperties</a></i>
}
</pre>

### YAML

<pre>
<a href="#enablemagneticstorewrites" title="EnableMagneticStoreWrites">EnableMagneticStoreWrites</a>: <i>Boolean</i>
<a href="#magneticstorerejecteddatalocation" title="MagneticStoreRejectedDataLocation">MagneticStoreRejectedDataLocation</a>: <i><a href="magneticstorewriteproperties.md">MagneticStoreWriteProperties</a></i>
</pre>

## Properties

#### EnableMagneticStoreWrites

Boolean flag indicating whether magnetic store writes are enabled.

_Required_: Yes

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MagneticStoreRejectedDataLocation

_Required_: No

_Type_: <a href="magneticstorewriteproperties.md">MagneticStoreWriteProperties</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

