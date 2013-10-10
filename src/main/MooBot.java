package main;

import se.openmind.kart.Bot;
import se.openmind.kart.GameState.Entity;
import se.openmind.kart.GameState.Kart;

/**
 * Base class for bots providing utility methods.
 */
public abstract class MooBot implements Bot {
  private static final double DOT_FIRE_THRESHOLD = 0.8;

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

    double distance = sourceToTarget.getLength();
    if (distance > 30) {
      return false;
    }

    double dot = unitSourceFacing.dot(sourceToTarget.getUnitVector());
    return Math.abs(dot) > DOT_FIRE_THRESHOLD;
  }
}
