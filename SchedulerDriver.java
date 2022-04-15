
//  JOHN ZHAO
//  CS 4348.003
//  PROJECT THREE

//  MAIN DRIVER CLASS

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;

public class SchedulerDriver
{
    public static void main(String[] args) throws FileNotFoundException
    {
        //  ASSUMPTION: ALWAYS PERFECT INPUT

        //  CHECK IF ARGS EXIST
        if(args.length > 1)
        {
            //  PRIMARY WAY OF READING FILE
            Scanner algorithmReader = new Scanner(new File(args[0]));
            Scanner processesReader = new Scanner(new File(args[1]));

            //  PRIORITY QUEUE: STORE EVENTS
            PriorityQueue<ProcessEvent> eventQueue = new PriorityQueue<>();

            //  LINKED LIST: STORE PROCESSES, WHICH ARE QUEUES
            //  OF ACTIVITY INSTANCES.
            LinkedList<SimulatedProcess> processList = new LinkedList<>();

            int processId = 0;

            //  FIRST INPUT LINE WILL ALWAYS BE ALGORITHM KEY.
            String algorithm = algorithmReader.nextLine();

            while(processesReader.hasNextLine())
            {
                String[] lineElements = processesReader.nextLine().split("\\s+");

                //  ADD NEW PROCESS (ID: processId)
                processList.add(new SimulatedProcess());

                //  ITERATE THRU EACH LINE ELEMENT
                for(int element = 0; element < lineElements.length; element++)
                {
                    if(element == 0)
                    {
                        int arrivalTime = Integer.parseInt(lineElements[element]);

                        eventQueue.add(new ProcessEvent(ProcessEvent.Type.ARRIVE, processId,
                                arrivalTime));
                    }
                    else
                    {
                        if(lineElements[element].equals("CPU"))
                            processList.get(processId).addActivity(new Activity(Activity.Type.CPU,
                                    Integer.parseInt(lineElements[++element])));
                        else if(lineElements[element].equals("IO"))
                            processList.get(processId).addActivity(new Activity(Activity.Type.IO,
                                    Integer.parseInt(lineElements[++element])));
                    }
                }

                //  INCREMENT PROCESS ID
                processId++;
            }

            System.out.print("\nCURRENT ALGORITHM: ");

            if(algorithm.equals("FCFS"))
            {
                System.out.println("FIRST COME, FIRST SERVE\n");

                System.out.println("INITIAL STATE");
                for(SimulatedProcess process : processList)
                    System.out.println(process);

                System.out.println();

                int clockTime = 0;
                SimulatedProcess currentProcess = null;
                ProcessEvent currentEvent = null;

                while(!eventQueue.isEmpty())
                {
                    //  GRAB NEXT EVENT
                    currentEvent = eventQueue.poll();

                    //  IF TIMESTAMP IS AHEAD OF CLOCK, SET CLOCK TO MATCH
                    if(currentEvent.getTime() > clockTime)
                        clockTime = currentEvent.getTime();

                    //  PRINT CLOCK TIME AND CURRENT EVENT
                    System.out.println("\nCLOCK: " + clockTime);
                    System.out.println(currentEvent);

                    //  PARSE CURRENT EVENT
                    if(currentEvent.getType() == ProcessEvent.Type.ARRIVE)
                    {
                        int elapsedTime = 0;

                        processList.get(currentEvent.getProcessId()).setArrivalTime(clockTime);

                        processList.get(currentEvent.getProcessId()).setCurrentState(SimulatedProcess.PState.READY);
                    }
                    else if(currentEvent.getType() == ProcessEvent.Type.BLOCK)
                        processList.get(currentEvent.getProcessId()).setCurrentState(SimulatedProcess.PState.BLOCKING);
                    else if(currentEvent.getType() == ProcessEvent.Type.UNBLOCK)
                        processList.get(currentEvent.getProcessId()).setCurrentState(SimulatedProcess.PState.RUNNING);
                    else if(currentEvent.getType() == ProcessEvent.Type.EXIT)
                    {
                        processList.get(currentEvent.getProcessId()).setCurrentState(SimulatedProcess.PState.TERMINATED);

                        currentProcess.setFinishTime(clockTime);

                        currentProcess = null;
                    }

                    if(currentProcess == null || currentProcess.getCurrentState() == SimulatedProcess.PState.TERMINATED)
                    {
                        for(SimulatedProcess process : processList)
                        {
                            if(process.getCurrentState() == SimulatedProcess.PState.READY)
                            {
                                currentProcess = process;

                                currentProcess.setStartTime(clockTime);

                                currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                                int elapsedTime = 0;

                                while(!currentProcess.getActivities().isEmpty())
                                {
                                    Activity activity = currentProcess.getActivities().poll();

                                    if(activity.getType() == Activity.Type.CPU)
                                        elapsedTime += activity.getActivityDuration();
                                    else if(activity.getType() == Activity.Type.IO)
                                    {
                                        eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcess.getProcessId(), clockTime + elapsedTime));

                                        elapsedTime += activity.getActivityDuration();

                                        eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcess.getProcessId(), clockTime + elapsedTime));
                                    }
                                }

                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), clockTime + elapsedTime));

                                break;
                            }
                        }
                    }

                    System.out.println("\tCURRENT STATE");

                    if(currentProcess == null)
                        System.out.println("NO PROCESS RUNNING");
                    else
                        System.out.println("CURRENT PROCESS RUNNING: " + processList.get(currentEvent.getProcessId()));

                    for(SimulatedProcess process : processList)
                        System.out.println(process);
                }
            }
            else if(algorithm.equals("VRR"))
            {
                System.out.println("VIRTUAL ROUND ROBIN\n");

            }
            else if(algorithm.equals("SPN"))
            {
                System.out.println("SHORTEST REMAINING TIME\n");

            }

        }
        else
            System.out.println("MISSING ARGUMENTS!");
    }
}
