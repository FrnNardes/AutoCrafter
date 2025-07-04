package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javafx.animation.AnimationTimer;
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
import util.PathData;

public class SimulationController{
  // Instanciating our FXML objects
  @FXML private ImageView r1, r2, r3, r4, r5, r6, r7, r8;

  @FXML private Slider sliderR1, sliderR2, sliderR3, sliderR4, sliderR5, sliderR6, sliderR7, sliderR8; 

  @FXML private ImageView pathVisual1, pathVisual2, pathVisual3, pathVisual4, pathVisual5, pathVisual6, pathVisual7, pathVisual8;

  @FXML private ImageView pauseButton1, pauseButton2, pauseButton3, pauseButton4, pauseButton5, pauseButton6, pauseButton7, pauseButton8;

  @FXML private ImageView pathButton1, pathButton2, pathButton3, pathButton4, pathButton5, pathButton6, pathButton7, pathButton8;  

  @FXML private Rectangle rec1_1, rec1_2, rec1_3, rec1_4, rec1_5, rec2, rec3_1, rec3_2, rec4_1, rec4_2, rec4_3, rec5, rec6, rec7, rec8, rec9, rec10, rec11, rec12, rec13, rec14;

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
  
  private AnimationTimer simulationTimer;
  private List<Robot> activeRobots = new ArrayList<>();
  private PathData paths = new PathData();

  @FXML
  public void initialize(){
    setupCriticalRegions();
    startSimulation();
  }

  public void startSimulation(){
    stopSimulation();
    activeRobots.clear();

    List<ImageView> robotSprites = Arrays.asList(r1, r2, r3, r4, r5, r6, r7, r8);
    List<ImageView> pathVisuals = Arrays.asList(pathVisual1, pathVisual2, pathVisual3, pathVisual4, pathVisual5, pathVisual6, pathVisual7, pathVisual8);
    List<Slider> sliders = Arrays.asList(sliderR1, sliderR2, sliderR3, sliderR4, sliderR5, sliderR6, sliderR7, sliderR8);
    List<List<Position>> paths = PathData.getPaths();

    for (int i = 0; i < 7; i++) {
      if(i != 2 && i != 3){
        System.out.println(i);
        Robot robot = new Robot(i + 1, robotSprites.get(i), paths.get(i), pathVisuals.get(i), sliders.get(i), this);
        robot.start();
        activeRobots.add(robot);
      }
    }

    configureUI();

    simulationTimer = new AnimationTimer() {
      @Override
      public void handle(long now) {
          activeRobots.forEach(Robot::updateVisuals);
      }
    };
    simulationTimer.start();
  }

  private void stopSimulation() {
    if (simulationTimer != null) {
        simulationTimer.stop();
    }

    for (Robot robot : activeRobots) {
        robot.interrupt();
    }

    for (CriticalRegion region : allCriticalRegions) {
        semaphoresByRegion.put(region, new Semaphore(1));
        occupantsByRegion.put(region, null);
    }
  }

  // --- Critical  Regions ---
  private void setupCriticalRegions() {
    // Robot R4 x R5
    List<Rectangle> pathR12 = Arrays.asList(rec1_1, rec1_2, rec1_3, rec1_4, rec1_5);
    CriticalRegion cr1 = new CriticalRegion("regiao1", pathR12);

    List<Rectangle> pathR12_1 = Arrays.asList(rec2);
    CriticalRegion cr2 = new CriticalRegion("regiao2", pathR12_1);

    List<Rectangle> pathR12_2 = Arrays.asList(rec3_1, rec3_2);
    CriticalRegion cr3 = new CriticalRegion("regiao3", pathR12_2);

    List<Rectangle> pathR12_3 = Arrays.asList(rec4_1, rec4_2, rec4_3);
    CriticalRegion cr4 = new CriticalRegion("regiao4", pathR12_3);

    List<Rectangle> pathR12_4 = Arrays.asList(rec5);
    CriticalRegion cr5 = new CriticalRegion("regiao5", pathR12_4);

    List<Rectangle> pathR12_5 = Arrays.asList(rec6);
    CriticalRegion cr6 = new CriticalRegion("regiao6", pathR12_5);

    List<Rectangle> pathR12_6 = Arrays.asList(rec7);
    CriticalRegion cr7 = new CriticalRegion("regiao7", pathR12_6);

    List<Rectangle> pathR12_7 = Arrays.asList(rec8);
    CriticalRegion cr8 = new CriticalRegion("regiao7", pathR12_7);

    List<Rectangle> pathR12_8 = Arrays.asList(rec9);
    CriticalRegion cr9 = new CriticalRegion("regiao7", pathR12_8);

    List<Rectangle> pathR12_9 = Arrays.asList(rec10);
    CriticalRegion cr10 = new CriticalRegion("regiao7", pathR12_9);

    List<Rectangle> pathR12_10 = Arrays.asList(rec11);
    CriticalRegion cr11 = new CriticalRegion("regiao7", pathR12_10);

    List<Rectangle> pathR12_11 = Arrays.asList(rec12);
    CriticalRegion cr12 = new CriticalRegion("regiao7", pathR12_11);

    List<Rectangle> pathR12_12 = Arrays.asList(rec13);
    CriticalRegion cr13 = new CriticalRegion("regiao7", pathR12_12);

    List<Rectangle> pathR12_13 = Arrays.asList(rec14);
    CriticalRegion cr14 = new CriticalRegion("regiao7", pathR12_13);

    List<CriticalRegion> criticalRegions = Arrays.asList(cr1, cr2, cr3, cr4, cr5, cr6, cr7, cr8, cr9, cr10, cr11, cr12, cr13, cr14);
    allCriticalRegions.addAll(criticalRegions);

    for(CriticalRegion region : allCriticalRegions){
      semaphoresByRegion.put(region, new Semaphore(1));
      occupantsByRegion.put(region, null);
    }
  }

  // --- Critical Regions Methods ---
  // Retorna o semáforo associado a uma região específica.
  public Semaphore getSemaphoreForRegion(CriticalRegion region) {
      return semaphoresByRegion.get(region);
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
      List<ImageView> pauseButtons = Arrays.asList(pauseButton1, pauseButton2, pauseButton3, pauseButton4, pauseButton5, pauseButton6, pauseButton7, pauseButton8);
      List<ImageView> pathButtons = Arrays.asList(pathButton1, pathButton2, pathButton3, pathButton4, pathButton5, pathButton6, pathButton7, pathButton8);
      List<ImageView> pathVisuals = Arrays.asList(pathVisual1, pathVisual2, pathVisual3, pathVisual4, pathVisual5, pathVisual6, pathVisual7, pathVisual8);

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
