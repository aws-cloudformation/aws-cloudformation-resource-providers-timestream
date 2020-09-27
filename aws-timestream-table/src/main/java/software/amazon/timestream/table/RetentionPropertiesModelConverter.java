package software.amazon.timestream.table;

/**
 * Class for conversion between Timestream retention policy model and what is defined in the resource provider package.
 *
 * Note in the resource provider's policy model, TTL is defined as String type due to Long type is missing, see issue
 * https://github.com/aws-cloudformation/cloudformation-cli-java-plugin/issues/151
 */
class RetentionPropertiesModelConverter {
    public static com.amazonaws.services.timestreamwrite.model.RetentionProperties convert(RetentionProperties retentionProperties) {
        if (retentionProperties == null) {
            return null;
        }

        return new com.amazonaws.services.timestreamwrite.model.RetentionProperties()
                .withMemoryStoreRetentionPeriodInHours(Long.parseLong(retentionProperties.getMemoryStoreRetentionPeriodInHours()))
                .withMagneticStoreRetentionPeriodInDays(Long.parseLong(retentionProperties.getMagneticStoreRetentionPeriodInDays()));
    }

    public static RetentionProperties convert(com.amazonaws.services.timestreamwrite.model.RetentionProperties sdkModel) {
        if (sdkModel == null) {
            return null;
        }

        return RetentionProperties.builder()
                .memoryStoreRetentionPeriodInHours(sdkModel.getMemoryStoreRetentionPeriodInHours().toString())
                .magneticStoreRetentionPeriodInDays(sdkModel.getMagneticStoreRetentionPeriodInDays().toString())
                .build();
    }
}
