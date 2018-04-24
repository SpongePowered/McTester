package pw.aaron1011.mctester.framework;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.Message;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.OneShotEventListener;
import pw.aaron1011.mctester.framework.proxy.InvocationData;
import pw.aaron1011.mctester.message.toserver.MessageAck;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TesterHandler {

    volatile ConcurrentLinkedQueue<InvocationData> invokeQueue = new ConcurrentLinkedQueue<>();
    volatile ConcurrentLinkedQueue<MessageAck> ackQueue = new ConcurrentLinkedQueue<>();
    volatile ConcurrentLinkedQueue<OneShotEventListener> listeners = new ConcurrentLinkedQueue<>();
    volatile ConcurrentLinkedQueue<Message> outboundMessages = new ConcurrentLinkedQueue<>();

    public static volatile Object lock = new Object();

    public void receiveAck(MessageAck ack) {
        this.ackQueue.add(ack);
    }

    public <T> void addOneShot(OneShotEventListener<T> listener) {
        this.listeners.add(listener);
    }

    public void addOutbound(Message message) {
        this.outboundMessages.add(message);
    }


    public void run() {
        Sponge.getScheduler().createTaskBuilder().name("Proxy executor").intervalTicks(1).execute((task) -> {


            // First, check any listeners that finished
            MessageAck ack = ackQueue.poll();
            if (ack != null) {
                for (OneShotEventListener listener: listeners) {
                    if (!listener.handled) {
                        McTester.INSTANCE.logger.error("One shot listener didn't run: " + listener.listener);
                    }
                    Sponge.getEventManager().unregisterListeners(listener);
                }
                listeners.clear();
                TesterHandler.lock.notify();
            }

            Message outbound = outboundMessages.poll();
            if (outbound != null) {
                McTester.INSTANCE.channel.sendTo(McTester.getThePlayer(), outbound);
            }

            // Next, handle the latest method invocation, and wake up the thread
            InvocationData data = invokeQueue.poll();
            if (data != null) {
                try {
                    data.execute();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                TesterHandler.lock.notify();
            }


        }).submit(McTester.INSTANCE);
    }

}
