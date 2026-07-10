package net.aerh.slashcommands.internal.handlers;

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy-based fake for a reply callback that records every interaction with the reply and hook APIs.
 * Any method the production code is not expected to touch fails the test with
 * {@link UnsupportedOperationException}.
 */
final class RecordingInteraction {
    private final List<String> calls = new ArrayList<>();
    private final boolean acknowledged;

    RecordingInteraction(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    List<String> calls() {
        return calls;
    }

    IReplyCallback replyCallback() {
        return proxy(IReplyCallback.class, (proxyInstance, method, args) -> switch (method.getName()) {
            case "isAcknowledged" -> acknowledged;
            case "getHook" -> hook();
            case "reply" -> {
                calls.add("reply(" + args[0] + ")");
                yield chainRecordingAction(ReplyCallbackAction.class);
            }
            case "toString" -> "RecordingReplyCallback";
            default -> throw new UnsupportedOperationException("Unexpected call: " + method.getName());
        });
    }

    private InteractionHook hook() {
        return proxy(InteractionHook.class, (proxyInstance, method, args) -> switch (method.getName()) {
            case "editOriginal" -> {
                calls.add("editOriginal(" + args[0] + ")");
                yield chainRecordingAction(WebhookMessageEditAction.class);
            }
            case "sendMessage" -> {
                calls.add("sendMessage(" + args[0] + ")");
                yield chainRecordingAction(WebhookMessageCreateAction.class);
            }
            case "toString" -> "RecordingInteractionHook";
            default -> throw new UnsupportedOperationException("Unexpected call: " + method.getName());
        });
    }

    /**
     * Creates a rest action proxy that records {@code queue()} calls and records-then-chains any other
     * builder-style call (such as {@code setEphemeral}) by returning itself.
     */
    private <T> T chainRecordingAction(Class<T> type) {
        Object[] self = new Object[1];
        InvocationHandler handler = (proxyInstance, method, args) -> switch (method.getName()) {
            case "queue" -> {
                calls.add("queue");
                yield null;
            }
            case "toString" -> "Recording" + type.getSimpleName();
            case "hashCode" -> System.identityHashCode(proxyInstance);
            case "equals" -> proxyInstance == args[0];
            default -> {
                calls.add(method.getName() + (args != null && args.length > 0 ? "(" + args[0] + ")" : ""));
                yield self[0];
            }
        };
        self[0] = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
        return type.cast(self[0]);
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
