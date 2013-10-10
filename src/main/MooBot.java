package main;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.openmind.kart.Bot;
import se.openmind.kart.GameState;
import se.openmind.kart.GameState.Entity;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.GameState.MovingEntity;
import se.openmind.kart.OrderUpdate.Order;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Base class for bots providing utility methods.
 */
public abstract class MooBot implements Bot {
  private double BULLET_LIFETIME_S = 1.0;

  private ImmutableMap<Integer, Integer> BOX_DISTANCE_THRESHOLD = ImmutableMap.<Integer, Integer>builder()
      .put(0, 100)
      .put(1, 10)
      .put(2, 10)
      .put(3, 5)
      .put(4, 5)
      .put(5, 0)
      .build();

  protected double distance(Entity a, Entity b) {
    double xDist = a.getXPos() - b.getXPos();
    double yDist = a.getYPos() - b.getYPos();
    return Math.sqrt(xDist*xDist + yDist*yDist);
  }

  /**
   * Returns true if we think that source will actually hit target if it fires now.
   */
  protected boolean isGuaranteedHit(Kart source, Kart target) {
    if (target.getInvulnerableTimeLeft() > 0) {
      return false;
    }

    Vector sourcePosition = new Vector(source.getXPos(), source.getYPos());
    Vector targetPosition = new Vector(target.getXPos(), target.getYPos());

    Vector unitSourceFacing = new Vector(source.getxSpeed(), source.getySpeed()).getUnitVector();
    Vector sourceToTarget = Vector.between(sourcePosition, targetPosition);
    double currentDistance = sourceToTarget.getLength();

    Vector sourceToExpectedTarget = Vector.between(
        sourcePosition, expectedPosition(target, BULLET_LIFETIME_S));
    if (sourceToExpectedTarget.getLength() > 28) {
      return false;
    }

    double dot = unitSourceFacing.dot(sourceToTarget.getUnitVector());
    return Math.abs(dot) > getAngleThreshold(currentDistance);
  }

  Vector expectedPosition(MovingEntity entity, double seconds) {
    return new Vector(
        entity.getXPos() + seconds * entity.getxSpeed(),
        entity.getYPos() + seconds * entity.getySpeed());
  }

  /**
   * Returns a dot-product threshold for firing depending on the distance to the target.
   */
  private double getAngleThreshold(double distance) {
    double normalizedDistance = distance / 30.0;
    double min = 0.7;
    double max = 0.95;
    return max - normalizedDistance * (max - min);
  }

  protected Optional<Vector> getShellEvasionDirection() {
    throw new UnsupportedOperationException();
  }

  protected Optional<ItemBox> selectItemBox(GameState state) {
    final Kart me = state.getYourKart();
    List<ItemBox> itemBoxes = state.getItemBoxes();
    Collections.sort(itemBoxes, new DistanceToEntityComparator(state.getYourKart()));
    for (ItemBox box : itemBoxes) {
      double distance = distance(me, box);
      if (distance < BOX_DISTANCE_THRESHOLD.get(me.getShells())) {
        return Optional.of(box);
      }
    }
    return Optional.absent();
  }

  protected Order moveTo(Entity entity) {
    return Order.MoveOrder(entity.getXPos(), entity.getYPos());
  }

  protected static class DistanceToEntityComparator implements Comparator<Entity> {
    private Entity entity;

    public DistanceToEntityComparator(Entity entity) {
      this.entity = entity;
    }

    @Override
    public int compare(Entity first, Entity second) {
      Vector meToFirst = Vector.between(Vector.positionOf(entity), Vector.positionOf(first));
      Vector meToSecond = Vector.between(Vector.positionOf(entity), Vector.positionOf(second));
      return Double.compare(meToFirst.getLength(), meToSecond.getLength());
    }
  }
}
