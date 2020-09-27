package software.amazon.timestream.table;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@Data
@Builder(builderClassName = "CallbackContextBuilder")
@JsonDeserialize(builder = CallbackContext.CallbackContextBuilder.class)
public class CallbackContext {
    private boolean deleteTableStarted;
    private boolean deleteTableStabilized;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CallbackContextBuilder {
    }
}