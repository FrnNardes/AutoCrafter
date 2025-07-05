package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.animation.AnimationTimer;
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
import util.Direction;
import util.PathData;

public class SimulationController{
  // Instanciating our FXML objects
  @FXML private ImageView r1, r2, r3, r4, r5, r6, r7, r8;

  @FXML private Slider sliderR1, sliderR2, sliderR3, sliderR4, sliderR5, sliderR6, sliderR7, sliderR8; 

  @FXML private ImageView pathVisual1, pathVisual2, pathVisual3, pathVisual4, pathVisual5, pathVisual6, pathVisual7, pathVisual8;

  @FXML private ImageView pauseButton1, pauseButton2, pauseButton3, pauseButton4, pauseButton5, pauseButton6, pauseButton7, pauseButton8;

  @FXML private ImageView pathButton1, pathButton2, pathButton3, pathButton4, pathButton5, pathButton6, pathButton7, pathButton8;  

  @FXML private Rectangle r1_1, r1_2, r1_3, r1_4, r1_5, r1_6, r1_7, r1_8, r1_9, r1_10, r1_11, r1_12, r1_13;
  @FXML private Rectangle r2_1, r2_2, r2_3;
  @FXML private Rectangle r4_1, r4_2;
  @FXML private Rectangle r5_1, r5_2, r5_3, r5_4, r5_5, r5_6, r5_7, r5_8;
  @FXML private Rectangle rx_1, rx_2, rx_3, rx_4, rx_5, rx_6;

  @FXML private AnchorPane simulationPane;

  // Loading images for the buttons
  private final Image pause_button = new Image(getClass().getResourceAsStream("/assets/pause_button.png"));
  private final Image play_button = new Image(getClass().getResourceAsStream("/assets/play_button.png"));
  private final Image path_button_disable = new Image(getClass().getResourceAsStream("/assets/path_button_disable.png"));
  private final Image path_button_enable = new Image(getClass().getResourceAsStream("/assets/path_button_enable.png"));

  // --- Colision Controll ---
  private List<CriticalRegion> allCriticalRegions = new ArrayList<>();

  // --- Animation ---
  private AnimationTimer simulationTimer;
  private List<Robot> activeRobots = new ArrayList<>();

  @FXML
  public void initialize(){
    setupCriticalRegions();
    startSimulation();
  }

  // --- Critical  Regions ---
  private void setupCriticalRegions() {

    // Solitary 1
    List<Rectangle> recR1 = Arrays.asList(rx_1);
    CriticalRegion regionS1 = new CriticalRegion("Solitary_1", recR1);

    // Solitary 2
    List<Rectangle> recR2 = Arrays.asList(rx_2);
    CriticalRegion regionS2 = new CriticalRegion("Solitary_1", recR2);

    // Solitary 3
    List<Rectangle> recR3 = Arrays.asList(rx_3);
    CriticalRegion regionS3 = new CriticalRegion("Solitary_1", recR3);

    // Solitary 4
    List<Rectangle> recR4 = Arrays.asList(rx_4);
    CriticalRegion regionS4 = new CriticalRegion("Solitary_1", recR4);

    // Solitary 4
    List<Rectangle> recR5 = Arrays.asList(rx_5);
    CriticalRegion regionS5 = new CriticalRegion("Solitary_1", recR5);

     // Solitary 4
    List<Rectangle> recR6 = Arrays.asList(rx_6);
    CriticalRegion regionS6 = new CriticalRegion("Solitary_1", recR6);

    // Region 1
    List<Rectangle> rectsR1 = Arrays.asList(r1_1, r1_2, r1_3, r1_4, r1_5, r1_6, r1_7, r1_8, r1_9, r1_10, r1_11, r1_12, r1_13);
    CriticalRegion region1 = new CriticalRegion("Region_1", rectsR1);

    // Region 2
    List<Rectangle> rectsR2 = Arrays.asList(r2_1, r2_2, r2_3);
    CriticalRegion region2 = new CriticalRegion("Region_2", rectsR2, true);

    // Region 3
    //List<Rectangle> rectsR3 = Arrays.asList(r3_1, r3_2, r3_3, r3_4);
    //CriticalRegion region3 = new CriticalRegion("Region_3", rectsR3);
    
    // Region 4
    List<Rectangle> rectsR4 = Arrays.asList(r4_1, r4_2);
    CriticalRegion region4 = new CriticalRegion("Region_4", rectsR4);

    // Region 5
    List<Rectangle> rectsR5 = Arrays.asList(r5_1, r5_2, r5_3, r5_4, r5_5, r5_6, r5_7, r5_8);
    CriticalRegion region5 = new CriticalRegion("Region_5", rectsR5);

    allCriticalRegions.addAll(Arrays.asList(regionS1, regionS2, regionS3, regionS4, regionS5, regionS6, region1, region2, region4, region5));
  }

