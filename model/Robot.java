package model;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import controller.SimulationController;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import util.Direction;

public class Robot extends Thread {
    // --- Atributos ---
    private final int id;
    private final ImageView sprite;
    private final ImageView pathVisual;
    private final List<Position> path;
    private final SimulationController controller;
    private final Slider slider;

    // --- Sincronizacao e Estado ---
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private volatile int pathIndex = 0;
    private volatile double currentX;
    private volatile double currentY;
    private final double ARRIVAL_DISTANCE = 5.0;

    // --- Colision System ---
    private final Direction direction;
    private volatile Bounds futureBounds;
    private volatile Bounds currentBounds;
    private volatile CountDownLatch movementLatch;
    private volatile boolean isClearedToMove = false;
    private Rectangle acquiredStreet = null;
    private Rectangle nextPhysicalStreet = null;

    private CriticalRegion currentRegion = null;
    private CriticalRegion futureRegion = null;

    private final Rectangle boundsCalculator = new Rectangle(40, 40);

    public Robot(int id, ImageView sprite, List<Position> path, ImageView pathVisual, Slider slider, Direction direction, SimulationController controller) {
        this.id = id;
        this.sprite = sprite;
        this.path = path;
        this.pathVisual = pathVisual;
        this.slider = slider;
        this.controller = controller;
        this.direction = direction;

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

    public void run() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                this.movementLatch = new CountDownLatch(1);
                this.isClearedToMove = true;

                movementLatch.await();
                if (!running) break;
                
                this.isClearedToMove = false; 
                System.out.printf("[ROBO %d | RUN] SINAL RECEBIDO!%n", id);
                System.out.printf("[ROBO %d | RUN] ANALISANDO - Estado: Adquirida= [%s] | Próxima= [%s]%n",
                        id,
                        (acquiredStreet != null ? acquiredStreet.getId() : "NENHUMA"),
                        (nextPhysicalStreet != null ? nextPhysicalStreet.getId() : "NENHUMA"));

                if (futureRegion != null) {
                    while (futureRegion.getDirection() != Direction.NONE && futureRegion.getDirection() != this.getDirection()) {
                        System.out.printf("[ROBO %d | RUN] BLOQUEADO. Direção da região é %s.%n", id, futureRegion.getDirection());
                        Thread.sleep(1000);
                    }

                    while (futureRegion.isEgoist() && !futureRegion.getOccupants().contains(this) && futureRegion.getOccupants().size() > 0) {
                        System.out.printf("[ROBO %d | RUN] REGIAO EGOISTA BLOQUEADA.%n", id);
                        Thread.sleep(1000);
                    }
                }

                if (nextPhysicalStreet != acquiredStreet) {
                    releaseAcquiredStreet();
                    acquireTargetRegion(nextPhysicalStreet);
                }
                
                System.out.printf("[ROBO %d | RUN] ANALISE COMPLETA. Robo .%n", id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("[ROBO %d | RUN] Thread interrompida.%n", id);
        } finally {
            System.out.printf("[ROBO %d | RUN] Fim da thread. Garantindo liberacao final...%n", id);
            releaseAcquiredStreet(); 
        }
    }

    private void releaseAcquiredStreet() {
        if (acquiredStreet != null) {
            System.out.printf("[ROBO %d | RUN] Liberando semáforo da regiao %s...%n", id, acquiredStreet.getId());
            controller.findRegionForBounds(currentBounds).getSemaphoreForRec(acquiredStreet).release();
            this.acquiredStreet = null;
            if(nextPhysicalStreet == null){
                currentRegion.removeOccupant(this);
                if(currentRegion.getOccupants().size() == 0){
                    currentRegion.setDirection(Direction.NONE);
                    System.out.println("[ROBO " + id + " | RUN] Mudando direcao da "+ currentRegion.getId() + " - " + currentRegion.getDirection());
                }
            }
        }
    }

    private void acquireTargetRegion(Rectangle streetToAcquire) throws InterruptedException {
        if (streetToAcquire != null) {
            System.out.printf("[ROBO %d | RUN] Adquirindo semáforo da regiao %s...%n", id, streetToAcquire.getId());
            
            futureRegion.getSemaphoreForRec(streetToAcquire).acquire();
            this.acquiredStreet = streetToAcquire;
            
            if(!futureRegion.getOccupants().contains(this)){
                futureRegion.addOccupant(this);
            }

            futureRegion.setDirection(this.direction);
            System.out.println("[ROBO " + id + " | RUN] Mudando direcao da "+ futureRegion.getId() + " - " + futureRegion.getDirection());
            System.out.printf("[ROBO %d | RUN] SUCESSO! Posse da regiao %s confirmada.%n", id, acquiredStreet.getId());
        } else {
            System.out.printf("[ROBO %d | RUN] ROBO EM AREA LIVRE.%n", id);
        }
    }

    public void updateVisuals() {
        if (paused || !isClearedToMove) {
            return;
        }
        
        // --- Lógica de Movimento ---
        currentX = sprite.getLayoutX();
        currentY = sprite.getLayoutY();

        boundsCalculator.setX(currentX);
        boundsCalculator.setY(currentY);
        currentBounds = boundsCalculator.getBoundsInLocal();

        currentRegion = controller.findRegionForBounds(currentBounds);

        Position target = path.get((pathIndex + 1) % path.size());

        double targetX = target.getX();
        double targetY = target.getY();
        double deltaX = targetX - currentX;
        double deltaY = targetY - currentY;
        double distanceToTarget = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

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
        futureBounds = boundsCalculator.getBoundsInLocal();

        futureRegion = controller.findRegionForBounds(futureBounds);

        if(futureRegion != null){
            nextPhysicalStreet = futureRegion.findRecForBounds(futureBounds);
        }

        if (nextPhysicalStreet != acquiredStreet) {
          System.out.printf("[ROBO %d | UI] MUDANDO DE ROTA COLISORA! Estado: Proxima Regiao = [%s] | Regiao Atual Adquirida = [%s]%n",id,
            (nextPhysicalStreet != null ? nextPhysicalStreet.getId() : "NENHUMA"),
            (acquiredStreet != null ? acquiredStreet.getId() : "NENHUMA"));
          
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
        this.acquiredStreet = null;
        this.nextPhysicalStreet = null;
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
    public Direction getDirection() { return direction; }
}