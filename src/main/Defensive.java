package main;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import se.openmind.kart.GameState;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.OrderUpdate.Order;

public class Defensive extends MooBot {
  ImmutableList<Vector> corners = ImmutableList.of(
      new Vector(0, 0),
      new Vector(100, 0),
      new Vector(0, 100),
      new Vector(100, 00));

  @Override
  public Order playGame(GameState currentState) {
    Kart me = currentState.getYourKart();
    Vector myPosition = Vector.positionOf(me);

    final Vector enemyCentroid = getEnemyCentroid(currentState);
    Vector themToMe = Vector.between(enemyCentroid, myPosition);

    Ordering<Vector> minDistanceToCentroid = Ordering.natural().onResultOf(
      new Function<Vector, Double>(){
        @Override
        public Double apply(Vector v) {
          return Vector.between(enemyCentroid, v).getLength();
        }
      });
    return Order.MoveOrder(minDistanceToCentroid.max(corners));
  }
}
