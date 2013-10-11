package main;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import se.openmind.kart.Bot;
import se.openmind.kart.GameState;
import se.openmind.kart.GameState.Entity;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.GameState.MovingEntity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Base class for bots providing utility methods.
 */
public abstract class MooBot implements Bot {
  private static final Log log = LogFactory.getLog(MooBot.class);

  private double BULLET_LIFETIME_S = 1.0;

  private ImmutableMap<Integer, Integer> BOX_DISTANCE_THRESHOLD = ImmutableMap.<Integer, Integer>builder()
      .put(0, 100)
      .put(1, 25)
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

  protected boolean isValid(Vector position) {
    return position.x >= 0 && position.x <= 100 && position.y >= 0 && position.y <= 100;
  }

  /**
   * Returns true if we think that source will actually hit target if it fires now.
   */
  protected boolean isGuaranteedHit(Kart source, Kart target) {
    if (target.getInvulnerableTimeLeft() > 0 || target.getStunnedTimeLeft() > 0) {
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

  protected Optional<ItemBox> selectItemBox(GameState state, boolean threshold) {
    final Kart me = state.getYourKart();
    List<ItemBox> itemBoxes = state.getItemBoxes();
    Collections.sort(itemBoxes, new DistanceToEntityComparator(state.getYourKart()));

    for (ItemBox box : itemBoxes) {
      double distance = distance(me, box);
      boolean cont = false;
      for (Kart enemy : state.getEnemyKarts()) {
        if (distance(enemy, box) < distance) {
          // Ignore boxes with enemies close by.
          cont = true;
        }
      }
      if (cont) {
        continue;
      }

      if (!threshold || distance < BOX_DISTANCE_THRESHOLD.get(me.getShells())) {
        return Optional.of(box);
      }
    }
    return Optional.absent();
  }

  double turnMoveDistance(Entity target, MovingEntity source, double turnRadius) {
    Vector targetPos = new Vector(target.getXPos(), target.getYPos());
    Vector sourcePos = new Vector(source.getXPos(), source.getYPos());
    Vector targetDelta = Vector.between(sourcePos, targetPos);
    Vector sourceDirection = new Vector(Math.cos(source.getDirection()), Math.sin(source.getDirection()));
    double cross = targetDelta.getUnitVector().cross(sourceDirection);
    double dotted = targetDelta.getUnitVector().dot(sourceDirection);

    if (dotted < 1+0.000001 && dotted > 1-0.000001) {
      return targetDelta.getLength();
    }

    Vector turnCenter;
    if (cross < 0) {
      turnCenter = sourcePos.add(sourceDirection.turn(-Math.PI / 2).scale(turnRadius));
    } else {
      turnCenter = sourcePos.add(sourceDirection.turn(Math.PI / 2).scale(turnRadius));
    }

    double[][] tangents =
        CircleTangents.getTangents(turnCenter.x, turnCenter.y, turnRadius, targetPos.x,
            targetPos.y, 0);
    if (tangents.length == 0) {
      return -1;
    } else {
      double straightDistance =
          Vector.between(new Vector(tangents[0][0], tangents[0][1]),
              new Vector(tangents[0][2], tangents[0][3])).getLength();
      Vector tangentStartDelta = Vector.between(sourcePos, new Vector(tangents[0][0], tangents[0][1]));
      double turnRadians = Math.asin(tangentStartDelta.divide(2).getLength()/turnRadius) * 2 * turnRadius;
      return straightDistance + turnRadians;
    }
  }

  protected Vector getSafePointNearEnemy(Kart me, Kart enemy) {
    Vector myPosition = Vector.positionOf(me);
    Vector enemyPosition = Vector.positionOf(enemy);
    Vector enemyDirection = new Vector(enemy.getxSpeed(), enemy.getySpeed()).getUnitVector();
    Vector safeDirection = enemyDirection.turn(Math.PI / 2.0);

    if (Double.isNaN(safeDirection.x) || Double.isNaN(safeDirection.y)) {
      log.info("Enemy position: " + enemyPosition + ", enemy direction: " + enemyDirection);
      return enemyPosition;
    }

    Vector safeLeft = enemyPosition.add(safeDirection);
    Vector safeRight = enemyPosition.subtract(safeDirection);

    double distanceLeft = Vector.between(myPosition, safeLeft).getLength();
    double distanceRight = Vector.between(myPosition, safeRight).getLength();

    Vector returnValue = distanceLeft < distanceRight ? safeLeft : safeRight;
    return returnValue.truncateToValid();
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
