package model;

import java.util.List;

import controller.SimulationController;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;

public class Robot  extends Thread{
  private int id;
  private List<Position> path;
  private ImageView pathVisual;
  private ImageView sprite;
  private Slider slider;

  private SimulationController controller;

  // NOVO ATRIBUTO: 
  private CriticalRegion currentAcquiredCriticalRegion = null;

  private volatile boolean running = true;
  private volatile boolean paused = false;

  private int pathIndex = 0;

  public Robot(int id, ImageView sprite, List<Position> path, ImageView pathVisual, Slider slider, SimulationController controller){
    this.id = id;
    this.sprite = sprite;
    this.path = path;
    this.pathVisual = pathVisual;
    this.slider = slider;
    this.controller = controller;
    sprite.setFitWidth(40); 
    sprite.setFitHeight(40);
  }

  @Override
  public void run(){
    try {
      
      Platform.runLater(() -> {
        sprite.setLayoutX(path.get(0).getX());
        sprite.setLayoutY(path.get(0).getY());
      });

      Thread.sleep(1000); // Pausa para a UI renderizar a posição inicial
      // --- LÓGICA DE AQUISIÇÃO DA REGIÃO INICIAL ---
      CriticalRegion initialRegion = controller.findRegionForBounds(sprite.getBoundsInParent());
      if (initialRegion != null) {
        try {
          System.out.println("Robô " + id + " try access...");
          controller.getSemaphoreForRegion(initialRegion).acquire();
          System.out.println("Robô " + id + " ACQUIRE REGION " + initialRegion.getId());
          currentAcquiredCriticalRegion = initialRegion;
          controller.setRegionOccupied(currentAcquiredCriticalRegion, this); // Registra no controlador!
        } catch (InterruptedException e) {
          // Se for interrompido no spawn, é importante lidar com isso.
          Thread.currentThread().interrupt();
          running = false;
          return; // Sai do método run se for interrompido logo no início
        }
      }
      // --- FIM DA LÓGICA DE AQUISIÇÃO DA REGIÃO INICIAL ---

      while(running){
        Position target = path.get((pathIndex + 1) % path.size());

        int currentX = (int) sprite.getLayoutX();
        int currentY = (int) sprite.getLayoutY();

        while(currentX != target.getX() || currentY != target.getY() && running && !paused){
          if(paused){
            Thread.sleep(100);
            continue;
          }
          // Calcula o próximo pixel para onde o robô vai se mover
          int nextX = currentX - Integer.compare(currentX, target.getX());
          int nextY = currentY - Integer.compare(currentY, target.getY());

          // --- 1. Lógica de Aquisição Preventiva (Baseado no seu input) ---
          // Criar Bounds para a **PRÓXIMA POSIÇÃO** do robô.
          
          Bounds futureBounds = new javafx.geometry.BoundingBox(nextX, nextY, sprite.getFitWidth(), sprite.getFitHeight());

          // Identificar a região crítica que a **PRÓXIMA POSIÇÃO** vai intersectar.
          CriticalRegion potentialNextCriticalRegion = controller.findRegionForBounds(futureBounds);
          
          if(potentialNextCriticalRegion != null && potentialNextCriticalRegion != currentAcquiredCriticalRegion){
            try {
              // Esta linha BLOQUEIA a thread do robô aqui se o semáforo não estiver livre.
              System.out.println("Robô " + id + " try access...");
              controller.getSemaphoreForRegion(potentialNextCriticalRegion).acquire();
             // CORREÇÃO: Primeiro, registre no controlador que a NOVA região está ocupada por este robô.
              controller.setRegionOccupied(potentialNextCriticalRegion, this);
              
              // Segundo, atualize o atributo interno do robô para refletir a nova região que ele adquiriu.
              currentAcquiredCriticalRegion = potentialNextCriticalRegion; 
              System.out.println("Robô " + id + " ACQUIRE REGION " + potentialNextCriticalRegion.getId());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              running = false;
            }
          }

          currentX = nextX;
          currentY = nextY;

          int finalX = currentX;
          int finalY = currentY;

          Platform.runLater(() -> {
            sprite.setLayoutX(finalX);
            sprite.setLayoutY(finalY);
          });

          // --- 3. Lógica de Liberação (Depois que o robô se moveu para fora de uma região) ---
          // Pega os limites da sprite NA POSIÇÃO ATUAL (depois de mover este pixel)
          Bounds currentSpriteBounds = sprite.getBoundsInParent();
          // Pergunta ao controlador qual região crítica ele está ocupando AGORA.
          CriticalRegion currentDetectedRegion = controller.findRegionForBounds(currentSpriteBounds);

          // Se a região **DETETADA** agora é diferente da região **ADQUIRIDA** anteriormente,
          // e a região ADQUIRIDA não é nula (ele estava em uma RC)
          if (currentAcquiredCriticalRegion != null) {
    // Verifica se ele *saiu* da região que ele adquiriu
    boolean stillIntersectsAcquiredRegion = currentAcquiredCriticalRegion.intersects(currentSpriteBounds);

    // Se ele não intersecta mais a região adquirida OU se ele entrou em uma região *diferente*
    // da que ele adquiriu (e essa nova região *não* é nula, ou seja, ele entrou em outra RC)
    if (!stillIntersectsAcquiredRegion || (currentDetectedRegion != null && currentDetectedRegion != currentAcquiredCriticalRegion)) {
        // Libera o semáforo da região que ele estava segurando
        controller.getSemaphoreForRegion(currentAcquiredCriticalRegion).release();
        System.out.println("Robô " + id + " RELEASE " + currentAcquiredCriticalRegion.getId() + "!");
        controller.setRegionOccupied(currentAcquiredCriticalRegion, null); // Limpa o registro de ocupante
        
        // Agora, atualiza currentAcquiredCriticalRegion para a nova região (se houver)
        // ou para null se ele saiu para uma área não crítica.
        currentAcquiredCriticalRegion = currentDetectedRegion;
        // Se ele entrou em uma nova região, já deveria ter adquirido o semáforo dela na etapa 1.
        // Apenas precisamos atualizar o currentAcquiredCriticalRegion para refletir isso.
        if (currentAcquiredCriticalRegion != null) {
            // Se ele adquiriu na etapa 1, o ocupante já foi setado.
            // Se não, e ele está nela, algo está errado, mas a lógica de acquire deve ter pego.
            // Apenas para garantir, se ele está nela e não é o ocupante, setamos.
            if (controller.getRegionOccupant(currentAcquiredCriticalRegion) != this) {
                controller.setRegionOccupied(currentAcquiredCriticalRegion, this);
            }
        }
    }
} else { // Se ele não possui nenhuma região adquirida atualmente
    // Mas ele acabou de entrar em uma nova região crítica
    if (currentDetectedRegion != null) {
        // Esta é a primeira aquisição ou uma re-aquisição após ter liberado tudo
        // Ele já deveria ter tentado adquirir na etapa 1 (potentialNextCriticalRegion)
        currentAcquiredCriticalRegion = currentDetectedRegion;
        // Ocupante já deve ter sido setado na etapa 1.
    }
}
          
          Thread.sleep((long) (2 * slider.getValue()));
        }

        if(!paused){
          pathIndex = (pathIndex + 1) % path.size(); // Go to next point
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Respeita a interrupção da thread
      running = false;
    }
  }

  
  public void togglePath(){
    if(isPathVisible()){
      pathVisual.setVisible(false);
    } else {
      pathVisual.setVisible(true);
    }
  }
  
  public boolean isPathVisible(){
    return pathVisual.isVisible();
  }
  

  /* ***************************************************************
  * Method: togglePause
  * Funcao: Changes the robot's pause state
  * Parameters: none
  * Return: void
  *************************************************************** */
  public void togglePause() {
    this.paused = !this.paused;
  }

  /* ***************************************************************
  * Method: isPaused
  * Function: Returns the robot's pause state
  * Parameters: none
  * Retorno: boolean - true if its paused, false otherwise
  *************************************************************** */
  public boolean isPaused() {
    return paused;
  }

  /* ***************************************************************
  * Method: kill
  * Function: Stops robot execution, resets sliders and interrept the thread.
  * Parameters: none
  * Return: void
  *************************************************************** */

  public int getRobotId(){
    return id;
  }

  public ImageView getSprite(){
    return sprite;
  }

  public void kill(){
    this.slider.setValue(6);
    Platform.runLater(() -> {
      pathVisual.setVisible(false);
      sprite.setLayoutX(path.get(0).getX());
      sprite.setLayoutY(path.get(0).getY());
    }); 
    this.running = false;
    this.interrupt();
  }
}
