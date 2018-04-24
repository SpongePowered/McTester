package pw.aaron1011.mctester.framework.proxy;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class GameProxy implements InvocationHandler {

    private static ThreadLocal<Game> game;

    public static void init() {
        Game origGame = Sponge.getGame();
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        return null;
    }
}
