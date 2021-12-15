package software.amazon.timestream.scheduledquery;

import com.amazonaws.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for conversion between Timestream Scheduled Query configurations and what is defined in the resource provider package.
 */
public class ScheduledQueryModelConverter {

    public static com.amazonaws.services.timestreamquery.model.ScheduleConfiguration convertToTimestreamScheduleConfiguration(
            ScheduleConfiguration scheduleConfiguration) {

        if (scheduleConfiguration == null) {
            return null;
        }

        return new com.amazonaws.services.timestreamquery.model.ScheduleConfiguration()
                .withScheduleExpression(scheduleConfiguration.getScheduleExpression());
    }

    public static ScheduleConfiguration convertToModelScheduleConfiguration(
            com.amazonaws.services.timestreamquery.model.ScheduleConfiguration scheduleConfiguration) {

        if (scheduleConfiguration == null) {
            return null;
        }

        return ScheduleConfiguration.builder()
                .scheduleExpression(scheduleConfiguration.getScheduleExpression())
                .build();
    }

    public static com.amazonaws.services.timestreamquery.model.NotificationConfiguration convertToTimestreamNotificationConfiguration(
            NotificationConfiguration notificationConfiguration) {

        if (notificationConfiguration == null) {
            return null;
        }

        return new com.amazonaws.services.timestreamquery.model.NotificationConfiguration()
                .withSnsConfiguration(new com.amazonaws.services.timestreamquery.model.SnsConfiguration()
                        .withTopicArn(notificationConfiguration
                                .getSnsConfiguration()
                                .getTopicArn()));
    }

    public static NotificationConfiguration convertToModelNotificationConfiguration(
            com.amazonaws.services.timestreamquery.model.NotificationConfiguration notificationConfiguration) {

        if (notificationConfiguration == null) {
            return null;
        }

        return NotificationConfiguration.builder()
                .snsConfiguration(SnsConfiguration.builder()
                        .topicArn(notificationConfiguration
                                .getSnsConfiguration()
                                .getTopicArn())
                        .build())
                .build();
    }

    public static com.amazonaws.services.timestreamquery.model.TargetConfiguration convertToTimestreamTargetConfiguration(
            TargetConfiguration targetConfiguration) {

        if (targetConfiguration == null) {
            return null;
        }

        TimestreamConfiguration timestreamConfiguration = targetConfiguration.getTimestreamConfiguration();

        com.amazonaws.services.timestreamquery.model.TargetConfiguration timestreamTargetConfiguration =
                new com.amazonaws.services.timestreamquery.model.TargetConfiguration()
                        .withTimestreamConfiguration(new com.amazonaws.services.timestreamquery.model.TimestreamConfiguration()
                                .withDatabaseName(timestreamConfiguration.getDatabaseName())
                                .withTableName(timestreamConfiguration.getTableName())
                                .withTimeColumn(timestreamConfiguration.getTimeColumn())
                                .withDimensionMappings(convertToTimestreamDimensionMappings(timestreamConfiguration.getDimensionMappings())));

        final MultiMeasureMappings multiMeasureMappings = timestreamConfiguration.getMultiMeasureMappings();
        if (multiMeasureMappings != null) {
            timestreamTargetConfiguration.getTimestreamConfiguration().withMultiMeasureMappings(
                    convertToTimestreamMultiMeasureMappings(multiMeasureMappings));
        }

        final List<MixedMeasureMapping> mixedMeasureMappings = timestreamConfiguration.getMixedMeasureMappings();
        if (mixedMeasureMappings != null && !mixedMeasureMappings.isEmpty()) {
            timestreamTargetConfiguration.getTimestreamConfiguration().withMixedMeasureMappings(
                    convertToTimestreamMixedMeasureMappings(mixedMeasureMappings));
        }

        if (!StringUtils.isNullOrEmpty(timestreamConfiguration.getMeasureNameColumn())) {
            timestreamTargetConfiguration.getTimestreamConfiguration()
                    .withMeasureNameColumn(timestreamConfiguration.getMeasureNameColumn());
        }

        return timestreamTargetConfiguration;
    }

