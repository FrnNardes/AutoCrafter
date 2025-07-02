package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Bounds;
import model.CriticalRegion;
import model.Position;
import model.Robot;

public class SimulationController{
  // Instanciating our FXML objects
  @FXML private ImageView r1, r2, r3, r4, r5, r6, r7, r8;

  @FXML private Slider sliderR1, sliderR2, sliderR3, sliderR4, sliderR5, sliderR6, sliderR7, sliderR8; 

  @FXML private ImageView pathVisual1, pathVisual2, pathVisual3, pathVisual4, pathVisual5, pathVisual6, pathVisual7, pathVisual8;

  @FXML private ImageView pauseButton1, pauseButton2, pauseButton3, pauseButton4, pauseButton5, pauseButton6, pauseButton7, pauseButton8;

  @FXML private ImageView pathButton1, pathButton2, pathButton3, pathButton4, pathButton5, pathButton6, pathButton7, pathButton8;  

  // ROBOT 1 x 2 - 0
  @FXML private Rectangle recP12_1, recP12_2, recP12_3, recP12_4, recP12_5;

  // ROBOT 1 x 2 - 1
  @FXML private Rectangle recP12_1_1;

  // ROBOT 1 x 2 - 2
  @FXML private Rectangle recP12_2_1, recP12_2_2, recP12_2_3;

  @FXML private AnchorPane simulationPane;

  // Loading images for the buttons
  private final Image pause_button = new Image(getClass().getResourceAsStream("/assets/pause_button.png"));
  private final Image play_button = new Image(getClass().getResourceAsStream("/assets/play_button.png"));
  private final Image path_button_disable = new Image(getClass().getResourceAsStream("/assets/path_button_disable.png"));
  private final Image path_button_enable = new Image(getClass().getResourceAsStream("/assets/path_button_enable.png"));

  // --- Colision Controll ---
  private Map<CriticalRegion, Semaphore> semaphoresByRegion = new HashMap<>();
  private Map<CriticalRegion, Robot> occupantsByRegion = new HashMap<>();
  private List<CriticalRegion> allCriticalRegions = new ArrayList<>();
  
  private List<Robot> activeRobots = new ArrayList<>();;

  // Defining each path
  List<Position> path1 = Arrays.asList(
    new Position(150, 660),
    new Position(150, 0),
    new Position(810, 0),
    new Position(810, 263),
    new Position(546, 263),
    new Position(546, 393),
    new Position(678, 393),
    new Position(678, 528),
    new Position(810, 528),
    new Position(810, 660),
    new Position(546, 660),
    new Position(546, 528),
    new Position(414, 528),
    new Position(414, 660)

  );

  List<Position> path2 = Arrays.asList(
    new Position(546, 263),
    new Position(414, 263),
    new Position(414, 660),
    new Position(150, 660),
    new Position(150, 0),
    new Position(810, 0),
    new Position(810, 660),
    new Position(546, 660)
  );

  List<Position> path3 = Arrays.asList(
    new Position(282, 0),
    new Position(282, 653),
    new Position(546, 653),
    new Position(546, 0)
  );

  List<Position> path4 = Arrays.asList(
    new Position(157, 0),
    new Position(157, 653),
    new Position(414, 653),
    new Position(414, 0)
  );

  List<Position> path5 = Arrays.asList(
    new Position(414, 0),
    new Position(810, 0),
    new Position(810, 392),
    new Position(414, 392)
  );

  List<Position> path6 = Arrays.asList(
    new Position(810, 653),
    new Position(546, 653),
    new Position(546, 0),
    new Position(810, 0)
  );

  List<Position> path7 = Arrays.asList(
    new Position(414, 528),
    new Position(157, 528),
    new Position(157, 137),
    new Position(414, 137),
    new Position(414, 0),
    new Position(546, 0),
    new Position(546, 137),
    new Position(810, 137),
    new Position(810, 528),
    new Position(546, 528),
    new Position(546, 653),
    new Position(414, 653)
  );

  List<Position> path8 = Arrays.asList(
    new Position(414, 137),
    new Position(546, 137),
    new Position(546, 263),
    new Position(678, 263),
    new Position(678, 393),
    new Position(810, 393),
    new Position(810, 653),
    new Position(546, 653),
    new Position(546, 528),
    new Position(414, 528),
    new Position(414, 393),
    new Position(282, 393),
    new Position(282, 263),
    new Position(157, 263),
    new Position(157, 0),
    new Position(414, 0)
  );

  // --- Critical  Regions ---
  private void setupCriticalRegions() {
    // Robot R4 x R5
    List<Rectangle> pathR12 = Arrays.asList(recP12_1, recP12_2, recP12_3, recP12_4, recP12_5);
    CriticalRegion cr1 = new CriticalRegion("pathR12", pathR12);

    List<Rectangle> pathR12_1 = Arrays.asList(recP12_1_1);
    CriticalRegion cr2 = new CriticalRegion("pathR12_1", pathR12_1);

    List<Rectangle> pathR12_2 = Arrays.asList(recP12_2_1, recP12_2_2, recP12_2_3);
    CriticalRegion cr3 = new CriticalRegion("pathR12_2", pathR12_2);


    List<CriticalRegion> criticalRegions = Arrays.asList(cr1, cr2, cr3);
    allCriticalRegions.addAll(criticalRegions);

    for(CriticalRegion region : allCriticalRegions){
      for(Rectangle rec : region.getCriticalZones()){
        if (!simulationPane.getChildren().contains(rec)) {
          simulationPane.getChildren().add(rec);
        }
        rec.toBack();
      }
      semaphoresByRegion.put(region, new Semaphore(1));
      occupantsByRegion.put(region, null);
    }
  }

