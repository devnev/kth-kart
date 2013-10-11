package main;

import se.openmind.kart.GameState;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.OrderUpdate.Order;

import com.google.common.collect.ImmutableList;

public class Peaceful extends ConstructionBot {
  ImmutableList<Vector> candidatePoints = ImmutableList.of(
    new Vector(0, 0),
    new Vector(0, 100),
    new Vector(100, 0),
    new Vector(100, 100));


  @Override
  protected Order nothingUrgentOrder(GameState currentState) {
    double maxDistance = -1;
    for (Kart e1 : currentState.getEnemyKarts()) {
      Vector position1 = Vector.positionOf(e1);

      for (Kart e2 : currentState.getEnemyKarts()) {
        Vector position2 = Vector.positionOf(e2);
        if (e1 == e2) {
          continue;
        }
        maxDistance = Math.max(maxDistance, Vector.between(position1, position2).getLength());
      }
    }

    Vector enemyCentroid = getEnemyCentroid(currentState);

    if (maxDistance < 30) {
      double maxCentroidDistance = -1;
      Vector farthestPoint = null;
      for (Vector point : candidatePoints) {
        double d = Vector.between(enemyCentroid, point).getLength();
        if (d > maxCentroidDistance) {
          farthestPoint = point;
          maxCentroidDistance = d;
        }
      }

      return Order.MoveOrder(farthestPoint.x, farthestPoint.y);
    }
    return null;
  }
}
