package software.amazon.timestream.scheduledquery;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.Builder(toBuilder = true)
public class CallbackContext extends StdCallbackContext {
    private boolean deleteScheduledQueryStarted;
}
