package main;

import java.util.Collections;
import java.util.List;

import se.openmind.kart.ApiClient;
import se.openmind.kart.GameState;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.GameState.Shell;
import se.openmind.kart.OrderUpdate.Order;

import com.google.common.base.Optional;

public class ConstructionBot extends MooBot {
  public static void main(String[] args) {
    String url = "http://kart.openmind.se/api/GameState";
    String accessKey = "e3e1e3c3";
    new ApiClient(url, accessKey, "Moo").Run(new ConstructionBot());
  }

  @Override
  public Order playGame(GameState state) {
    Kart me = state.getYourKart();

    List<Kart> sortedKarts = state.getEnemyKarts();
    Collections.sort(sortedKarts, new DistanceToEntityComparator(state.getYourKart()));
    List<ItemBox> sortedBoxes = state.getItemBoxes();
    Collections.sort(sortedBoxes, new DistanceToEntityComparator(state.getYourKart()));

    if (!sortedBoxes.isEmpty() && me.getShells() == 0) {
      return moveTo(sortedBoxes.get(0));
    }

    Optional<Shell> shell = getShellForDefense(state);
    if (shell.isPresent() && me.getShells() >= 2) {
      return Order.FireOrder(shell.get().getId());
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
    return nothingUrgentOrder(state);
  }

  protected Vector getInPositionToShootEnemeny(Kart me, Kart enemy) {
    Vector myPosition = Vector.positionOf(me);
    Vector meToEnemy = Vector.between(Vector.positionOf(me), Vector.positionOf(enemy));
    if (meToEnemy.getLength() > 30) {
      return Vector.positionOf(enemy);
    }

    Vector myDirection = new Vector(me.getxSpeed(), me.getySpeed());
    double dot = myDirection.dot(meToEnemy);
    if (dot >= 0) {
      return Vector.positionOf(enemy);
    } else {
      return myPosition.subtract(meToEnemy);
    }
  }

  protected Optional<Shell> getShellForDefense(GameState state) {
    Kart me = state.getYourKart();
    Vector myPosition = Vector.positionOf(me);

    for (Shell shell : state.getShells()) {
      if (shell.getTargetId() == me.getId()) {
        Vector shellPosition = Vector.positionOf(shell);
        Vector meToShell = Vector.between(myPosition, shellPosition);
        if (meToShell.getLength() > 5) {
          continue;
        }

        Vector myDirection = new Vector(me.getxSpeed(), me.getySpeed());
        double dot = myDirection.getUnitVector().dot(meToShell.getUnitVector());
        if (Math.abs(dot) > 0.9) {
          return Optional.of(shell);
        }
      }
    }
    return Optional.absent();
  }

  protected Order nothingUrgentOrder(GameState state) {
    Kart me = state.getYourKart();
    List<Kart> sortedKarts = state.getEnemyKarts();
    Collections.sort(sortedKarts, new DistanceToEntityComparator(state.getYourKart()));
    if (sortedKarts.isEmpty()) {
      return null;
    }

    for (Kart enemy : sortedKarts) {
      if (enemy.getInvulnerableTimeLeft() > 0 || enemy.getStunnedTimeLeft() > 0) {
        continue;
      }
      Vector nextPosition = getInPositionToShootEnemeny(me, enemy).truncateToValid();
      return Order.MoveOrder(nextPosition.x, nextPosition.y);
    }
    return null;
  }
}
