package org.spongepowered.mctester.old.message.toclient;

import org.spongepowered.api.network.Message;

public abstract class BaseClientMessage implements Message {

    public abstract void process();

}
