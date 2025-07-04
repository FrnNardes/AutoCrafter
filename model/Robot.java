package model;

import java.util.List;

import controller.SimulationController;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class Robot  extends Thread{
  // --- Atributos de Identificação e UI ---
  private final int id;
  private final ImageView sprite; // O visual do robô na tela
  private final ImageView pathVisual;
  private final List<Position> path;
  private final SimulationController controller;
  private final Slider slider;

  // --- Variáveis de Estado (Ponte entre Threads) ---
  // volatile garante que alterações em uma thread sejam imediatamente visíveis na outra.
  private volatile boolean running = true;
  private volatile boolean paused = false;
  private volatile boolean isBlocked = false; // A "ponte" principal: true se a lógica de colisão está trabalhando.
  private volatile int pathIndex = 0;

  // A lógica de colisão (thread 'run') usa estas para saber onde o robô está.
  private volatile double currentX;
  private volatile double currentY;

  // Apenas a thread 'run' deve modificar esta variável.
  private CriticalRegion acquiredRegion = null;

  public Robot(int id, ImageView sprite, List<Position> path, ImageView pathVisual, Slider slider, SimulationController controller){
    this.id = id;
    this.sprite = sprite;
    this.path = path;
    this.pathVisual = pathVisual;
    this.slider = slider;
    this.controller = controller;

    if (!path.isEmpty()){
        this.currentX = path.get(0).getX();
        this.currentY = path.get(0).getY();
    } else {
        this.currentX = 0; // Posição padrão caso o caminho seja vazio
        this.currentY = 0;
    }

    this.setDaemon(true);
    sprite.setFitWidth(40); 
    sprite.setFitHeight(40);
  }

 @Override
public void run() {
    try {
        while (!Thread.currentThread().isInterrupted()) {
            // CORRETO: Crie um "Bounds" fantasma usando as variáveis seguras (volatile)
            Bounds currentBounds = new Rectangle(this.currentX, this.currentY, 40, 40).getBoundsInLocal();

            // O resto da lógica de liberação permanece igual, pois agora usa os 'Bounds' seguros
            if (acquiredRegion != null && !acquiredRegion.intersects(currentBounds)) {
                controller.getSemaphoreForRegion(acquiredRegion).release();
                acquiredRegion = null;
            }

            // --- 2. LÓGICA DE AQUISIÇÃO PREVENTIVA (JÁ ESTAVA CORRETA) ---
            // Esta parte já estava correta porque já usava 'this.currentX' e 'this.currentY'.
            Position target = path.get((pathIndex + 1) % path.size());
            double futureX = this.currentX - Integer.compare((int)this.currentX, target.getX());
            double futureY = this.currentY - Integer.compare((int)this.currentY, target.getY());
            
            Bounds futureBounds = new Rectangle(futureX, futureY, 40, 40).getBoundsInLocal();

            CriticalRegion nextRegion = controller.findRegionForBounds(futureBounds);

            // O resto do código não precisa de alterações
            if (nextRegion != null && nextRegion != acquiredRegion) {
                this.isBlocked = true;

                try {
                    controller.getSemaphoreForRegion(nextRegion).acquire();
                    if (running) {
                      if(acquiredRegion != null && !acquiredRegion.equals(nextRegion)){
                        controller.getSemaphoreForRegion(acquiredRegion).release();
                        acquiredRegion = null;
                      }
                        this.acquiredRegion = nextRegion;
                    }
                } finally {
                    this.isBlocked = false;
                }
            }
        }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
        } finally {
            // Lógica de limpeza para garantir que nenhum semáforo fique preso se a thread morrer
            if (acquiredRegion != null) {
                controller.getSemaphoreForRegion(acquiredRegion).release();
            }
        }
    }


   public void updateVisuals() {
      if (paused || isBlocked || !running) {
          return; 
      }

      Position target = path.get((pathIndex + 1) % path.size());

      // Se já chegou ao destino do segmento, avança para o próximo.
      if ((int)sprite.getLayoutX() == target.getX() && (int)sprite.getLayoutY() == target.getY()) {
          pathIndex = (pathIndex + 1) % path.size();
          return;
      }

      // Calcula o próximo pixel do movimento
      double nextX = sprite.getLayoutX() - Integer.compare((int)sprite.getLayoutX(), target.getX());
      double nextY = sprite.getLayoutY() - Integer.compare((int)sprite.getLayoutY(), target.getY());

      // Atualiza a UI e o estado compartilhado
      sprite.setLayoutX(nextX);
      sprite.setLayoutY(nextY);
      this.currentX = nextX;
      this.currentY = nextY;
  }

  public void kill() {
    this.paused = false;
    this.isBlocked = false;
    this.pathIndex = 0;

    if (!path.isEmpty()) {
        Position initialPos = path.get(0);
        // Atualiza as variáveis de estado para a thread de lógica
        this.currentX = initialPos.getX();
        this.currentY = initialPos.getY();

        // Garante que a atualização da UI ocorra na thread correta
        Platform.runLater(() -> {
            sprite.setLayoutX(initialPos.getX());
            sprite.setLayoutY(initialPos.getY());
        });
    }
}

  public void togglePause() { this.paused = !this.paused; }
  public boolean isPaused() { return paused; }
  public void togglePath() { pathVisual.setVisible(!pathVisual.isVisible()); }
  public boolean isPathVisible() { return pathVisual.isVisible(); }
  public ImageView getSprite(){ return sprite; }
}
