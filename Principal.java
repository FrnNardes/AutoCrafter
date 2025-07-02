/* ***************************************************************
* Autor............: Fernando Nardes Ferreira Neto
* Matricula........: 202410403
* Inicio...........: 25/06/2025
* Ultima alteracao.: 25/06/2025
* Nome.............: Principal.java
* Funcao...........: Classe principal que inicializa a aplicacao com as 
*                    bibliotecas necessarias para a execucao do JavaFX. 
*                    Abre a primeira tela carregando o FXML.
*************************************************************** */

// Importando as bibliotecas necessarias
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Importando os controllers para a compilacao do programa pelo Principal.java
import controller.SimulationController;

public class Principal extends Application {

  /* ***************************************************************
  * Metodo: start
  * Funcao: Inicializa o stage primario do programa, onde serao exibidas 
  *         as cenas, carregando inicialmente o menu principal.
  * Parametros: primaryStage - o stage principal para as cenas da aplicacao.
  * Retorno: void
  *************************************************************** */
  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/Simulation.fxml")); // Carrega o FXML
    Parent root = loader.load(); // Define o root com o FXML e carrega o controller atraves do metodo load
    Scene scene = new Scene(root); // Define a cena com o root

    Image icon = new Image(getClass().getResourceAsStream("./assets/appIcon.png")); // Define o icone do Stage
    primaryStage.getIcons().add(icon); // Adiciona o icone ao Stage
    
    primaryStage.setScene(scene); // Define a cena no Stage
    primaryStage.setTitle("Smart Warehouse Simulator"); // Define o titulo do Stage
    // primaryStage.initStyle(StageStyle.UNDECORATED); // Estiliza o Stage, removendo as bordas
    primaryStage.setResizable(false); // Define o Stage como nao redimensionavel
    primaryStage.show(); // Exibe o Stage para o usuario
  } // Fim do metodo start

  /* ***************************************************************
  * Metodo: main
  * Funcao: Metodo principal do programa, responsavel por inicializar 
  *         a aplicacao JavaFX.
  * Parametros: args - um array de Strings que guarda os argumentos passados 
  *                     para a aplicacao na execucao.
  * Retorno: void
  *************************************************************** */
  public static void main(String[] args) {
    launch(args);
  } // Fim do metodo main
}// Fim da classe Principal
