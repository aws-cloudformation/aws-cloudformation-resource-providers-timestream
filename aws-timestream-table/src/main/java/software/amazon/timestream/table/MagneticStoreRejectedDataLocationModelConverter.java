package software.amazon.timestream.table;

/**
 * Class for conversion between Timestream magnetic store rejected data location model and what is defined in the resource provider package.
 *
 */
class MagneticStoreRejectedDataLocationModelConverter {
    public static com.amazonaws.services.timestreamwrite.model.MagneticStoreRejectedDataLocation convert(MagneticStoreRejectedDataLocation magneticStoreRejectedDataLocation) {
        if (magneticStoreRejectedDataLocation == null) {
            return null;
        }

        return new com.amazonaws.services.timestreamwrite.model.MagneticStoreRejectedDataLocation()
                .withS3Configuration(S3ConfigurationModelConverter.convert(magneticStoreRejectedDataLocation.getS3Configuration()));
    }

    public static MagneticStoreRejectedDataLocation convert(com.amazonaws.services.timestreamwrite.model.MagneticStoreRejectedDataLocation sdkModel) {
        if (sdkModel == null) {
            return null;
        }

        return MagneticStoreRejectedDataLocation.builder()
                .s3Configuration(S3ConfigurationModelConverter.convert(sdkModel.getS3Configuration()))
                .build();
    }
}
