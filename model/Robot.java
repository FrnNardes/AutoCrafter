package model;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import controller.SimulationController;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class Robot extends Thread {
    // --- Atributos (sem alterações) ---
    private final int id;
    private final ImageView sprite;
    private final ImageView pathVisual;
    private final List<Position> path;
    private final SimulationController controller;
    private final Slider slider;

    // --- Sincronização e Estado ---
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private volatile int pathIndex = 0;
    private volatile double currentX;
    private volatile double currentY;
    private volatile CountDownLatch movementLatch;
    private volatile boolean isClearedToMove = false;
    
    private CriticalRegion acquiredRegion = null;
    private CriticalRegion physicalRegion = null;
    private CriticalRegion nextPhysicalRegion = null;
    private final Rectangle boundsCalculator = new Rectangle(40, 40);

    public Robot(int id, ImageView sprite, List<Position> path, ImageView pathVisual, Slider slider, SimulationController controller) {
        this.id = id;
        this.sprite = sprite;
        this.path = path;
        this.pathVisual = pathVisual;
        this.slider = slider;
        this.controller = controller;

        if (!path.isEmpty()) {
            this.currentX = path.get(0).getX();
            this.currentY = path.get(0).getY();
        }
        
        this.setDaemon(true);
        this.setName("Robot-" + id);
        sprite.setFitWidth(40);
        sprite.setFitHeight(40);
        resetPosition();
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                Thread.sleep(5);
                
                // 1. Autoriza o movimento e prepara o latch.
                this.movementLatch = new CountDownLatch(1);
                this.isClearedToMove = true;
                System.out.printf("[ROBO %d | RUN] Autorizando movimento. Aguardando sinal do Piloto...%n", id);

                // 2. Dorme e espera o sinal.
                movementLatch.await();
                // 3. Acorda.
                this.isClearedToMove = false;
                System.out.printf("[ROBO %d | RUN] SINAL RECEBIDO! Movimento pausado para análise.%n", id);
                

                if (!running) break;

                System.out.printf("[ROBO %d | RUN] Análise - Posicao Fisica: %-10s | Posicao adquirida: %s%n",
                        id,
                        (physicalRegion != null ? physicalRegion.getId() : "NENHUMA"),
                        (acquiredRegion != null ? acquiredRegion.getId() : "NENHUMA"));

                if (physicalRegion != acquiredRegion) {
                    if (acquiredRegion != null) {
                        System.out.printf("[ROBO %d | RUN] Decisão: LIBERAR semaforo da regiao %s.%n", id, acquiredRegion.getId());
                        controller.getSemaphoreForRegion(acquiredRegion).release();
                        this.acquiredRegion = null;
                    }
                    if (physicalRegion != null) {
                        System.out.printf("[ROBO %d | RUN] Decisão: ADQUIRIR semaforo para a regiao %s...%n", id, physicalRegion.getId());
                        controller.getSemaphoreForRegion(physicalRegion).acquire();
                        this.acquiredRegion = physicalRegion;
                        System.out.printf("[ROBO %d | RUN] SUCESSO! Posse da regiao %s confirmada.%n", id, physicalRegion.getId());
                    } else {
                        this.physicalRegion = null;
                        System.out.printf("[ROBO %d | RUN] Decisão: ROBO agora está em área livre.%n", id);
                    }
                } else if(physicalRegion == null && acquiredRegion == null){
                    System.out.printf("[ROBO %d | RUN] DECISAO1: ADQUIRIR semaforo para a proxima regiao %s...%n", id, nextPhysicalRegion.getId());
                    System.out.println(controller.getSemaphoreForRegion(nextPhysicalRegion).availablePermits());
                    controller.getSemaphoreForRegion(nextPhysicalRegion).acquire();
                    this.acquiredRegion = nextPhysicalRegion;
                    this.physicalRegion = nextPhysicalRegion;
                }else{
                  if(nextPhysicalRegion != null){
                    System.out.printf("[ROBO %d | RUN] DECISAO1: ADQUIRIR semaforo para a proxima regiao %s...%n", id, nextPhysicalRegion.getId());
                    System.out.println(controller.getSemaphoreForRegion(nextPhysicalRegion).availablePermits());
                    controller.getSemaphoreForRegion(nextPhysicalRegion).acquire();
                    if(acquiredRegion != null){
                      System.out.printf("[ROBO %d | RUN] DECISAO2: LIBERAR semaforo da regiao %s.%n", id, acquiredRegion.getId());
                      controller.getSemaphoreForRegion(acquiredRegion).release();
                    }
                    this.acquiredRegion = nextPhysicalRegion;
                  } else {
                    System.out.printf("[ROBO %d | RUN] DECISAO1: ROBO agora está em área livre.%n", id);
                    System.out.printf("[ROBO %d | RUN] DECISAO2: LIBERAR semaforo da regiao %s.%n", id, acquiredRegion.getId());
                    controller.getSemaphoreForRegion(acquiredRegion).release();
                    this.physicalRegion = null;
                    this.acquiredRegion = null;
                  }
              }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (physicalRegion != null) {
                System.out.printf("[ROBO %d | RUN] FIM DA THREAD. Liberando semaforo final da regiao %s.%n", id, physicalRegion.getId());
                controller.getSemaphoreForRegion(physicalRegion).release();
            }
        }
    }

    public void updateVisuals() {
        if (paused || !isClearedToMove) {
            return;
        }
        
        // --- Lógica de Movimento ---
        double currentX = sprite.getLayoutX();
        double currentY = sprite.getLayoutY();

        boundsCalculator.setX(this.currentX);
        boundsCalculator.setY(this.currentY);
        Bounds currentBounds = boundsCalculator.getBoundsInLocal();
        physicalRegion = controller.findRegionForBounds(currentBounds);

        Position target = path.get((pathIndex + 1) % path.size());
        // ... (cálculo de nextX/nextY) ...
        double targetX = target.getX();
        double targetY = target.getY();
        double deltaX = targetX - currentX;
        double deltaY = targetY - currentY;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        final double ARRIVAL_DISTANCE = 20.0;

        if (distanceToTarget < ARRIVAL_DISTANCE) {
            pathIndex = (pathIndex + 1) % path.size();
            target = path.get((pathIndex + 1) % path.size());
        }

        double speed = slider.getValue() / 6.0;
        double moveX = Math.signum(deltaX) * Math.min(speed, Math.abs(deltaX));
        double moveY = Math.signum(deltaY) * Math.min(speed, Math.abs(deltaY));
        double nextX = currentX + moveX;
        double nextY = currentY + moveY;
        
        // --- Lógica do Vigia ---
        boundsCalculator.setX(nextX);
        boundsCalculator.setY(nextY);
        Bounds futureBounds = boundsCalculator.getBoundsInLocal();
        nextPhysicalRegion = controller.findRegionForBounds(futureBounds);

        if (nextPhysicalRegion != acquiredRegion) {
          System.out.printf("[ROBO %d | UI] MUDANDO DE ROTA COLISORA! Próxima regiao: %-10s | Regiao Atual Adquirida: %s. Sinalizando Torre...%n",id,
            (nextPhysicalRegion != null ? nextPhysicalRegion.getId() : "NENHUMA"),
            (acquiredRegion != null ? acquiredRegion.getId() : "NENHUMA"));
          
          if (movementLatch != null && movementLatch.getCount() > 0) {
            movementLatch.countDown();
          }
          return;
        }

        // --- Movimento ---
        sprite.setLayoutX(nextX);
        sprite.setLayoutY(nextY);
        this.currentX = nextX;
        this.currentY = nextY;
    }

    public void stopRunning() {
        this.running = false;
        if(this.isAlive()) {
            this.interrupt();
        }
    }

    public void resetPosition() {
        this.paused = false;
        this.isClearedToMove = false;
        this.pathIndex = 0;
        if (!path.isEmpty()) {
            Position initialPos = path.get(0);
            this.currentX = initialPos.getX();
            this.currentY = initialPos.getY();
            Platform.runLater(() -> {
                sprite.setLayoutX(initialPos.getX());
                sprite.setLayoutY(initialPos.getY());
            });
        }
        if (movementLatch != null && movementLatch.getCount() > 0) {
            movementLatch.countDown();
        }
    }

    // ... seus outros métodos ...
    public void togglePause() { this.paused = !this.paused; }
    public boolean isPaused() { return paused; }
    public void togglePath() { pathVisual.setVisible(!pathVisual.isVisible()); }
    public boolean isPathVisible() { return pathVisual.isVisible(); }
    public ImageView getSprite() { return sprite; }
}