  // --- Critical Regions Methods ---
  // Retorna o semáforo associado a uma região específica.
  public Semaphore getSemaphoreForRegion(CriticalRegion region) {
      return semaphoresByRegion.get(region);
  }

  // Registra qual robô está ocupando uma região (ou null se está desocupando).
  public void setRegionOccupied(CriticalRegion region, Robot robot) {
      if (occupantsByRegion.containsKey(region)) { // Garante que a região existe no mapa
          occupantsByRegion.put(region, robot);
      }
  }

  // Retorna o robô que está ocupando uma região (ou null se vazia).
  public Robot getRegionOccupant(CriticalRegion region) {
      return occupantsByRegion.get(region);
  }

  // Método para encontrar a região crítica baseada nos limites do robô.
  public CriticalRegion findRegionForBounds(Bounds robotBounds) {
    for (CriticalRegion criticalRegion : allCriticalRegions) {
      if (criticalRegion.intersects(robotBounds)) { // Usa o novo método intersects da CriticalRegion
        return criticalRegion;
      }
    }
    return null;
  }

  public void startSimulation(){
    activeRobots.clear();
    
    // Creating 8 robots
    List<Robot> robots = Arrays.asList(
      new Robot(1, r1, path1, pathVisual1, sliderR1, this),
      new Robot(2, r2, path2, pathVisual2, sliderR2, this)
      //new Robot(3, r3, path3, pathVisual3, sliderR3, this),
      //new Robot(4, r4, path4, pathVisual4, sliderR4, this),
      //new Robot(5, r5, path5, pathVisual5, sliderR5, this),
      //new Robot(6, r6, path6, pathVisual6, sliderR6, this),
      //new Robot(7, r7, path7, pathVisual7, sliderR7, this),
      //new Robot(8, r8, path8, pathVisual8, sliderR8, this)
    );

    List<ImageView> pauseButtons = Arrays.asList(pauseButton1, pauseButton2, pauseButton3, pauseButton4, pauseButton5, pauseButton6, pauseButton7, pauseButton8);
    List<ImageView> pathButtons = Arrays.asList(pathButton1, pathButton2, pathButton3, pathButton4, pathButton5, pathButton6, pathButton7, pathButton8);

    // Connecting each button with the respective robot
    for(int i = 0; i < robots.size(); i++){
      final Robot robot = robots.get(i);
      ImageView pauseButton = pauseButtons.get(i);
      ImageView pathButton = pathButtons.get(i);

      pauseButton.setImage(pause_button);
      pathButton.setImage(path_button_disable);
      applyButtonAnimation(pauseButton, pathButton);

      // Define a acao de clique para o botao de pausa
      pauseButton.setOnMouseClicked(event -> {
        robot.togglePause(); // Calls the pause method of the corresponding thread
        if(robot.isPaused()){ // Change the button image between 'play' and 'pause
          pauseButton.setImage(play_button);
        } else{
          pauseButton.setImage(pause_button);
        }
      });

      // Define a acao de clique para o botao de path
      pathButton.setOnMouseClicked(event -> {
        robot.togglePath(); // Calls the pause method of the corresponding thread
        if(robot.isPathVisible()){ // Change the button image between 'enable' and 'disable'
          pathButton.setImage(path_button_enable);
        } else{
          pathButton.setImage(path_button_disable);
        }
      });
    }

    for(Robot robot : robots){
      robot.setDaemon(true); 
      robot.start();
      Platform.runLater(() -> robot.getSprite().toFront()); 
    }
    activeRobots.addAll(robots);
  }

  public void stopSimulation(){
    for(Robot robot : activeRobots){
      robot.kill();
    }
    activeRobots.clear();

    for (CriticalRegion region : allCriticalRegions) {
        semaphoresByRegion.put(region, new Semaphore(1));
        occupantsByRegion.put(region, null);
    }
  }

  @FXML
  public void reset(){
    stopSimulation();
    startSimulation();
  }

  /* ***************************************************************
  * Metodo: aplicarAnimacaoBotao
  * Funcao: Adiciona efeitos visuais de hover e clique a um conjunto
  * de ImageViews para que se comportem como botoes.
  * Parametros: ImageViews - um array de uma ou mais ImageViews.
  * Retorno: void
  *************************************************************** */
  public void applyButtonAnimation(ImageView... ImageViews) {
    ColorAdjust efeitoHover = new ColorAdjust();
    efeitoHover.setBrightness(0.4); 
    ColorAdjust efeitoPressionado = new ColorAdjust();
    efeitoPressionado.setBrightness(-0.4);

    for(ImageView imageView : ImageViews){
      imageView.setOnMouseEntered(e -> imageView.setEffect(efeitoHover)); 
      imageView.setOnMouseExited(e -> imageView.setEffect(null));
      imageView.setOnMousePressed(e -> imageView.setEffect(efeitoPressionado));
      imageView.setOnMouseReleased(e -> imageView.setEffect(efeitoHover));
    }// Fim do For
  }// Fim do metodo aplicarAnimacaoBotao

  @FXML
  public void initialize(){
    setupCriticalRegions();
    startSimulation();
  }
}
