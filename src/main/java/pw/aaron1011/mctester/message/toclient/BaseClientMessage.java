package pw.aaron1011.mctester.message.toclient;

import net.minecraft.client.Minecraft;
import org.spongepowered.api.network.Message;

public abstract class BaseClientMessage implements Message {

    public abstract void process();

}
