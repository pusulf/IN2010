import java.util.*;
import java.io.*;


public class Job {//A graph

  int numberOfTasks;
  LinkedList<Task> tasks = new LinkedList<>();
  int totalProjectTime = 0;

  private class Task {//Nodes in a graph

    int id;//Unique id for each job
    int time;//The time it takes to do a finish a jab
    int manpower;//Required manpower for the job
    String name;//Name of the job

    LinkedList<Task> outEdges = new LinkedList<>();//All tasks dependent on said job
    int cntPredecessors;//Tasks this job is dependent on
    LinkedList<Integer> predecessorsId = new LinkedList<>();
    boolean visited = false;
    int state = -1;

    int earliestStart = 0;
    int earliestFinish = 0;

    int latestStart = 0;
    int latestFinish = 0;

    int slack = 0;


    Task(int id){
      this.id = id;
    }


    public int getId(){
      return this.id;
    }


    public String toString(){
      String denpententOnThis = "";
      for(Task t: outEdges){
        denpententOnThis += " " + Integer.toString(t.getId());
      }
      return "Task id: " + id
      + "\nTask name: " + name
      + "\nTime: " + time
      + "\nManpower: " + manpower
      + "\nEarliest start: " + Integer.toString(this.earliestStart)
      + "\nSlack: " + Integer.toString(slack)
      + "\nOutedges: " + denpententOnThis;
    }


    private void settEarliestStart(){
      for(Task t: outEdges){
        if(t.earliestStart < this.earliestStart + this.time){
          t.earliestStart = this.earliestStart + this.time;
        }
      }
    }


    private void settLatestStart(){
      if(this.outEdges.size()==0){
        this.latestStart = totalProjectTime - this.time;
      }

      int tempTime = totalProjectTime;

      for(Task sucsessor:this.outEdges){
        if(sucsessor.latestStart < tempTime){
          tempTime = sucsessor.latestStart;
        }
      }

      this.latestStart = tempTime - this.time;
    }


    private void settSlack(){
      this.slack = this.latestStart - this.earliestStart;
    }

  }


  public void Job(){}

  public void doProject(File file){
      readFile(file);
      if(!reliziability()){
        return;
      }
      System.out.println("Starting to process jobb");
      optimalTimeSchedule();
      printProject();
      printTasks();
    }


  private void readFile(File file){
    Scanner scanner = null;
    try{
      scanner = new Scanner(file);
    } catch(FileNotFoundException e){
      System.out.println("File not found");
      System.out.println("Terminating program");
      System.exit(0);
    }

    numberOfTasks = Integer.parseInt(scanner.nextLine());
    System.out.println("The number of tasks in this job is: " + numberOfTasks);
    scanner.nextLine(); //Empthy line


    while(scanner.hasNextLine()){//Reads from the txt file
      String line = scanner.nextLine();
      String[] info = line.split("\\s+");


      //Creates a new Task and adds to the list
      int newId = Integer.parseInt(info[0]);
      Task newTask = new Task(newId);
      tasks.add(newTask);

      //Adds most of the variabels to the Task
      newTask.name = info[1];

      newTask.time = Integer.parseInt(info[2]);

      newTask.manpower = Integer.parseInt(info[3]);

      for(int i = 4; i < info.length-1; i ++){
        newTask.predecessorsId.add(Integer.parseInt(info[i])); //Adds all the predecesors id to the list, but not the 0 in the file
      }
    }

    //Adds outedges
    for(Task task:tasks){
      task.cntPredecessors = task.predecessorsId.size();
      for(int i:task.predecessorsId){
        tasks.get(i-1).outEdges.add(task);
      }
    }

  }


  private boolean reliziability(){//Checks for cycles
    System.out.println("Checking for reliziability");

    LinkedList<Task> temp = new LinkedList<>();
    Task startForDFS = tasks.get(0);


    if(DFSDetection(startForDFS, temp)){
      System.out.println("There was a cycle");
      System.out.println("Terminating program");
      return false;
    }

    else{
      System.out.println("There is no cycle in the given graph");
      return true;
    }
  }


  private boolean cycleDetectionDFS(){//Start of recursion for detecting cycles
    LinkedList<Task> temp = new LinkedList<>(); //List for storing tasks for when im gonna look for nodes later

    for(Task ta: tasks){ //For each components in the graph
      //System.out.println("Doing DFS on " + ta.id);
      if(DFSDetection(ta,temp)){
        System.out.println("Cycle registered");
        return true;
      }
    }
    return false;
  }


  private boolean DFSDetection(Task t, LinkedList<Task> temp){//Based on lokkeleting from Ingrid Chiesh, in2010 2018 lecture no. 4
    //System.out.println("Checking " + Integer.toString(t.getId()));
    temp.add(t); //Adds t to the stack for finding the cycle
    if(t.state == 0){//If the task is still in use, there is a cycle
      System.out.println("\n\nCycle: ");
      for(Task task: tasks){
        if(task.state == 0){

          System.out.println(task.getId());
        }
      }
      //printLokke(temp);
      System.out.println("\nExeting dfs");
      return true;

    } else if(t.state == -1){//if the task has not been checked yet, a recusive methode is called
      t.state = 0;
      for(Task task : t.outEdges){
        if(DFSDetection(task,temp)){
          return true;
        }
      }
    }
    t.state = 1; //When all nighbors of the task has been explored, the task is done
    //System.out.println(Integer.toString(t.getId()) + " contains no cycle");
    return false;
  }


