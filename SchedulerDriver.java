
//  JOHN ZHAO
//  CS 4348.003
//  PROJECT THREE

//  MAIN DRIVER CLASS

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
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

            int quantum = -1, numPriorities = -1;
            boolean serviceGiven = false;
            double alpha = -1;

            //  FIRST INPUT LINE WILL ALWAYS BE ALGORITHM KEY.
            String algorithm = algorithmReader.nextLine();

            //  IF ALGORITHM FILE HAS MORE TO PARSE
            while(algorithmReader.hasNextLine())
            {
                String line = algorithmReader.nextLine();

                if(line.substring(0, 6).equals("alpha="))
                    alpha = Double.parseDouble(line.substring(6));
                else if(line.substring(0, 8).equals("quantum="))
                    quantum = Integer.parseInt(line.substring(8));
                else if(line.substring(0, 14).equals("service_given="))
                    serviceGiven = Boolean.parseBoolean(line.substring(14));
                else if(line.substring(0, 15).equals("num_priorities="))
                    numPriorities = Integer.parseInt(line.substring(15));
            }

            //  DEBUG: SHOW ALL PARAMETERS' VALUES
            // System.out.println("\nQUANTUM: " + quantum + "\nSERVICE GIVEN: " + serviceGiven +
            //         "\nNUMBER OF PRIORITIES: " + numPriorities + "\nALPHA: " + alpha);

            //  PARSE THRU PROCESSES FILE
            while(processesReader.hasNextLine())
            {
                //  SPLIT THE PROCESS LINE BY WHITESPACE, AND STORE EVERYTHING
                //  IN A STRING ARRAY, FOR EASE OF PARSING
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
                simulateFCFS(processList, eventQueue);
            else if(algorithm.equals("VRR"))
                simulateVRR(processList, eventQueue, quantum);
            else if(algorithm.equals("SPN"))
                simulateSPN(processList, eventQueue, serviceGiven, alpha);
            else if(algorithm.equals("HRRN"))
                simulateHRRN(processList, eventQueue, serviceGiven, alpha);
            else if(algorithm.equals("FEEDBACK"))
                simulateFEEDBACK(processList, eventQueue, quantum, numPriorities);
        }
        else
            System.out.println("MISSING ARGUMENTS!");
    }

    //  HELPER - Display all processes
    private static void displayCurrentProcessState(LinkedList<SimulatedProcess> processList)
    {
        System.out.println("CURRENT PROCESS STATE: ");
        for(SimulatedProcess process : processList)
            System.out.println(process);
    }

    //  HELPER - Display process queue
    private static void displayProcessQueue(Queue<SimulatedProcess> processQueue)
    {
        System.out.println("CURRENT PROCESS QUEUE: ");

        if(processQueue.isEmpty())
            System.out.println("\tEMPTY");
        else
        {
            for (SimulatedProcess process : processQueue)
                System.out.println(process);
        }
    }

    //  HELPER - FEEDBACK
    private static void simulateFEEDBACK(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue, int quantum, int numPriorities)
    {


    }

    //  HELPER - HIGHEST RESPONSE RATIO NEXT
    private static void simulateHRRN(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue, boolean serviceGiven, double alpha)
    {


    }

    //  HELPER - SHORTEST REMAINING TIME
    private static void simulateSPN(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue, boolean serviceGiven, double alpha)
    {
        int clockTime = 0;

        ProcessEvent currentEvent = null;

        Queue<SimulatedProcess> processQueue = new LinkedList<>();

        debugDisplay("SHORTEST REMAINING TIME", processQueue, processList);

        while(!eventQueue.isEmpty())
        {
            //  GRAB NEXT EVENT
            currentEvent = eventQueue.poll();

        }
    }

    //  HELPER - VIRTUAL ROUND ROBIN
    private static void simulateVRR(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue, int quantum)
    {
        int clockTime = 0;

        ProcessEvent currentEvent = null;

        //  QUEUE for Processes
        Queue<SimulatedProcess> processQueue = new LinkedList<>();

        //  DISPLAY INFORMATION
        debugDisplay("VIRTUAL ROUND ROBIN", processQueue, processList);

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

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            parseEvent(processQueue, currentEvent, eventProcess, clockTime);

            //  HEAD OF QUEUE IS CURRENT RUNNING PROCESS! REMEMBER
            if(!processQueue.isEmpty())
            {
                SimulatedProcess currentProcess = processQueue.peek();
                int currentProcessId = currentProcess.getProcessId();

                //  HANDLE PROCESS STATES
                if(currentProcess.getCurrentState() == SimulatedProcess.PState.READY)
                {
                    //  READY -> RUNNING
                    currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                    //  ONCE THE PROCESS FINALLY GETS TO RUN, IT'S NOT
                    //      WAITING ANYMORE; CALCULATE AND ADD RESPONSE TIME DIFFERENCE
                    currentProcess.addResponseTime(clockTime - currentProcess.getCurrentWaitTime());

                    if(currentProcess.getStartTime() == -1)
                        currentProcess.setStartTime(clockTime);

                    if(!currentProcess.getActivities().isEmpty())
                    {
                        //  ASSUMPTION: ALWAYS START WITH CPU ACTIVITY
                        int durationCPU = currentProcess.getActivities().peek().getActivityDuration();

                        if (quantum < durationCPU)
                        {
                            currentProcess.getActivities().peek().decrementActivityDuration(quantum);

                            currentProcess.incrementServiceTime(quantum);

                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.TIMEOUT, currentProcessId, clockTime + quantum));
                        }
                        else if (quantum >= durationCPU)
                        {
                            //  POP CPU ACTIVITY
                            currentProcess.getActivities().poll();

                            currentProcess.incrementServiceTime(durationCPU);

                            if(!currentProcess.getActivities().isEmpty())
                            {
                                int durationIO = currentProcess.getActivities().poll().getActivityDuration();

                                int startTimeIO = clockTime + durationCPU;
                                int endTimeIO = startTimeIO + durationIO;

                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcessId, startTimeIO));
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcessId, endTimeIO));

                                //  DOES THIS PROCESS END AFTER UNBLOCKING? CHECK.
                                if(currentProcess.getActivities().isEmpty())
                                    eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcessId, endTimeIO));
                            }
                            else
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcessId, clockTime + durationCPU));
                        }
                    }

                }
            }

            //  DISPLAY INFORMATION OF CURRENT STATE | DEBUGGING PURPOSES
            debugDisplay("", processQueue, processList);
        }
    }

    //  HELPER - FIRST COME FIRST SERVE
    private static void simulateFCFS(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue)
    {
        int clockTime = 0;

        //  Is current process necessary? (Can peek() top queue)
        ProcessEvent currentEvent = null;

        //  QUEUE for Processes
        Queue<SimulatedProcess> processQueue = new LinkedList<>();

        //  DEBUG DISPLAY
        debugDisplay("FIRST COME FIRST SERVE", processQueue, processList);

        System.out.println();

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

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            //  PARSE CURRENT EVENT (HELPER)
            parseEvent(processQueue, currentEvent, eventProcess, clockTime);

            //  HEAD OF QUEUE IS CURRENT RUNNING PROCESS! REMEMBER
            if(!processQueue.isEmpty())
            {
                SimulatedProcess currentProcess = processQueue.peek();
                int currentProcessId = currentProcess.getProcessId();

                //  HANDLE PROCESS STATES
                if(currentProcess.getCurrentState() == SimulatedProcess.PState.READY)
                {
                    //  READY -> RUNNING
                    currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                    //  ONCE THE PROCESS FINALLY GETS TO RUN, IT'S NOT
                    //      WAITING ANYMORE; CALCULATE AND ADD RESPONSE TIME DIFFERENCE
                    currentProcess.addResponseTime(clockTime - currentProcess.getCurrentWaitTime());

                    if(currentProcess.getStartTime() == -1)
                        currentProcess.setStartTime(clockTime);

                    //  PROCESS COULD END AFTER UNBLOCKING, MAKE SURE TO CHECK
                    if(!currentProcess.getActivities().isEmpty())
                    {
                        //  ASSUMPTION: ALWAYS START WITH CPU ACTIVITY
                        int durationCPU = currentProcess.getActivities().poll().getActivityDuration();
                        currentProcess.incrementServiceTime(durationCPU);

                        //  [START | CPU]
                        //  [START | CPU | IO]
                        if (!currentProcess.getActivities().isEmpty())
                        {
                            int durationIO = currentProcess.getActivities().poll().getActivityDuration();

                            int startTimeIO = clockTime + durationCPU;
                            int endTimeIO = startTimeIO + durationIO;

                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcessId, startTimeIO));
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcessId, endTimeIO));

                            //  DOES THIS PROCESS END AFTER UNBLOCKING? CHECK.
                            if(currentProcess.getActivities().isEmpty())
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcessId, endTimeIO));
                        }
                        else
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcessId, clockTime + durationCPU));
                    }

                }
            }

            //  DISPLAY INFORMATION OF CURRENT STATE | DEBUGGING PURPOSES
            debugDisplay("", processQueue, processList);
        }
    }

    //  HELPER: PARSE EVENTS APPROPRIATELY (FCFS) (VRR)
    private static void parseEvent(Queue<SimulatedProcess> processQueue, ProcessEvent currentEvent, SimulatedProcess eventProcess, int clockTime)
    {
        if(currentEvent.getType() == ProcessEvent.Type.ARRIVE ||
                currentEvent.getType() == ProcessEvent.Type.UNBLOCK)
        {
            if(eventProcess.getArrivalTime() == -1)
                eventProcess.setArrivalTime(clockTime);

            eventProcess.setCurrentState(SimulatedProcess.PState.READY);

            //  CALCULATE AVERAGE RESPONSE TIME, MEASURE
            //      WAIT TIME FOR PROCESS AFTER ARRIVAL/PRE-EMPTION/UNBLOCK
            eventProcess.setCurrentWaitTime(clockTime);

            //  ADD PROCESS TO PROCESS QUEUE
            processQueue.add(eventProcess);
        }
        else if(currentEvent.getType() == ProcessEvent.Type.BLOCK)
        {
            eventProcess.setCurrentState(SimulatedProcess.PState.BLOCKING);

            //  REMOVE PROCESS FROM QUEUE
            processQueue.poll();
        }
        else if(currentEvent.getType() == ProcessEvent.Type.TIMEOUT)
        {
            eventProcess.setCurrentState(SimulatedProcess.PState.READY);

            //  CALCULATE AVERAGE RESPONSE TIME, MEASURE
            //      WAIT TIME FOR PROCESS AFTER ARRIVAL/PRE-EMPTION/UNBLOCK
            eventProcess.setCurrentWaitTime(clockTime);

            processQueue.add(processQueue.poll());
        }
        else if(currentEvent.getType() == ProcessEvent.Type.EXIT)
        {
            //  IF A PROCESS COMES OUT OF BLOCKING, IT MAY GET PUT BACK INTO
            //      QUEUE, AND HAVE TO EXIT IMMEDIATELY.
            //  CANNOT JUST TERMINATE HEAD QUEUE, MUST SEARCH FOR IT INSTEAD.
            eventProcess = processQueue.poll();

            while(currentEvent.getProcessId() != eventProcess.getProcessId())
            {
                processQueue.add(eventProcess);

                eventProcess = processQueue.poll();
            }

            eventProcess.setFinishTime(clockTime);

            eventProcess.setCurrentState(SimulatedProcess.PState.TERMINATED);
        }

    }

    //  DEBUG: DISPLAY STRING, PROCESS QUEUE, STATE OF PROCESSES
    private static void debugDisplay(String algorithm, Queue<SimulatedProcess> processQueue, LinkedList<SimulatedProcess> processList)
    {
        if(!algorithm.equals(""))
        {
            System.out.println(algorithm + "\n");

            System.out.println("INITIAL STATE");
        }

        displayProcessQueue(processQueue);
        displayCurrentProcessState(processList);

        System.out.println();
    }


}