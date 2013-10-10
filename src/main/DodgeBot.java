package main;

import se.openmind.kart.GameState;
import se.openmind.kart.GameState.ItemBox;
import se.openmind.kart.GameState.Kart;
import se.openmind.kart.GameState.Shell;
import se.openmind.kart.OrderUpdate.Order;

public class DodgeBot extends MooBot {
  @Override
  public Order playGame(GameState state) {
    Kart me = state.getYourKart();
    Vector mePos = new Vector(me.getXPos(), me.getYPos());
    
    for (Shell shell : state.getShells()) {
      if (shell.getTargetId() == me.getId()) {
        Vector shellPos = new Vector(shell.getXPos(), shell.getYPos());
        Vector delta = Vector.between(shellPos, mePos);
        if (delta.getLength() > 20) {
          return Order.MoveOrder(shell.getXPos(), shell.getYPos());
        }
        else {
          Vector ortho;
          if (delta.cross(new Vector(me.getxSpeed(), me.getySpeed())) > 0) {
            ortho = new Vector(shell.getxSpeed(), shell.getySpeed()).turn(Math.PI/2);
          }
          else {
            ortho = new Vector(shell.getxSpeed(), shell.getySpeed()).turn(Math.PI/2);
          }
          Vector dest = shellPos.add(ortho.scale(100));
          return Order.MoveOrder(dest.x, dest.y);
        }
      }
    }
    
    // This default implementation will move towards the closest item box
    ItemBox closestItemBox = null;
    for(ItemBox i : state.getItemBoxes()) {
      if(closestItemBox == null || distance(me, i) < distance(me, closestItemBox)) {
        closestItemBox = i;
      }
    }
    if(closestItemBox != null) {
      return Order.MoveOrder(closestItemBox.getXPos(), closestItemBox.getYPos());
    }

    // Returning null is ok, your bot will continue doing what it is doing
    return null;
  }
}
