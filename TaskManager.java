import java.util.*;
import java.io.*;

class TaskManager {

  public static void main(String[] args) {
    Job nyJobb = new Job();
    System.out.println("\n\n\n---buildGarage---");
    nyJobb.doProject(new File("buildGarage.txt"));

    Job nesteJobb = new Job();
    System.out.println("\n\n\n---buildHouse2---");
    nesteJobb.doProject(new File("buildHouse2.txt"));




  }

}
