package pw.aaron1011.mctester.framework;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.testcase.ChatTest;

public class TesterThread extends Thread {

    public TesterThread() {
        this.setName("Tester Thread");
    }

    @Override
    public void run() {
        Game realGame = Sponge.getGame();
        Game fakeGame = (Game) McTester.proxy(realGame);
        IntegratedClientHandler client = new IntegratedClientHandler();

        ChatTest chatTest = new ChatTest();
        chatTest.runTest(fakeGame, client);
    }

}
