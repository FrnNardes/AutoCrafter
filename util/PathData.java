/* ***************************************************************
* Autor............: Fernando Nardes Ferreira Neto
* Matricula........: 202410403
* Inicio...........: 25/06/2025
* Ultima alteracao.: 02/06/2025
* Nome.............: PathData.java
* Funcao...........: Util class that keeps track of every path
*************************************************************** */
package  util;

import model.Position;
import java.util.Arrays;
import java.util.List;

public class PathData {
  /* ***************************************************************
  * Method: getPaths
  * Function: Get all the 8 possible paths
  * Parameters: none
  * Returns: List<List<Position>>
  *************************************************************** */
  public static List<List<Position>> getPaths() {
    List<Position> path1 = Arrays.asList( // Path for Robot 1
      new Position(810, 0),
      new Position(810, 132),
      new Position(810, 263), 
      new Position(546, 263), 
      new Position(546, 395),
      new Position(678, 395), 
      new Position(678, 528), 
      new Position(810, 528),
      new Position(810, 660),
      new Position(678, 660),
      new Position(546, 660), 
      new Position(546, 528),
      new Position(414, 528), 
      new Position(414, 660),
      new Position(282, 660),
      new Position(150, 660), 
      new Position(150, 0)
    );

    List<Position> path2 = Arrays.asList( // Path for Robot 2
      new Position(546, 263), 
      new Position(414, 263), 
      new Position(414, 395), 
      new Position(414, 528), 
      new Position(414, 660),
      new Position(282, 660),
      new Position(150, 660), 
      new Position(150, 528), 
      new Position(150, 395), 
      new Position(150, 263), 
      new Position(150, 132),  
      new Position(150, 0),
      new Position(810, 0),
      new Position(810, 132),
      new Position(810, 263),
      new Position(810, 395),
      new Position(810, 528),
      new Position(810, 660), 
      new Position(678, 660), 
      new Position(546, 660),
      new Position(546, 528),
      new Position(546, 395)
    );

    List<Position> path3 = Arrays.asList( // Path for Robot 3
      new Position(282, 132), 
      new Position(282, 660),
      new Position(414, 660),
      new Position(546, 660), 
      new Position(546, 0),
      new Position(282, 0)
    );

    List<Position> path4 = Arrays.asList( // Path for Robot 4
      new Position(414, 660),
      new Position(414, 0),
      new Position(150, 0), 
      new Position(150, 660)
    );

    List<Position> path5 = Arrays.asList( // Path for Robot 5
      new Position(414, 0), new Position(810, 0),
      new Position(810, 395), new Position(414, 395)
    );

    List<Position> path6 = Arrays.asList( // Path for Robot 6
      new Position(810, 660), new Position(546, 660),
      new Position(546, 0), new Position(810, 0)
    );

    List<Position> path7 = Arrays.asList( // Path for Robot 7
      new Position(414, 528), new Position(150, 528), new Position(150, 132),
      new Position(414, 132), new Position(414, 0), new Position(546, 0),
      new Position(546, 132), new Position(810, 132), new Position(810, 528),
      new Position(546, 528), new Position(546, 660), new Position(414, 660)
    );

    List<Position> path8 = Arrays.asList( // Path for Robot 8
      new Position(282, 263), new Position(150, 263), new Position(150, 0),
      new Position(414, 0), new Position(414, 132), new Position(546, 132),
      new Position(546, 263), new Position(678, 263), new Position(678, 395),
      new Position(810, 395), new Position(810, 660), new Position(546, 660),
      new Position(546, 528), new Position(414, 528), new Position(414, 395),
      new Position(282, 395)
    );

    return Arrays.asList(path1, path2, path3, path4, path5, path6, path7, path8); // Return all paths
  }
}// End of class PathData