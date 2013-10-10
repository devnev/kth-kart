package se.openmind.kart;

import com.google.common.collect.ImmutableList;

import main.ConstructionBot;
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

    Util.runBot(url, new ConstructionBot(), "testkey" + "1", "Moo");

    ImmutableList<Bot> opponents = ImmutableList.<Bot>of(
      new FetchAndFireBot(),
      new MyBot(),
      new MyBot());
    int i = 2;
    for (Bot opponent : opponents) {
      runBot(url, opponent, "testkey" + i, "Opponent" + i);
      ++i;
    }
  }

  public static void runBot(final String url, final Bot bot, final String accessKey,
      final String teamName) {
    Runnable r = new Runnable() {
      @Override
      public void run() {
        ApiClient client = new ApiClient(url, accessKey, teamName);
        client.Run(bot);
      }
    };
    new Thread(r).start();
  }
}
