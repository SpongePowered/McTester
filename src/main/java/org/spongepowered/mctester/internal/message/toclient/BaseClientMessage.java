package org.spongepowered.mctester.internal.message.toclient;

import org.spongepowered.api.network.Message;

public abstract class BaseClientMessage implements Message {

    public abstract void process();

}
