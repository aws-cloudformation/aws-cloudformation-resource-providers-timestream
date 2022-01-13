package software.amazon.timestream.table;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class MagneticStoreWritePropertiesModelConverterTest {

    @Test
    void shouldConvertSDKModelFromUluruModel() {
        S3Configuration uluruS3configuration = S3Configuration.builder()
                .bucketName("BucketName")
                .objectKeyPrefix("ObjectKeyPrefix")
                .encryptionOption("EncryptionOption")
                .kmsKeyId("KmsKeyId")
                .build();

        MagneticStoreRejectedDataLocation uluruRejectedDataLocation = MagneticStoreRejectedDataLocation.builder()
                .s3Configuration(uluruS3configuration)
                .build();

        MagneticStoreWriteProperties uluruModel = MagneticStoreWriteProperties.builder()
                .enableMagneticStoreWrites(true)
                .magneticStoreRejectedDataLocation(uluruRejectedDataLocation)
                .build();

        com.amazonaws.services.timestreamwrite.model.S3Configuration sdkS3ConfigurationModel =
            new com.amazonaws.services.timestreamwrite.model.S3Configuration()
                    .withBucketName("BucketName")
                    .withObjectKeyPrefix("ObjectKeyPrefix")
                    .withEncryptionOption("EncryptionOption")
                    .withKmsKeyId("KmsKeyId");

        com.amazonaws.services.timestreamwrite.model.MagneticStoreRejectedDataLocation sdkDataLocationModel =
                new com.amazonaws.services.timestreamwrite.model.MagneticStoreRejectedDataLocation()
                    .withS3Configuration(sdkS3ConfigurationModel);

        com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties sdkModel =
                new com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties()
                        .withEnableMagneticStoreWrites(true)
                        .withMagneticStoreRejectedDataLocation(sdkDataLocationModel);

        assertThat(sdkModel).isEqualTo(MagneticStoreWritePropertiesModelConverter.convert(uluruModel));
    }

    @Test
    void shouldConvertUluruModelFromSDKModel() {
        S3Configuration uluruS3configuration = S3Configuration.builder()
                .bucketName("BucketName")
                .objectKeyPrefix("ObjectKeyPrefix")
                .encryptionOption("EncryptionOption")
                .kmsKeyId("KmsKeyId")
                .build();

        MagneticStoreRejectedDataLocation uluruRejectedDataLocation = MagneticStoreRejectedDataLocation.builder()
                .s3Configuration(uluruS3configuration)
                .build();

        MagneticStoreWriteProperties uluruModel = MagneticStoreWriteProperties.builder()
                .enableMagneticStoreWrites(true)
                .magneticStoreRejectedDataLocation(uluruRejectedDataLocation)
                .build();

        com.amazonaws.services.timestreamwrite.model.S3Configuration sdkS3ConfigurationModel =
                new com.amazonaws.services.timestreamwrite.model.S3Configuration()
                        .withBucketName("BucketName")
                        .withObjectKeyPrefix("ObjectKeyPrefix")
                        .withEncryptionOption("EncryptionOption")
                        .withKmsKeyId("KmsKeyId");

        com.amazonaws.services.timestreamwrite.model.MagneticStoreRejectedDataLocation sdkDataLocationModel =
                new com.amazonaws.services.timestreamwrite.model.MagneticStoreRejectedDataLocation()
                        .withS3Configuration(sdkS3ConfigurationModel);

        com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties sdkModel =
                new com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties()
                        .withEnableMagneticStoreWrites(true)
                        .withMagneticStoreRejectedDataLocation(sdkDataLocationModel);

        assertThat(uluruModel).isEqualTo(MagneticStoreWritePropertiesModelConverter.convert(sdkModel));
    }

    @Test
    void shouldConvertSDKModelFromUluruModelWithInputAsNull() {
        assertNull(MagneticStoreWritePropertiesModelConverter.convert((MagneticStoreWriteProperties) null));
    }

    @Test
    void shouldConvertUluruModelFromSDKModelWithInputAsNull() {
        assertNull(MagneticStoreWritePropertiesModelConverter.convert(
                (com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties) null));
    }

    @Test
    void shouldConvertSDKLocationModelFromUluruModelWithInputAsNull() {
        assertNull(MagneticStoreRejectedDataLocationModelConverter.convert((MagneticStoreRejectedDataLocation) null));
    }

    @Test
    void shouldConvertUluruLocationModelFromSDKModelWithInputAsNull() {
        assertNull(MagneticStoreRejectedDataLocationModelConverter.convert(
                (com.amazonaws.services.timestreamwrite.model.MagneticStoreRejectedDataLocation) null));
    }

    @Test
    void shouldConvertSDKS3ConfigurationModelFromUluruModelWithInputAsNull() {
        assertNull(S3ConfigurationModelConverter.convert((S3Configuration) null));
    }

    @Test
    void shouldConvertUluruS3ConfigurationModelFromSDKModelWithInputAsNull() {
        assertNull(S3ConfigurationModelConverter.convert(
                (com.amazonaws.services.timestreamwrite.model.S3Configuration) null));
    }

}