    public static TargetConfiguration convertToModelTargetConfiguration(
            com.amazonaws.services.timestreamquery.model.TargetConfiguration targetConfiguration) {

        if (targetConfiguration == null) {
            return null;
        }

        com.amazonaws.services.timestreamquery.model.TimestreamConfiguration timestreamConfiguration = targetConfiguration.getTimestreamConfiguration();

        TimestreamConfiguration.TimestreamConfigurationBuilder modelTimestreamConfigurationBuilder =
                TimestreamConfiguration.builder()
                        .databaseName(timestreamConfiguration.getDatabaseName())
                        .tableName(timestreamConfiguration.getTableName())
                        .timeColumn(timestreamConfiguration.getTimeColumn())
                        .dimensionMappings(convertToModelDimensionMappings(timestreamConfiguration.getDimensionMappings()));

        final com.amazonaws.services.timestreamquery.model.MultiMeasureMappings multiMeasureMappings = timestreamConfiguration.getMultiMeasureMappings();
        if (multiMeasureMappings != null) {
            modelTimestreamConfigurationBuilder
                    .multiMeasureMappings(convertToModelMultiMeasureMappings(multiMeasureMappings));
        }

        final List<com.amazonaws.services.timestreamquery.model.MixedMeasureMapping> mixedMeasureMappings = timestreamConfiguration.getMixedMeasureMappings();
        if (mixedMeasureMappings != null && !mixedMeasureMappings.isEmpty()) {
            modelTimestreamConfigurationBuilder
                    .mixedMeasureMappings(convertToModelMixedMeasureMappings(mixedMeasureMappings));
        }

        if (!StringUtils.isNullOrEmpty(timestreamConfiguration.getMeasureNameColumn())) {
            modelTimestreamConfigurationBuilder
                    .measureNameColumn(timestreamConfiguration.getMeasureNameColumn());
        }

        return TargetConfiguration.builder()
                .timestreamConfiguration(modelTimestreamConfigurationBuilder.build())
                .build();
    }

    public static List<com.amazonaws.services.timestreamquery.model.DimensionMapping> convertToTimestreamDimensionMappings(
            List<DimensionMapping> dimensionMappings) {

        if (dimensionMappings == null || dimensionMappings.isEmpty()) {
            return null;
        }

        return dimensionMappings.stream()
                .map(dm -> new com.amazonaws.services.timestreamquery.model.DimensionMapping()
                        .withName(dm.getName())
                        .withDimensionValueType(dm.getDimensionValueType()))
                .collect(Collectors.toList());
    }

    public static List<DimensionMapping> convertToModelDimensionMappings(
            List<com.amazonaws.services.timestreamquery.model.DimensionMapping> dimensionMappings) {

        if (dimensionMappings == null || dimensionMappings.isEmpty()) {
            return null;
        }

        return dimensionMappings.stream()
                .map(dm -> DimensionMapping.builder()
                        .name(dm.getName())
                        .dimensionValueType(dm.getDimensionValueType())
                        .build())
                .collect(Collectors.toList());
    }

    public static com.amazonaws.services.timestreamquery.model.MultiMeasureMappings convertToTimestreamMultiMeasureMappings(
            MultiMeasureMappings multiMeasureMappings) {

        if (multiMeasureMappings == null) {
            return null;
        }

        com.amazonaws.services.timestreamquery.model.MultiMeasureMappings timestreamMultiMeasureMapping =
                new com.amazonaws.services.timestreamquery.model.MultiMeasureMappings()
                        .withMultiMeasureAttributeMappings(convertToTimestreamMultiMeasureAttributeMappings(multiMeasureMappings.getMultiMeasureAttributeMappings()));

        if (!StringUtils.isNullOrEmpty(multiMeasureMappings.getTargetMultiMeasureName())) {
            timestreamMultiMeasureMapping
                    .withTargetMultiMeasureName(multiMeasureMappings.getTargetMultiMeasureName());
        }

        return timestreamMultiMeasureMapping;
    }

