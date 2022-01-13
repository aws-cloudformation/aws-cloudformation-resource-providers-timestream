package software.amazon.timestream.table;

/**
 * Class for conversion between Timestream magnetic store writes properties model and what is defined in the resource provider package.
 *
 */
class MagneticStoreWritePropertiesModelConverter {
    public static com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties convert(MagneticStoreWriteProperties magneticStoreWriteProperties) {
        if (magneticStoreWriteProperties == null) {
            return null;
        }

        return new com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties()
                .withEnableMagneticStoreWrites(magneticStoreWriteProperties.getEnableMagneticStoreWrites())
                .withMagneticStoreRejectedDataLocation(MagneticStoreRejectedDataLocationModelConverter.convert(magneticStoreWriteProperties.getMagneticStoreRejectedDataLocation()));
    }

    public static MagneticStoreWriteProperties convert(com.amazonaws.services.timestreamwrite.model.MagneticStoreWriteProperties sdkModel) {
        if (sdkModel == null) {
            return null;
        }

        return MagneticStoreWriteProperties.builder()
                .enableMagneticStoreWrites(sdkModel.getEnableMagneticStoreWrites())
                .magneticStoreRejectedDataLocation(MagneticStoreRejectedDataLocationModelConverter.convert(sdkModel.getMagneticStoreRejectedDataLocation()))
                .build();
    }
}
