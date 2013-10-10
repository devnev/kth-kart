package main;

import se.openmind.kart.GameState;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.OrderUpdate.Order;

public class FetchAndFireBot extends MooBot {
  @Override
  public Order playGame(GameState state) {
    Kart me = state.getYourKart();

    if (me.getShells() < 1) {
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
      for (Kart k : state.getEnemyKarts()) {
        if (isGuaranteedHit(me, k)) {
          return Order.FireOrder(k.getId());
        }
      }
      
      Kart closestKart = null;
      for (Kart k : state.getEnemyKarts()) {
        if (closestKart == null || distance(me, k) < distance(me, closestKart)) {
          closestKart = k;
        }
      }
      if (closestKart != null) {
        return Order.MoveOrder(closestKart.getXPos(), closestKart.getYPos());
      }
    }

    return null;
  }
}
