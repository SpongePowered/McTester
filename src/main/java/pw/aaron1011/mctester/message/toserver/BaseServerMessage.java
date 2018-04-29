package pw.aaron1011.mctester.message.toserver;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.Message;

public abstract class BaseServerMessage implements Message {

    public abstract void process();

}
