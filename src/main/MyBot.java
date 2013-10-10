package main;

import se.openmind.kart.ApiClient;
import se.openmind.kart.Bot;
import se.openmind.kart.GameState;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.OrderUpdate.Order;
import se.openmind.kart.Util;

import com.google.common.collect.ImmutableList;

public class MyBot extends MooBot {
  /*
   * The first thing you should do is enter your access key and team name
   * Note: Team name cannot be changed once set (without magic from contest administrators)
   */
  private static String teamName =  "Moo";

  public static void main(String[] args) {
    String url = "http://localhost:8080/api/GameState";
    String accessKey = "testkey1";

    if (args.length == 2 && args[0].equals("official")) {
      // Config for official submission.
      url = "http://kart.openmind.se/api/GameState";
      accessKey = "e3e1e3c3";
      new ApiClient(url, accessKey, teamName).Run(new MyBot());
      return;
    }

    Util.runBot(url, new MyBot(), "testkey" + "1", "Moo");

    ImmutableList<Bot> opponents = ImmutableList.<Bot>of(
      new MyBot(),
      new MyBot(),
      new MyBot());
    int i = 2;
    for (Bot opponent : opponents) {
      Util.runBot(url, opponent, "testkey" + i, "Opponent" + i);
      ++i;
    }
  }

  Integer targetEnemy;

  /**
   * This is the main method that the ApiClient will invoke, put your game logic here.
   */
  @Override
  public Order playGame(GameState state) {
    Kart me = state.getYourKart();

    // This default implementation will move towards the closest item box
    ItemBox closestItemBox = null;
    for(ItemBox i : state.getItemBoxes()) {
      if(closestItemBox == null || distance(me, i) < distance(me, closestItemBox)) {
        closestItemBox = i;
      }
    }
    if(closestItemBox != null) {
      return Order.MoveOrder(closestItemBox.getXPos(), closestItemBox.getYPos());
    }

    // Returning null is ok, your bot will continue doing what it is doing
    return null;
  }
}
