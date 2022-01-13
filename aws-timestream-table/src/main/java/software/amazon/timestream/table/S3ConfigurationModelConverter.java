package software.amazon.timestream.table;

/**
 * Class for conversion between Timestream magnetic store rejected s3 configuration model and what is defined in the resource provider package.
 *
 */
class S3ConfigurationModelConverter {
    public static com.amazonaws.services.timestreamwrite.model.S3Configuration convert(S3Configuration s3Configuration) {
        if (s3Configuration == null) {
            return null;
        }

        return new com.amazonaws.services.timestreamwrite.model.S3Configuration()
                .withBucketName(s3Configuration.getBucketName())
                .withObjectKeyPrefix(s3Configuration.getObjectKeyPrefix())
                .withEncryptionOption(s3Configuration.getEncryptionOption())
                .withKmsKeyId(s3Configuration.getKmsKeyId());
    }

    public static S3Configuration convert(com.amazonaws.services.timestreamwrite.model.S3Configuration sdkModel) {
        if (sdkModel == null) {
            return null;
        }

        return S3Configuration.builder()
                .bucketName(sdkModel.getBucketName())
                .objectKeyPrefix(sdkModel.getObjectKeyPrefix())
                .encryptionOption(sdkModel.getEncryptionOption())
                .kmsKeyId(sdkModel.getKmsKeyId())
                .build();
    }
}
