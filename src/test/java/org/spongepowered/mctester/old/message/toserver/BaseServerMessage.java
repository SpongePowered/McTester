package org.spongepowered.mctester.old.message.toserver;

import org.spongepowered.api.network.Message;

public abstract class BaseServerMessage implements Message {

    public abstract void process();

}
