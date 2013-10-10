package se.openmind.kart;

import main.FetchAndFireBot;
import main.MyBot;

public class Util {
  /**
   * Utility for running multiple bots in the same process
   * 
   * @param args
   */
  public static void main(String[] args) {
    String url = "http://localhost:8080/api/GameState";
    if (args.length > 1) {
      url = args[0];
    }

    if (args.length == 2 && args[0].equals("official")) {
      // Config for official submission.
      url = "http://kart.openmind.se/api/GameState";
      new ApiClient(url, "e3e1e3c3", "Moo").Run(new MyBot());
      return;
    }

    runBot(url, new FetchAndFireBot(), "testkey" + "1", "Moo");
    runBot(url, new MyBot(), "testkey" + "2", "TestBot2");
    runBot(url, new MyBot(), "testkey" + "3", "TestBot3");
    runBot(url, new MyBot(), "testkey" + "4", "TestBot4");
    runBot(url, new MyBot(), "testkey" + "5", "TestBot5");
  }

  public static void runBot(final String url, final Bot bot, final String accessKey,
      final String teamName) {
    Runnable r = new Runnable() {
      public void run() {
        ApiClient client = new ApiClient(url, accessKey, teamName);
        client.Run(bot);
      }
    };
    new Thread(r).start();
  }
}
