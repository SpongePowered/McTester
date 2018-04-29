package pw.aaron1011.mctester.framework;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.network.Message;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.OneShotEventListener;
import pw.aaron1011.mctester.framework.proxy.InvocationData;
import pw.aaron1011.mctester.message.toserver.MessageAck;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TesterHandler {

    volatile ConcurrentLinkedQueue<MessageAck> ackQueue = new ConcurrentLinkedQueue<>();
    volatile ConcurrentLinkedQueue<OneShotEventListener> listeners = new ConcurrentLinkedQueue<>();
    volatile ConcurrentLinkedQueue<Message> outboundMessages = new ConcurrentLinkedQueue<>();

    public static ReentrantLock lock = new ReentrantLock();
    public static volatile Condition condition = lock.newCondition();

    public void receiveAck(MessageAck ack) {
        this.ackQueue.add(ack);
    }

    public <T extends Event> void addOneShotOld(OneShotEventListener<T> listener) {
        this.listeners.add(listener);
    }

    public void addOutbound(Message message) {
        this.outboundMessages.add(message);
    }

    public void addInvocation(InvocationData data) {
        //this.invokeQueue.add(data);
    }


    public void run() {
        TesterThread thread = new TesterThread();
        thread.start();

        Sponge.getScheduler().createTaskBuilder().name("Proxy executor").intervalTicks(1).execute((task) -> {


            lock.lock();

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
                condition.signal();
            }

            Message outbound = outboundMessages.poll();
            if (outbound != null) {
                McTester.INSTANCE.channel.sendTo(McTester.getThePlayer(), outbound);
            }

            // Next, handle the latest method invocation, and wake up the thread
            /*InvocationData data = invokeQueue.poll();
            if (data != null) {
                try {
                    data.execute();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                condition.signal();
            }

            lock.unlock();*/


        }).submit(McTester.INSTANCE);
    }

}