    public static MultiMeasureMappings convertToModelMultiMeasureMappings(
            com.amazonaws.services.timestreamquery.model.MultiMeasureMappings multiMeasureMappings) {

        if (multiMeasureMappings == null) {
            return null;
        }

        MultiMeasureMappings.MultiMeasureMappingsBuilder multiMeasureMappingsBuilder = MultiMeasureMappings.builder()
                .multiMeasureAttributeMappings(
                        convertToModelMultiMeasureAttributeMappings(
                                multiMeasureMappings.getMultiMeasureAttributeMappings()));

        if (!StringUtils.isNullOrEmpty(multiMeasureMappings.getTargetMultiMeasureName())) {
            multiMeasureMappingsBuilder
                    .targetMultiMeasureName(multiMeasureMappings.getTargetMultiMeasureName());
        }

        return multiMeasureMappingsBuilder.build();
    }

    public static com.amazonaws.services.timestreamquery.model.MultiMeasureAttributeMapping convertToTimestreamMultiMeasureAttributeMapping(
            MultiMeasureAttributeMapping multiMeasureAttributeMapping) {

        if (multiMeasureAttributeMapping == null) {
            return null;
        }

        com.amazonaws.services.timestreamquery.model.MultiMeasureAttributeMapping timestreamMultiMeasureAttributeMapping =
                new com.amazonaws.services.timestreamquery.model.MultiMeasureAttributeMapping()
                        .withSourceColumn(multiMeasureAttributeMapping.getSourceColumn())
                        .withMeasureValueType(multiMeasureAttributeMapping.getMeasureValueType());

        if (!StringUtils.isNullOrEmpty(multiMeasureAttributeMapping.getTargetMultiMeasureAttributeName())) {
            timestreamMultiMeasureAttributeMapping
                    .withTargetMultiMeasureAttributeName(multiMeasureAttributeMapping.getTargetMultiMeasureAttributeName());
        }

        return timestreamMultiMeasureAttributeMapping;
    }

    public static MultiMeasureAttributeMapping convertToModelMultiMeasureAttributeMapping(
            com.amazonaws.services.timestreamquery.model.MultiMeasureAttributeMapping multiMeasureAttributeMapping) {

        if (multiMeasureAttributeMapping == null) {
            return null;
        }

        MultiMeasureAttributeMapping.MultiMeasureAttributeMappingBuilder multiMeasureAttributeMappingBuilder =
                MultiMeasureAttributeMapping.builder()
                        .sourceColumn(multiMeasureAttributeMapping.getSourceColumn())
                        .measureValueType(multiMeasureAttributeMapping.getMeasureValueType());

        if (!StringUtils.isNullOrEmpty(multiMeasureAttributeMapping.getTargetMultiMeasureAttributeName())) {
            multiMeasureAttributeMappingBuilder
                    .targetMultiMeasureAttributeName(multiMeasureAttributeMapping.getTargetMultiMeasureAttributeName());
        }

        return multiMeasureAttributeMappingBuilder.build();
    }

    public static List<com.amazonaws.services.timestreamquery.model.MultiMeasureAttributeMapping> convertToTimestreamMultiMeasureAttributeMappings(
            List<MultiMeasureAttributeMapping> multiMeasureAttributeMappings) {

        if (multiMeasureAttributeMappings == null || multiMeasureAttributeMappings.isEmpty()) {
            return null;
        }

        return multiMeasureAttributeMappings.stream()
                .map(mmam -> convertToTimestreamMultiMeasureAttributeMapping(mmam))
                .collect(Collectors.toList());
    }

    public static List<MultiMeasureAttributeMapping> convertToModelMultiMeasureAttributeMappings(
            List<com.amazonaws.services.timestreamquery.model.MultiMeasureAttributeMapping> multiMeasureAttributeMappings) {

        if (multiMeasureAttributeMappings == null || multiMeasureAttributeMappings.isEmpty()) {
            return null;
        }

        return multiMeasureAttributeMappings.stream()
                .map(mmam -> convertToModelMultiMeasureAttributeMapping(mmam))
                .collect(Collectors.toList());
    }

