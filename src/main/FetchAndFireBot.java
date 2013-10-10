package main;

import se.openmind.kart.GameState;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.OrderUpdate.Order;

public class FetchAndFireBot extends MooBot {
  @Override
  public Order playGame(GameState state) {
    Kart me = state.getYourKart();

    if (me.getShells() < 2) {
      // This default implementation will move towards the closest item
      // box
      ItemBox closestItemBox = null;
      for (ItemBox i : state.getItemBoxes()) {
        if (closestItemBox == null || distance(me, i) < distance(me, closestItemBox)) {
          closestItemBox = i;
        }
      }
      if (closestItemBox != null) {
        return Order.MoveOrder(closestItemBox.getXPos(), closestItemBox.getYPos());
      }
    } else {
      Kart closestKart = null;
      for (Kart k : state.getEnemyKarts()) {
        if (closestKart == null || distance(me, k) < distance(me, closestKart)) {
          closestKart = k;
        }
      }
      if (closestKart != null) {
        if (isGuaranteedHit(me, closestKart)) {
          return Order.FireOrder(closestKart.getId());
        } else {
          return Order.MoveOrder(closestKart.getXPos(), closestKart.getYPos());
        }
      }
    }

    return null;
  }
}
