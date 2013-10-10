package main;

import se.openmind.kart.Bot;
import se.openmind.kart.GameState.Entity;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.GameState.MovingEntity;

/**
 * Base class for bots providing utility methods.
 */
public abstract class MooBot implements Bot {
  private double BULLET_LIFETIME_S = 1.0;

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
    if (sourceToExpectedTarget.getLength() > 30) {
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

  double turnMoveDistance(Entity target, Kart me, double turnRadius) {
    Vector targetPos = new Vector(target.getXPos(), target.getYPos());
    Vector mePos = new Vector(me.getXPos(), me.getYPos());
    Vector targetDelta = Vector.between(mePos, targetPos);
    Vector meDirection = new Vector(Math.sin(me.getDirection()), Math.cos(me.getDirection()));
    double dotted = targetDelta.getUnitVector().dot(meDirection);

    if (dotted < 0.000001 && dotted > -0.000001) {
      return targetDelta.getLength();
    }

    Vector turnCenter;
    if (dotted < 0) {
      turnCenter = mePos.add(meDirection.turn(-Math.PI / 2).scale(turnRadius));
    } else {
      turnCenter = mePos.add(meDirection.turn(Math.PI / 2).scale(turnRadius));
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
      Vector tangentStartDelta = Vector.between(mePos, new Vector(tangents[0][0], tangents[0][1]));
      double turnRadians = Math.asin(tangentStartDelta.divide(2).getLength()) * 2;
      return straightDistance + turnRadians;
    }
  }
}