    public static com.amazonaws.services.timestreamquery.model.MixedMeasureMapping convertToTimestreamMixedMeasureMapping(
            MixedMeasureMapping mixedMeasureMapping) {

        if (mixedMeasureMapping == null) {
            return null;
        }

        com.amazonaws.services.timestreamquery.model.MixedMeasureMapping timestreamMixedMeasureMapping =
                new com.amazonaws.services.timestreamquery.model.MixedMeasureMapping()
                        .withMeasureValueType(mixedMeasureMapping.getMeasureValueType());

        if (!StringUtils.isNullOrEmpty(mixedMeasureMapping.getMeasureName())) {
            timestreamMixedMeasureMapping
                    .withMeasureName(mixedMeasureMapping.getMeasureName());
        }

        if (!StringUtils.isNullOrEmpty(mixedMeasureMapping.getSourceColumn())) {
            timestreamMixedMeasureMapping
                    .withSourceColumn(mixedMeasureMapping.getSourceColumn());
        }

        if (!StringUtils.isNullOrEmpty(mixedMeasureMapping.getTargetMeasureName())) {
            timestreamMixedMeasureMapping
                    .withTargetMeasureName(mixedMeasureMapping.getTargetMeasureName());
        }

        final List<MultiMeasureAttributeMapping> multiMeasureAttributeMappings =
                mixedMeasureMapping.getMultiMeasureAttributeMappings();
        if (multiMeasureAttributeMappings != null && !multiMeasureAttributeMappings.isEmpty()) {
            timestreamMixedMeasureMapping
                    .withMultiMeasureAttributeMappings(
                            convertToTimestreamMultiMeasureAttributeMappings(multiMeasureAttributeMappings));
        }

        return timestreamMixedMeasureMapping;
    }

    public static MixedMeasureMapping convertToModelMixedMeasureMapping(
            com.amazonaws.services.timestreamquery.model.MixedMeasureMapping mixedMeasureMapping) {

        if (mixedMeasureMapping == null) {
            return null;
        }

        MixedMeasureMapping.MixedMeasureMappingBuilder mixedMeasureMappingBuilder =
                MixedMeasureMapping.builder()
                        .measureValueType(mixedMeasureMapping.getMeasureValueType());

        if (!StringUtils.isNullOrEmpty(mixedMeasureMapping.getMeasureName())) {
            mixedMeasureMappingBuilder
                    .measureName(mixedMeasureMapping.getMeasureName());
        }

        if (!StringUtils.isNullOrEmpty(mixedMeasureMapping.getSourceColumn())) {
            mixedMeasureMappingBuilder
                    .sourceColumn(mixedMeasureMapping.getSourceColumn());
        }

        if (!StringUtils.isNullOrEmpty(mixedMeasureMapping.getTargetMeasureName())) {
            mixedMeasureMappingBuilder
                    .targetMeasureName(mixedMeasureMapping.getTargetMeasureName());
        }

        final List<com.amazonaws.services.timestreamquery.model.MultiMeasureAttributeMapping> multiMeasureAttributeMappings
                = mixedMeasureMapping.getMultiMeasureAttributeMappings();
        if (multiMeasureAttributeMappings != null && !multiMeasureAttributeMappings.isEmpty()) {
            mixedMeasureMappingBuilder
                    .multiMeasureAttributeMappings(
                            convertToModelMultiMeasureAttributeMappings(multiMeasureAttributeMappings));
        }

        return mixedMeasureMappingBuilder.build();
    }

    public static List<com.amazonaws.services.timestreamquery.model.MixedMeasureMapping> convertToTimestreamMixedMeasureMappings(
            List<MixedMeasureMapping> mixedMeasureMappings) {

        if (mixedMeasureMappings == null || mixedMeasureMappings.isEmpty()) {
            return null;
        }

        return mixedMeasureMappings.stream()
                .map(mmm -> convertToTimestreamMixedMeasureMapping(mmm))
                .collect(Collectors.toList());
    }

