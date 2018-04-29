package pw.aaron1011.mctester;

import pw.aaron1011.mctester.message.ResponseWrapper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ServerOnly {

    public static final BlockingQueue<ResponseWrapper> INBOUND_QUEUE = new ArrayBlockingQueue(1);

}