  public void startSimulation(){
    stopSimulation();
    activeRobots.clear();

    // Grouping objects as lists
    List<ImageView> robotSprites = Arrays.asList(r1, r2, r3, r4, r5, r6, r7, r8);
    List<ImageView> pathVisuals = Arrays.asList(pathVisual1, pathVisual2, pathVisual3, pathVisual4, pathVisual5, pathVisual6, pathVisual7, pathVisual8);
    List<Slider> sliders = Arrays.asList(sliderR1, sliderR2, sliderR3, sliderR4, sliderR5, sliderR6, sliderR7, sliderR8);

    List<List<Position>> paths = PathData.getPaths();

    Robot robot;
    for (int i = 0; i < 6; i++) { // Loop to create 8 robots
      if(i == 2){  
        robot = new Robot(i + 1, robotSprites.get(i), paths.get(i), pathVisuals.get(i), sliders.get(i), Direction.COUNTER_CLOCKWISE, this); 
        robot.start();
        activeRobots.add(robot);
      } else if (i == 1) {
        robot = new Robot(i + 1, robotSprites.get(i), paths.get(i), pathVisuals.get(i), sliders.get(i), Direction.CLOCKWISE, this); 
        robot.start();
        activeRobots.add(robot);
      }
    }

    configureUI(); // Setup all visual paths and buttons

    // Runs a animation timer for "draw" the images of the scene
    simulationTimer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        activeRobots.forEach(Robot::updateVisuals);
      }
    };
    simulationTimer.start();
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

  private void configureUI() {
    List<ImageView> pathVisuals = Arrays.asList(pathVisual1, pathVisual2, pathVisual3, pathVisual4, pathVisual5, pathVisual6, pathVisual7, pathVisual8);
    List<ImageView> pauseButtons = Arrays.asList(pauseButton1, pauseButton2, pauseButton3, pauseButton4, pauseButton5, pauseButton6, pauseButton7, pauseButton8);
    List<ImageView> pathButtons = Arrays.asList(pathButton1, pathButton2, pathButton3, pathButton4, pathButton5, pathButton6, pathButton7, pathButton8);
    for (int i = 0; i < activeRobots.size(); i++) {
        final Robot robot = activeRobots.get(i);
        final ImageView pauseBtn = pauseButtons.get(i);
        final ImageView pathBtn = pathButtons.get(i);

        pathVisuals.get(i).setVisible(false);
        pathBtn.setImage(path_button_disable);
        pauseBtn.setImage(pause_button);

        pauseBtn.setOnMouseClicked(event -> {
          robot.togglePause();
          pauseBtn.setImage(robot.isPaused() ? play_button : pause_button);
        });
        pathBtn.setOnMouseClicked(event -> {
          robot.togglePath();
          pathBtn.setImage(robot.isPathVisible() ? path_button_enable : path_button_disable);
        });
        applyButtonAnimation(pauseBtn, pathBtn);
    }
  }

  private void stopSimulation() {
    if (simulationTimer != null) {
      simulationTimer.stop();
    }

    for (Robot robot : activeRobots) {
      robot.resetPosition();
      robot.interrupt();
    }
  }

  @FXML
  public void reset(){
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
}
