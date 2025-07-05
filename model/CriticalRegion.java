package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;
import java.util.concurrent.Semaphore;
import util.Direction;

public class CriticalRegion {
  private String id;
  private List<Rectangle> rectangles;
  private Map<Rectangle, Semaphore> semaphorePerRectangle;
  private List<Robot> occupants;
  private volatile Direction direction;
  private boolean egoist;

  public CriticalRegion(String id, List<Rectangle> rectangles){
    this.id = id;
    this.rectangles = rectangles;
    this.direction = Direction.NONE;
    this.egoist = false;
    this.semaphorePerRectangle = new HashMap<>();
    this.occupants = new ArrayList<>();

    for(Rectangle rec : rectangles){
      semaphorePerRectangle.put(rec, new Semaphore(1));
    }
  }

  public CriticalRegion(String id, List<Rectangle> rectangles, boolean egoist){
    this.id = id;
    this.rectangles = rectangles;
    this.direction = Direction.NONE;
    this.egoist = true;
    this.semaphorePerRectangle = new HashMap<>();
    this.occupants = new ArrayList<>();

    for(Rectangle rec : rectangles){
      semaphorePerRectangle.put(rec, new Semaphore(1));
    }
  }

  public String getId(){
    return id;
  }

  public List<Rectangle> getCriticalZones() {
    return Collections.unmodifiableList(rectangles);
  }

  public boolean intersects(Bounds bounds) {
    for (Rectangle rect : rectangles) {
      if (bounds.intersects(rect.getBoundsInParent())) {
        return true; 
      }
    }
    return false;
  }

  public Rectangle findRecForBounds(Bounds robotBounds) {
        // Calcula o ponto central do robô
        double centerX = robotBounds.getMinX() + robotBounds.getWidth() / 2;
        double centerY = robotBounds.getMinY() + robotBounds.getHeight() / 2;

        for (Rectangle rect : this.rectangles) {
            // Usa o método .contains() que verifica se um ponto está dentro do retângulo
            if (rect.getBoundsInParent().contains(centerX, centerY)) {
                return rect; // Encontrou! Retorna e para de procurar.
            }
        }
        return null; // O centro do robô não está em nenhum retângulo.
    }

  public Semaphore getSemaphoreForRec(Rectangle rec){
    return semaphorePerRectangle.get(rec);
  }

  public void addOccupant(Robot robot){
    occupants.add(robot);
  }

  public void removeOccupant(Robot robot){
    occupants.remove(robot);
  }
  public List<Robot> getOccupants(){ return occupants;}
  public List<Rectangle> getRectangles(){ return rectangles;}
  public Direction getDirection(){ return direction;}
  public void setDirection(Direction direction){ this.direction = direction;}
  public boolean isEgoist(){ return egoist;}
}