    public static List<MixedMeasureMapping> convertToModelMixedMeasureMappings(
            List<com.amazonaws.services.timestreamquery.model.MixedMeasureMapping> mixedMeasureMappings) {

        if (mixedMeasureMappings == null || mixedMeasureMappings.isEmpty()) {
            return null;
        }

        return mixedMeasureMappings.stream()
                .map(mmm -> convertToModelMixedMeasureMapping(mmm))
                .collect(Collectors.toList());
    }

    public static com.amazonaws.services.timestreamquery.model.ErrorReportConfiguration convertToTimestreamErrorReportConfiguration(
            ErrorReportConfiguration errorReportConfiguration) {

        if (errorReportConfiguration == null) {
            return null;
        }

        S3Configuration s3Configuration = errorReportConfiguration.getS3Configuration();

        com.amazonaws.services.timestreamquery.model.S3Configuration timestreamS3Configuration =
                new com.amazonaws.services.timestreamquery.model.S3Configuration()
                        .withBucketName(s3Configuration.getBucketName());

        if (!StringUtils.isNullOrEmpty(s3Configuration.getObjectKeyPrefix())) {
            timestreamS3Configuration
                    .withObjectKeyPrefix(s3Configuration.getObjectKeyPrefix());
        }

        if (!StringUtils.isNullOrEmpty(s3Configuration.getEncryptionOption())) {
            timestreamS3Configuration
                    .withEncryptionOption(s3Configuration.getEncryptionOption());
        }

        return new com.amazonaws.services.timestreamquery.model.ErrorReportConfiguration()
                .withS3Configuration(timestreamS3Configuration);
    }

    public static ErrorReportConfiguration convertToModelErrorReportConfiguration(
            com.amazonaws.services.timestreamquery.model.ErrorReportConfiguration errorReportConfiguration) {

        if (errorReportConfiguration == null) {
            return null;
        }

        com.amazonaws.services.timestreamquery.model.S3Configuration s3Configuration = errorReportConfiguration.getS3Configuration();

        S3Configuration.S3ConfigurationBuilder s3ConfigurationBuilder = S3Configuration.builder()
                .bucketName(s3Configuration.getBucketName());

        if (!StringUtils.isNullOrEmpty(s3Configuration.getObjectKeyPrefix())) {
            s3ConfigurationBuilder
                    .objectKeyPrefix(s3Configuration.getObjectKeyPrefix());
        }

        if (!StringUtils.isNullOrEmpty(s3Configuration.getEncryptionOption())) {
            s3ConfigurationBuilder
                    .encryptionOption(s3Configuration.getEncryptionOption());
        }

        return ErrorReportConfiguration.builder()
                .s3Configuration(s3ConfigurationBuilder.build())
                .build();
    }

    public static com.amazonaws.services.timestreamquery.model.Tag convertToTimestreamTag(
            Tag tag) {

        if (tag == null) {
            return null;
        }

        return new com.amazonaws.services.timestreamquery.model.Tag()
                .withKey(tag.getKey())
                .withValue(tag.getValue());
    }

    public static Tag convertToModelTag(
            com.amazonaws.services.timestreamquery.model.Tag tag) {

        if (tag == null) {
            return null;
        }

        return Tag.builder()
                .key(tag.getKey())
                .value(tag.getValue())
                .build();
    }

    public static List<com.amazonaws.services.timestreamquery.model.Tag> convertToTimestreamTags(
            List<Tag> tags) {

        if (tags == null || tags.isEmpty()) {
            return null;
        }

        return tags.stream()
                .map(t -> convertToTimestreamTag(t))
                .collect(Collectors.toList());
    }

    public static List<com.amazonaws.services.timestreamquery.model.Tag> convertToTimestreamTags(
            Set<Tag> tags) {

        if (tags == null || tags.isEmpty()) {
            return null;
        }

        return tags.stream()
                .map(t -> convertToTimestreamTag(t))
                .collect(Collectors.toList());
    }

    public static List<Tag> convertToModelTags(
            List<com.amazonaws.services.timestreamquery.model.Tag> tags) {

        if (tags == null || tags.isEmpty()) {
            return null;
        }

        return tags.stream()
                .map(t -> convertToModelTag(t))
                .collect(Collectors.toList());
    }
}
