# AWS::Timestream::ScheduledQuery S3Configuration

Details on S3 location for error reports that result from running a query.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#bucketname" title="BucketName">BucketName</a>" : <i>String</i>,
    "<a href="#objectkeyprefix" title="ObjectKeyPrefix">ObjectKeyPrefix</a>" : <i>String</i>,
    "<a href="#encryptionoption" title="EncryptionOption">EncryptionOption</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#bucketname" title="BucketName">BucketName</a>: <i>String</i>
<a href="#objectkeyprefix" title="ObjectKeyPrefix">ObjectKeyPrefix</a>: <i>String</i>
<a href="#encryptionoption" title="EncryptionOption">EncryptionOption</a>: <i>String</i>
</pre>

## Properties

#### BucketName

Name of the S3 bucket under which error reports will be created.

_Required_: Yes

_Type_: String

_Minimum_: <code>3</code>

_Maximum_: <code>63</code>

_Pattern_: <code>[a-z0-9][\.\-a-z0-9]{1,61}[a-z0-9]</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ObjectKeyPrefix

Prefix for error report keys.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>896</code>

_Pattern_: <code>[a-zA-Z0-9|!\-_*'\(\)]([a-zA-Z0-9]|[!\-_*'\(\)\/.])+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EncryptionOption

Encryption at rest options for the error reports. If no encryption option is specified, Timestream will choose SSE_S3 as default.

_Required_: No

_Type_: String

_Allowed Values_: <code>SSE_S3</code> | <code>SSE_KMS</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

