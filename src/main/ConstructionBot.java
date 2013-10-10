package main;

import java.util.Collections;
import java.util.List;

import se.openmind.kart.GameState;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.OrderUpdate.Order;

import com.google.common.base.Optional;

public class ConstructionBot extends MooBot {
  @Override
  public Order playGame(GameState state) {
    Kart me = state.getYourKart();

    List<Kart> sortedKarts = state.getEnemyKarts();
    Collections.sort(sortedKarts, new DistanceToEntityComparator(state.getYourKart()));
    List<ItemBox> sortedBoxes = state.getItemBoxes();
    Collections.sort(sortedBoxes, new DistanceToEntityComparator(state.getYourKart()));

    if (me.getShells() == 0) {
      return moveTo(sortedBoxes.get(0));
    }

    for (Kart kart : sortedKarts) {
      if (isGuaranteedHit(me, kart)) {
        return Order.FireOrder(kart.getId());
      }
    }

    Optional<ItemBox> itemBox = selectItemBox(state);
    if (itemBox.isPresent()) {
      return Order.MoveOrder(itemBox.get().getXPos(), itemBox.get().getYPos());
    }

    Kart targetKart = sortedKarts.get(0);
    return Order.MoveOrder(targetKart.getXPos(), targetKart.getYPos());
  }
}