  private void printLokke(LinkedList<Task> temp){
    System.out.println("Stoerrelse: " + Integer.toString(temp.size()));
    for(Task t: temp){
      System.out.println(t.getId());
    }
    Task startCycle = temp.get(temp.size()-1); //The last element in the list, start of cycle
    System.out.println("Cycle found:");
    Deque<Task> cycleList = new LinkedList<>();
    int inCycle = temp.size()-2;//The last element in the temp list, aka the start of the cycle
    while(temp.get(inCycle) != startCycle){
      cycleList.addFirst(temp.get(inCycle));
      inCycle += -1;
    }
    cycleList.addFirst(startCycle);
    for(Task t:cycleList){
      System.out.println(t.getId());
    }
  }


  private LinkedList<Task> topograficalSort(){
    //System.out.println("Starter topologisk sortering");
    Stack<Task> stack = new Stack<>();
    LinkedList<Task> sortedList = new LinkedList<>();
    for(Task t: tasks){
      if(t.predecessorsId.size() == 0){
        stack.add(t);
      }
    }
    while(!stack.isEmpty()){
      Task t = stack.pop();
      //System.out.println("Henter ut fra stack og legger foerst i lista: " + t.name);
      sortedList.add(t);
      t.visited = true;
      //System.out.println("Sjekker etterkommere: \n");
      for(Task sucsessor : t.outEdges){
        //System.out.println("Fjerner en fra inngrad til " + sucsessor.name + ". Ny inngrad er " + Integer.toString(sucsessor.cntPredecessors));
        sucsessor.cntPredecessors += -1;
        if(sucsessor.cntPredecessors == 0){
          stack.add(sucsessor);
          //System.out.println("La til i stacken: " + sucsessor.name);
        }
      }
    }
    //System.out.println("\n\nSortert Liste: ");
    for(Task t : sortedList){
      //System.out.println(t.id);
    }
    return sortedList;
  }


  public void optimalTimeSchedule(){
    LinkedList<Task> topSort = topograficalSort();
    findEarliestTime(topSort);
    findTotalProjectTime(topSort);
    findLeatestTime(topSort);
    findSlack(topSort);
    //System.out.println("\n\n\nTimes found");
  }

  private void findTotalProjectTime(List<Task> topograficalSortedList){
    for(Task t: topograficalSortedList){
      if(t.earliestStart + t.time > totalProjectTime){
        totalProjectTime = t.earliestStart + t.time;
      }
    }
    //System.out.println("totalProjectTime = " + Integer.toString(totalProjectTime));
  }


  private void findEarliestTime(List<Task> topograficalSortedList){//find earliest start
    for(Task t:topograficalSortedList){
      t.settEarliestStart();
    }
    for(Task t:topograficalSortedList){
      //System.out.println(t.name + " earlist start: " + Integer.toString(t.earliestStart));
    }

  }




  private void findLeatestTime(List<Task> topograficalSortedList){//Find latest start
    //Latest start: forrige latest start minus din egen tid
    //System.out.println("\nStarter latestStart\n");


    for(int i = topograficalSortedList.size()-1; i >= 0;i--){
      topograficalSortedList.get(i).settLatestStart();
    }

    for(Task t: topograficalSortedList){
      //System.out.println(t.name + " starts latest= " + Integer.toString(t.latestStart));
    }
  }


  private void findSlack(List<Task> topograficalSortedList){
    //System.out.println("\n\n\nStarter slack");
    for(Task task: topograficalSortedList){
      task.settSlack();
      //System.out.println(task.name + " slack= " + Integer.toString(task.slack));
    }
    //System.out.println("Slutter slack");
  }



  public void printProject(){
    //Jeg printer naar noe skjer prosjektet, og sjekker sekund for sekund
    //Koden er inspirert av Martine Woldseth

    int tidspunkt=0;
    int arbeidStokk=0;

    boolean printetUtTidspunkt = false;
    boolean bemanningPrintet = false;


    while (tidspunkt<=totalProjectTime){

       for(Task t: tasks){
          if(t.earliestStart == tidspunkt || (t.earliestStart+t.time) == tidspunkt){ //Hvis noe starter eller stopper
             if(!printetUtTidspunkt){
                System.out.println("\n\nTime:" + tidspunkt);
                System.out.println("");
                printetUtTidspunkt = true;
                bemanningPrintet = true;
             }
             if(t.earliestStart == tidspunkt){
                System.out.println("  Starting task: "+ t.name + ", id: " + t.id);
                arbeidStokk += t.manpower;
             }
             if((t.earliestStart + t.time) == tidspunkt){ //Fikk ikke denne til aa fungere
                System.out.println("  Finished task: "+ t.name + ", id: " + t.id);
                arbeidStokk-=t.manpower;
             }
          }
       }
       printetUtTidspunkt= false;

       if(bemanningPrintet){

          System.out.println("  Active number for workers: " + arbeidStokk);
          bemanningPrintet=false;
       }
       tidspunkt++;
    }
    System.out.println("\nOptimal time: " + totalProjectTime);
 }


  public void printTasks(){
    System.out.println("\n\nPresenting all tasks in current Job");
    System.out.println("-------------------------------------");
    for(Task task: tasks){
      System.out.println("");
      System.out.println(task);
    }
  }
}
