package software.amazon.timestream.table;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class RetentionPropertiesModelConverterTest {

    @Test
    void shouldConvertSDKModelFromUluruModel() {
        RetentionProperties uluruModel = RetentionProperties.builder()
                .memoryStoreRetentionPeriodInHours("1000")
                .magneticStoreRetentionPeriodInDays("2000")
                .build();
        com.amazonaws.services.timestreamwrite.model.RetentionProperties sdkModel =
                new com.amazonaws.services.timestreamwrite.model.RetentionProperties()
                        .withMemoryStoreRetentionPeriodInHours(1000L)
                        .withMagneticStoreRetentionPeriodInDays(2000L);

        assertThat(sdkModel).isEqualTo(RetentionPropertiesModelConverter.convert(uluruModel));
    }

    @Test
    void shouldConvertUluruModelFromSDKModel() {
        RetentionProperties uluruModel = RetentionProperties.builder()
                .memoryStoreRetentionPeriodInHours("1000")
                .magneticStoreRetentionPeriodInDays("2000")
                .build();
        com.amazonaws.services.timestreamwrite.model.RetentionProperties sdkModel =
                new com.amazonaws.services.timestreamwrite.model.RetentionProperties()
                        .withMemoryStoreRetentionPeriodInHours(1000L)
                        .withMagneticStoreRetentionPeriodInDays(2000L);

        assertThat(uluruModel).isEqualTo(RetentionPropertiesModelConverter.convert(sdkModel));
    }

    @Test
    void shouldConvertSDKModelFromUluruModelWithInputAsNull() {
        assertNull(RetentionPropertiesModelConverter.convert((RetentionProperties) null));
    }

    @Test
    void shouldConvertUluruModelFromSDKModelWithInputAsNull() {
        assertNull(RetentionPropertiesModelConverter.convert(
                (com.amazonaws.services.timestreamwrite.model.RetentionProperties) null));
    }

}
