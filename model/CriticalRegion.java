package model;

import java.util.Collections;
import java.util.List;

import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;

public class CriticalRegion {
  private String id;
  private List<Rectangle> criticalZones;

  public CriticalRegion(String id, List<Rectangle> criticalZones){
    this.id = id;
    this.criticalZones = criticalZones;
  }

  public String getId(){
    return id;
  }

  public List<Rectangle> getCriticalZones() {
    return Collections.unmodifiableList(criticalZones); // Retorna uma lista imutável
  }

  // Método para verificar se os limites de um objeto (como um robô) intersectam esta região crítica
  public boolean intersects(Bounds bounds) {
    for (Rectangle rect : criticalZones) {
      if (bounds.intersects(rect.getBoundsInParent())) {
        return true; // Se intersectar qualquer um dos retângulos componentes, intersecta a região
      }
    }
    return false;
  }
}
