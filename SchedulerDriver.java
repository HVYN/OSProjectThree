
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

                //  FOR SPN ALGORITHM
                processList.get(processId).calculateRemainingBurstTime();

                processList.get(processId).calculateServiceTime();

                //  INCREMENT PROCESS ID
                processId++;
            }

            System.out.println("\nCURRENT ALGORITHM: " + algorithm + "\n");

            if(algorithm.equals("FCFS"))
                simulateFCFS(processList, eventQueue);
            else if(algorithm.equals("VRR"))
                simulateVRR(processList, eventQueue, quantum);
            else if(algorithm.equals("SPN"))
            {
                if(serviceGiven)
                    simulateSPN(processList, eventQueue);
                else
                    simulateSPN(processList, eventQueue, alpha);
            }
            else if(algorithm.equals("HRRN"))
            {
                if(serviceGiven)
                    simulateHRRN(processList, eventQueue);
                else
                    simulateHRRN(processList, eventQueue, alpha);
            }
            else if(algorithm.equals("FEEDBACK"))
                simulateFEEDBACK(processList, eventQueue, quantum, numPriorities);

            //  DISPLAY MEAN TURNAROUND TIME, MEAN NORMALIZED TURNAROUND TIME, AND
            //      MEAN AVERAGE RESPONSE TIME FOR ALL PROCESSES.
            displayFinalStats(processList);
        }
        else
            System.out.println("MISSING ARGUMENTS!");

    }

    //  HELPER - FEEDBACK
    private static void simulateFEEDBACK(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue, int quantum, int numPriorities)
    {
        int clockTime = 0;
        int lastClockTime, elapsedTime;

        ProcessEvent currentEvent;

        SimulatedProcess currentProcess = null;

        //  MAKE ARRAY OF QUEUES OF PROCESSES.
        LinkedList<Queue<SimulatedProcess>> processQueues = new LinkedList<>();

        //  FEEDBACK HAS MULTIPLE PRIORITIES
        //      MAKE QUEUES ACCORDING TO THAT.
        for(int priority = 0; priority < numPriorities; priority++)
            processQueues.add(new LinkedList<>());

        // debugDisplay("FEEDBACK", processQueue, processList);
        debugDisplay(processQueues, processList, null);

        System.out.println();

        while(!eventQueue.isEmpty())
        {
            lastClockTime = clockTime;

            currentEvent = eventQueue.poll();

            if(currentEvent.getTime() > clockTime)
                clockTime = currentEvent.getTime();

            elapsedTime = clockTime - lastClockTime;

            displayTime(clockTime, lastClockTime);
            System.out.println(currentEvent);

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            // parseEvent(processQueue, currentEvent, eventProcess, clockTime);

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
                //  processQueue.add(eventProcess);
                processQueues.get(eventProcess.getPriority()).add(eventProcess);
            }
            else if(currentEvent.getType() == ProcessEvent.Type.BLOCK)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.BLOCKING);

                //  REMOVE PROCESS FROM QUEUE
                //  processQueue.poll();
                int priority = eventProcess.getPriority();

                for(int index = 0; index < processQueues.get(priority).size(); index++)
                {
                    SimulatedProcess temp = processQueues.get(priority).poll();

                    if(temp.getProcessId() != eventProcess.getProcessId())
                        processQueues.get(priority).add(temp);
                    else
                        break;
                }
            }
            else if(currentEvent.getType() == ProcessEvent.Type.TIMEOUT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                //  CALCULATE AVERAGE RESPONSE TIME, MEASURE
                //      WAIT TIME FOR PROCESS AFTER ARRIVAL/PRE-EMPTION/UNBLOCK
                eventProcess.setCurrentWaitTime(clockTime);

                SimulatedProcess tempEventProcess = null;

                for(int index = 0; index < processQueues.get(eventProcess.getPriority()).size(); index++)
                {
                    tempEventProcess = processQueues.get(eventProcess.getPriority()).poll();

                    if(tempEventProcess.getProcessId() != eventProcess.getProcessId())
                        processQueues.get(eventProcess.getPriority()).add(tempEventProcess);
                    else
                        break;
                }

                if(tempEventProcess.getPriority() < numPriorities - 1)
                    tempEventProcess.incrementPriority(1);

                processQueues.get(tempEventProcess.getPriority()).add(tempEventProcess);
            }
            else if(currentEvent.getType() == ProcessEvent.Type.EXIT)
            {
                //  IF A PROCESS COMES OUT OF BLOCKING, IT MAY GET PUT BACK INTO
                //      QUEUE, AND HAVE TO EXIT IMMEDIATELY.
                //  CANNOT JUST TERMINATE HEAD QUEUE, MUST SEARCH FOR IT INSTEAD.
                int priority = eventProcess.getPriority();

                // System.out.println(eventProcess);

                SimulatedProcess tempEventProcess = null;

                for(int index = 0; index < processQueues.get(priority).size(); index++)
                {
                    tempEventProcess = processQueues.get(priority).poll();

                    if(tempEventProcess.getProcessId() != eventProcess.getProcessId())
                        processQueues.get(priority).add(tempEventProcess);
                    else
                        break;
                }

                tempEventProcess.setFinishTime(clockTime);

                tempEventProcess.setCurrentState(SimulatedProcess.PState.TERMINATED);
            }

            // if(!processQueue.isEmpty())
            if(!isProcessQueuesEmpty(processQueues))
            {
                currentProcess = getHeadProcess(processQueues);

                int currentProcessId = currentProcess.getProcessId();

                if(currentProcess.getCurrentState() == SimulatedProcess.PState.READY)
                {
                    currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                    currentProcess.addResponseTime(clockTime - currentProcess.getCurrentWaitTime());

                    if(currentProcess.getStartTime() == -1)
                        currentProcess.setStartTime(clockTime);

                    if(!currentProcess.getActivities().isEmpty())
                    {
                        int durationCPU = currentProcess.getActivities().peek().getActivityDuration();

                        if(quantum < durationCPU)
                        {
                            currentProcess.getActivities().peek().decrementActivityDuration(quantum);

                            // currentProcess.incrementServiceTime(quantum);

                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.TIMEOUT, currentProcessId, clockTime + quantum));
                        }
                        else if(quantum >= durationCPU)
                        {
                            System.out.println("\t[POLLING CPU ACTIVITY] DEBUG: " + currentProcess.getActivities().poll() + " FROM PROCESS " + currentProcessId);
                            // currentProcess.incrementServiceTime(durationCPU);

                            if(!currentProcess.getActivities().isEmpty())
                            {
                                int durationIO = currentProcess.getActivities().poll().getActivityDuration();

                                System.out.println("\t[IO ACTIVITY] DEBUG: " + durationIO + " FROM PROCESS " + currentProcessId);
                                int startTimeIO = clockTime + durationCPU;
                                int endTimeIO = startTimeIO + durationIO;

                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcessId, startTimeIO));
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcessId, endTimeIO));

                                if(currentProcess.getActivities().isEmpty())
                                    eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcessId, endTimeIO));
                            }
                            else
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcessId, clockTime + durationCPU));

                        }
                    }
                }
            }

            debugDisplay(processQueues, processList, currentProcess);
        }
    }

    //  HELPER - HIGHEST RESPONSE RATIO NEXT
    //      WITH SERVICE GIVEN
    private static void simulateHRRN(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue)
    {
        int clockTime = 0;
        int lastClockTime;

        ProcessEvent currentEvent;

        SimulatedProcess currentProcess = null;

        System.out.println("INITIAL STATE");
        debugDisplay(processList, null);

        while(!eventQueue.isEmpty())
        {
            lastClockTime = clockTime;

            currentEvent = eventQueue.poll();

            if(currentEvent.getTime() > clockTime)
                clockTime = currentEvent.getTime();

            displayTime(clockTime, lastClockTime);
            System.out.println(currentEvent);

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            if(currentEvent.getType() == ProcessEvent.Type.ARRIVE ||
                    currentEvent.getType() == ProcessEvent.Type.UNBLOCK)
            {
                if(eventProcess.getArrivalTime() == -1)
                    eventProcess.setArrivalTime(clockTime);

                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);
            }
            else if(currentEvent.getType() == ProcessEvent.Type.BLOCK)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.BLOCKING);

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }
            else if(currentEvent.getType() == ProcessEvent.Type.EXIT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.TERMINATED);

                eventProcess.setFinishTime(clockTime);

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }

            //  IF THERE ARE PROCESSES THAT CAN BE RUN,
            //  AND CURRENT PROCESS IS DONE/EMPTY, MARK ONE TO BE RUN
            if(currentProcess == null)
            {
                for(SimulatedProcess process : processList)
                {
                    if(process.getCurrentState() == SimulatedProcess.PState.READY)
                    {
                        if(currentProcess == null)
                            currentProcess = process;
                        else
                        {
                            System.out.println("PROCESS #" + process.getProcessId() + " RESPONSE RATIO: " + process.getResponseRatio(clockTime));
                            System.out.println("PREVIOUS PROCESS RESPONSE RATIO: " + currentProcess.getResponseRatio(clockTime));

                            if(process.getResponseRatio(clockTime) > currentProcess.getResponseRatio(clockTime))
                                currentProcess = process;
                        }
                    }
                }
            }

            //  RUN ACTIVITIES
            if(currentProcess != null)
            {
                if (currentProcess.getCurrentState() == SimulatedProcess.PState.READY)
                {
                    currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                    currentProcess.addResponseTime(clockTime - currentProcess.getCurrentWaitTime());

                    if (currentProcess.getStartTime() == -1)
                        currentProcess.setStartTime(clockTime);

                    if (!currentProcess.getActivities().isEmpty())
                    {
                        int durationCPU = currentProcess.getActivities().poll().getActivityDuration();
                        // currentProcess.incrementServiceTime(durationCPU);
                        currentProcess.decrementRemainingBurstTime(durationCPU);

                        if (!currentProcess.getActivities().isEmpty())
                        {
                            int durationIO = currentProcess.getActivities().poll().getActivityDuration();

                            int startTimeIO = clockTime + durationCPU;
                            int endTimeIO = startTimeIO + durationIO;

                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcess.getProcessId(), startTimeIO));
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcess.getProcessId(), endTimeIO));

                            if (currentProcess.getActivities().isEmpty())
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), endTimeIO));
                        }
                        else
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), clockTime + durationCPU));

                    }
                }
            }

            debugDisplay(processList, currentProcess);
        }
    }

    //  HELPER - HIGHEST RESPONSE RATIO NEXT
    //      USING EXPONENTIAL AVERAGING
    private static void simulateHRRN(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue, double alpha)
    {
        int clockTime = 0;
        int lastClockTime, elapsedTime;

        ProcessEvent currentEvent;

        SimulatedProcess currentProcess = null;

        // debugDisplay("HIGHEST RESPONSE RATIO NEXT (EXPONENTIAL AVERAGING)", processArrivalQueue, processList);
        // System.out.println();

        while(!eventQueue.isEmpty())
        {
            lastClockTime = clockTime;

            currentEvent = eventQueue.poll();

            if(currentEvent.getTime() > clockTime)
                clockTime = currentEvent.getTime();

            elapsedTime = clockTime - lastClockTime;

            displayTime(clockTime, lastClockTime);
            System.out.println(currentEvent);

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            if(currentEvent.getType() == ProcessEvent.Type.ARRIVE ||
                    currentEvent.getType() == ProcessEvent.Type.UNBLOCK)
            {
                if(eventProcess.getArrivalTime() == -1)
                    eventProcess.setArrivalTime(clockTime);

                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);
            }
            else if(currentEvent.getType() == ProcessEvent.Type.BLOCK)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.BLOCKING);

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }
            else if(currentEvent.getType() == ProcessEvent.Type.EXIT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.TERMINATED);

                eventProcess.setFinishTime(clockTime);

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }

            //  IF THERE ARE PROCESSES THAT CAN BE RUN,
            //  AND CURRENT PROCESS IS DONE/EMPTY, MARK ONE TO BE RUN
            if(currentProcess == null)
            {
                // for(int index = processArrivalList.size() - 1; index >= 0; index--)
                for(SimulatedProcess process : processList)
                {
                    if(process.getCurrentState() == SimulatedProcess.PState.READY)
                    {
                        if(currentProcess == null)
                            currentProcess = process;
                        else
                        {
                            if(process.getResponseRatio(clockTime, alpha) > currentProcess.getResponseRatio(clockTime, alpha))
                                currentProcess = process;
                        }
                    }
                }
            }

            //  RUN ACTIVITIES
            if(currentProcess != null)
            {
                if (currentProcess.getCurrentState() == SimulatedProcess.PState.READY)
                {
                    currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                    currentProcess.addResponseTime(clockTime - currentProcess.getCurrentWaitTime());

                    if (currentProcess.getStartTime() == -1)
                        currentProcess.setStartTime(clockTime);

                    if (!currentProcess.getActivities().isEmpty())
                    {
                        int durationCPU = currentProcess.getActivities().poll().getActivityDuration();
                        // currentProcess.incrementServiceTime(durationCPU);
                        currentProcess.decrementRemainingBurstTime(durationCPU);

                        if (!currentProcess.getActivities().isEmpty())
                        {
                            int durationIO = currentProcess.getActivities().poll().getActivityDuration();

                            int startTimeIO = clockTime + durationCPU;
                            int endTimeIO = startTimeIO + durationIO;

                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcess.getProcessId(), startTimeIO));
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcess.getProcessId(), endTimeIO));

                            if (currentProcess.getActivities().isEmpty())
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), endTimeIO));
                        }
                        else
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), clockTime + durationCPU));

                    }
                }
            }

            System.out.println("\nCURRENT RUNNING PROCESS: ");
            if(currentProcess == null)
                System.out.println("\t--");
            else
                System.out.println(currentProcess);

            displayCurrentProcessState(processList);

        }
    }

    //  HELPER - SHORTEST REMAINING TIME
    //      WITH SERVICE GIVEN
    private static void simulateSPN(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue)
    {
        int clockTime = 0;
        int lastClockTime, elapsedTime;

        ProcessEvent currentEvent;

        SimulatedProcess currentProcess = null;

        // debugDisplay("SHORTEST REMAINING TIME (SERVICE GIVEN)", processQueue, processList);
        System.out.println("INITIAL STATE");
        debugDisplay(processList, null);

        while(!eventQueue.isEmpty())
        {
            //  ASSIGN VALUE BEFORE CLOCK TIME CHANGES.
            lastClockTime = clockTime;

            currentEvent = eventQueue.poll();

            //  MOVE CLOCK TIME UP TO MATCH CURRENT EVENT
            if(currentEvent.getTime() > clockTime)
                clockTime = currentEvent.getTime();

            //  CALCULATE ELAPSED TIME BETWEEN LAST EVENT AND CURRENT EVENT
            elapsedTime = clockTime - lastClockTime;

            displayTime(clockTime, lastClockTime);
            System.out.println(currentEvent);

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            if(currentProcess != null)
            {
                currentProcess.getBackupActivities().peek().decrementActivityDuration(elapsedTime);

                currentProcess.decrementRemainingBurstTime(elapsedTime);
            }

            //  PARSE EVENT (SPN) (SERVICE GIVEN)
            if(currentEvent.getType() == ProcessEvent.Type.ARRIVE ||
                    currentEvent.getType() == ProcessEvent.Type.UNBLOCK)
            {
                if(eventProcess.getArrivalTime() == -1)
                    eventProcess.setArrivalTime(clockTime);

                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);

                if(currentProcess != null)
                {
                    if(eventProcess.getRemainingBurstTime() < currentProcess.getRemainingBurstTime())
                    {
                        currentProcess.restoreBackup();

                        System.out.println("CURRENT PROCESS ID: " + currentProcess.getProcessId());
                        System.out.println("EVENT QUEUE: " + eventQueue);

                        //  Temporary Event Queue
                        PriorityQueue<ProcessEvent> tempEventQueue = new PriorityQueue<>();

                        while(!eventQueue.isEmpty())
                        {
                            ProcessEvent event = eventQueue.poll();

                            System.out.println("DEBUG: CHECKING EVENT " + event);

                            if(event.getProcessId() != currentProcess.getProcessId())
                                tempEventQueue.add(event);
                        }

                        /*
                        for(int index = 0; index < eventQueue.size(); index++)
                        {
                            ProcessEvent event = eventQueue.poll();

                            System.out.println("DEBUG: CHECKING EVENT " + event);

                            if(event.getProcessId() != currentProcess.getProcessId())
                                eventQueue.add(event);
                        }
                        */

                        eventQueue = tempEventQueue;

                        eventQueue.add(new ProcessEvent(ProcessEvent.Type.TIMEOUT, currentProcess.getProcessId(), clockTime));

                        currentProcess = null;
                    }
                }
            }
            else if(currentEvent.getType() == ProcessEvent.Type.BLOCK)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.BLOCKING);

                eventProcess.clearBackup();

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }
            else if(currentEvent.getType() == ProcessEvent.Type.TIMEOUT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);
            }
            else if(currentEvent.getType() == ProcessEvent.Type.EXIT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.TERMINATED);

                eventProcess.setFinishTime(clockTime);

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }

            //  IF THERE ARE PROCESSES THAT CAN BE RUN,
            //  AND CURRENT PROCESS IS DONE/EMPTY, MARK ONE TO BE RUN
            if(currentProcess == null)
            {
                for(SimulatedProcess process : processList)
                {
                    if(process.getCurrentState() == SimulatedProcess.PState.READY)
                    {
                        if(currentProcess == null)
                            currentProcess = process;
                        else
                        {
                            System.out.println("PROCESS #" + process.getProcessId() + " REMAINING BURST TIME: " + process.getRemainingBurstTime());
                            System.out.println("PREVIOUS PROCESS REMAINING BURST TIME: " + currentProcess.getRemainingBurstTime());

                            if(process.getRemainingBurstTime() < currentProcess.getRemainingBurstTime())
                                currentProcess = process;
                        }
                    }
                }
            }

            //  RUN ACTIVITIES
            if(currentProcess != null)
            {
                if(currentProcess.getCurrentState() == SimulatedProcess.PState.READY)
                {
                    currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                    currentProcess.addResponseTime(clockTime - currentProcess.getCurrentWaitTime());

                    if(currentProcess.getStartTime() == -1)
                        currentProcess.setStartTime(clockTime);

                    if(!currentProcess.getActivities().isEmpty())
                    {
                        Activity CPUactivity = currentProcess.getActivities().poll();

                        currentProcess.backupActivity(CPUactivity);

                        int durationCPU = CPUactivity.getActivityDuration();

                        if(!currentProcess.getActivities().isEmpty())
                        {
                            Activity IOactivity = currentProcess.getActivities().poll();

                            currentProcess.backupActivity(IOactivity);

                            int durationIO = IOactivity.getActivityDuration();

                            int startTimeIO = clockTime + durationCPU;
                            int endTimeIO = startTimeIO + durationIO;

                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcess.getProcessId(), startTimeIO));
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcess.getProcessId(), endTimeIO));

                            if(currentProcess.getActivities().isEmpty())
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), endTimeIO));
                        }
                        else
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), clockTime + durationCPU));

                    }
                }
            }

            System.out.println("CURRENT EVENT QUEUE:\n" + eventQueue);
            debugDisplay(processList, currentProcess);
        }
    }

    //  HELPER - SHORTEST REMAINING TIME
    //      FINAL BOSS ALGORITHM, HARD TO IMPLEMENT
    //      ** TAKE FREQUENT BREAKS TO REFRESH **
    private static void simulateSPN(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue, double alpha)
    {
        int clockTime = 0;
        int lastClockTime, elapsedTime;

        ProcessEvent currentEvent = null;

        SimulatedProcess currentProcess = null;

        System.out.println("INITIAL STATE");
        debugDisplay(processList, null);

        while(!eventQueue.isEmpty())
        {
            //  ASSIGN VALUE BEFORE CLOCK TIME CHANGES.
            lastClockTime = clockTime;

            currentEvent = eventQueue.poll();

            //  MOVE CLOCK TIME UP TO MATCH CURRENT EVENT
            if(currentEvent.getTime() > clockTime)
                clockTime = currentEvent.getTime();

            //  CALCULATE ELAPSED TIME BETWEEN LAST EVENT AND CURRENT EVENT
            elapsedTime = clockTime - lastClockTime;

            displayTime(clockTime, lastClockTime);
            System.out.println(currentEvent);

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            if(currentProcess != null)
            {
                currentProcess.getBackupActivities().peek().decrementActivityDuration(elapsedTime);

                currentProcess.decrementRemainingBurstTime(elapsedTime);
            }

            //  PARSE EVENT (SPN) (SERVICE GIVEN)
            if(currentEvent.getType() == ProcessEvent.Type.ARRIVE ||
                    currentEvent.getType() == ProcessEvent.Type.UNBLOCK)
            {
                if(eventProcess.getArrivalTime() == -1)
                    eventProcess.setArrivalTime(clockTime);

                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);

                if(currentProcess != null)
                {
                    if(eventProcess.getRemainingBurstTime(alpha) < currentProcess.getRemainingBurstTime(alpha))
                    {
                        currentProcess.restoreBackup();

                        System.out.println("CURRENT PROCESS ID: " + currentProcess.getProcessId());
                        System.out.println("EVENT QUEUE: " + eventQueue);

                        //  Temporary Event Queue
                        PriorityQueue<ProcessEvent> tempEventQueue = new PriorityQueue<>();

                        while(!eventQueue.isEmpty())
                        {
                            ProcessEvent event = eventQueue.poll();

                            System.out.println("DEBUG: CHECKING EVENT " + event);

                            if(event.getProcessId() != currentProcess.getProcessId())
                                tempEventQueue.add(event);
                        }

                        /*
                        for(int index = 0; index < eventQueue.size(); index++)
                        {
                            ProcessEvent event = eventQueue.poll();

                            System.out.println("DEBUG: CHECKING EVENT " + event);

                            if(event.getProcessId() != currentProcess.getProcessId())
                                eventQueue.add(event);
                        }
                        */

                        eventQueue = tempEventQueue;

                        eventQueue.add(new ProcessEvent(ProcessEvent.Type.TIMEOUT, currentProcess.getProcessId(), clockTime));

                        currentProcess = null;
                    }
                }
            }
            else if(currentEvent.getType() == ProcessEvent.Type.BLOCK)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.BLOCKING);

                eventProcess.clearBackup();

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }
            else if(currentEvent.getType() == ProcessEvent.Type.TIMEOUT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);
            }
            else if(currentEvent.getType() == ProcessEvent.Type.EXIT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.TERMINATED);

                eventProcess.setFinishTime(clockTime);

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }

            //  IF THERE ARE PROCESSES THAT CAN BE RUN,
            //  AND CURRENT PROCESS IS DONE/EMPTY, MARK ONE TO BE RUN
            if(currentProcess == null)
            {
                for(SimulatedProcess process : processList)
                {
                    if(process.getCurrentState() == SimulatedProcess.PState.READY)
                    {
                        if(currentProcess == null)
                            currentProcess = process;
                        else
                        {
                            System.out.println("PROCESS #" + process.getProcessId() + " REMAINING BURST TIME: " + process.getRemainingBurstTime(alpha));
                            System.out.println("PREVIOUS PROCESS REMAINING BURST TIME: " + currentProcess.getRemainingBurstTime(alpha));

                            if(process.getRemainingBurstTime(alpha) < currentProcess.getRemainingBurstTime(alpha))
                                currentProcess = process;
                        }
                    }
                }
            }

            //  RUN ACTIVITIES
            if(currentProcess != null)
            {
                if(currentProcess.getCurrentState() == SimulatedProcess.PState.READY)
                {
                    currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                    currentProcess.addResponseTime(clockTime - currentProcess.getCurrentWaitTime());

                    if(currentProcess.getStartTime() == -1)
                        currentProcess.setStartTime(clockTime);

                    if(!currentProcess.getActivities().isEmpty())
                    {
                        Activity CPUactivity = currentProcess.getActivities().poll();

                        currentProcess.backupActivity(CPUactivity);

                        int durationCPU = CPUactivity.getActivityDuration();

                        if(!currentProcess.getActivities().isEmpty())
                        {
                            Activity IOactivity = currentProcess.getActivities().poll();

                            currentProcess.backupActivity(IOactivity);

                            int durationIO = IOactivity.getActivityDuration();

                            int startTimeIO = clockTime + durationCPU;
                            int endTimeIO = startTimeIO + durationIO;

                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcess.getProcessId(), startTimeIO));
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcess.getProcessId(), endTimeIO));

                            if(currentProcess.getActivities().isEmpty())
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), endTimeIO));
                        }
                        else
                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcess.getProcessId(), clockTime + durationCPU));

                    }
                }
            }

            System.out.println("CURRENT EVENT QUEUE:\n" + eventQueue);
            debugDisplay(processList, currentProcess);

        }
    }

    //  HELPER - VIRTUAL ROUND ROBIN
    private static void simulateVRR(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue, int quantum)
    {
        int clockTime = 0;
        int lastClockTime, elapsedTime;

        ProcessEvent currentEvent;

        SimulatedProcess currentProcess = null;

        //  QUEUE for Processes
        Queue<SimulatedProcess> readyProcessQueue = new LinkedList<>();
        Queue<SimulatedProcess> auxiliaryProcessQueue = new LinkedList<>();

        //  DISPLAY INFORMATION
        //  debugDisplay("VIRTUAL ROUND ROBIN", processQueue, processList);
        System.out.println("INITIAL STATE");
        debugDisplay(readyProcessQueue, auxiliaryProcessQueue, processList, currentProcess);

        while(!eventQueue.isEmpty())
        {
            lastClockTime = clockTime;

            //  GRAB NEXT EVENT
            currentEvent = eventQueue.poll();
            ProcessEvent.Type currentEventType = currentEvent.getType();

            //  IF TIMESTAMP IS AHEAD OF CLOCK, SET CLOCK TO MATCH
            if(currentEvent.getTime() > clockTime)
                clockTime = currentEvent.getTime();

            //  PRINT CLOCK TIME AND CURRENT EVENT
            displayTime(clockTime, lastClockTime);
            System.out.println(currentEvent);

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            //  PARSE EVENT
            if(currentEventType == ProcessEvent.Type.ARRIVE)
            {
                if(eventProcess.getArrivalTime() == -1)
                    eventProcess.setArrivalTime(clockTime);

                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);

                readyProcessQueue.add(eventProcess);
            }
            else if(currentEventType == ProcessEvent.Type.BLOCK)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.BLOCKING);

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }
            else if(currentEventType == ProcessEvent.Type.TIMEOUT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);

                if(currentProcess != null)
                {
                    if (eventProcess.getProcessId() == currentProcess.getProcessId())
                    {
                        readyProcessQueue.add(currentProcess);

                        if (eventProcess.getProcessId() == currentProcess.getProcessId())
                            currentProcess = null;
                    }
                }
            }
            else if(currentEventType == ProcessEvent.Type.UNBLOCK)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.READY);

                eventProcess.setCurrentWaitTime(clockTime);

                auxiliaryProcessQueue.add(eventProcess);
            }
            else if(currentEventType == ProcessEvent.Type.EXIT)
            {
                eventProcess.setCurrentState(SimulatedProcess.PState.TERMINATED);

                eventProcess.setFinishTime(clockTime);

                if(currentProcess != null)
                {
                    if(eventProcess.getProcessId() == currentProcess.getProcessId())
                        currentProcess = null;
                }
            }

            //  IF THERE ARE NO PROCESSES THAT CAN BE RUN,
            //  AND CURRENT PROCESS IS DONE/EMPTY, MARK ONE TO BE RUN
            if(currentProcess == null)
            {
                if(!auxiliaryProcessQueue.isEmpty())
                    currentProcess = auxiliaryProcessQueue.poll();
                else
                    currentProcess = readyProcessQueue.poll();
            }

            //  RUN ACTIVITIES
            if(currentProcess != null)
            {
                int currentProcessId = currentProcess.getProcessId();

                if(currentProcess.getCurrentState() == SimulatedProcess.PState.READY)
                {
                    currentProcess.setCurrentState(SimulatedProcess.PState.RUNNING);

                    currentProcess.addResponseTime(clockTime - currentProcess.getCurrentWaitTime());

                    if(currentProcess.getStartTime() == -1)
                        currentProcess.setStartTime(clockTime);

                    if(!currentProcess.getActivities().isEmpty())
                    {
                        int durationCPU = currentProcess.getActivities().peek().getActivityDuration();

                        if(quantum < durationCPU)
                        {
                            currentProcess.getActivities().peek().decrementActivityDuration(quantum);

                            // currentProcess.incrementServiceTime(quantum);

                            eventQueue.add(new ProcessEvent(ProcessEvent.Type.TIMEOUT, currentProcessId, clockTime + quantum));
                        }
                        else
                        {
                            currentProcess.getActivities().poll();

                            // currentProcess.incrementServiceTime(durationCPU);

                            if(!currentProcess.getActivities().isEmpty())
                            {
                                int durationIO = currentProcess.getActivities().poll().getActivityDuration();

                                int startTimeIO = clockTime + durationCPU;
                                int endTimeIO = startTimeIO + durationIO;

                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.BLOCK, currentProcessId, startTimeIO));
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.UNBLOCK, currentProcessId, endTimeIO));

                                if(currentProcess.getActivities().isEmpty())
                                    eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcessId, endTimeIO));
                            }
                            else
                                eventQueue.add(new ProcessEvent(ProcessEvent.Type.EXIT, currentProcessId, clockTime + durationCPU));

                        }
                    }
                }
            }

            /*
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

             */

            debugDisplay(readyProcessQueue, auxiliaryProcessQueue, processList, currentProcess);
        }
    }

    //  HELPER - FIRST COME FIRST SERVE
    private static void simulateFCFS(LinkedList<SimulatedProcess> processList, Queue<ProcessEvent> eventQueue)
    {
        int clockTime = 0;
        int lastClockTime, elapsedTime;

        //  Is current process necessary? (Can peek() top queue)
        ProcessEvent currentEvent;

        SimulatedProcess currentProcess = null;

        //  QUEUE for Processes
        Queue<SimulatedProcess> processQueue = new LinkedList<>();

        //  DEBUG DISPLAY
        //  debugDisplay("FIRST COME FIRST SERVE", processQueue, processList);
        System.out.println("INITIAL STATE");
        debugDisplay(processQueue, processList, null);

        System.out.println();

        while(!eventQueue.isEmpty())
        {
            lastClockTime = clockTime;

            //  GRAB NEXT EVENT
            currentEvent = eventQueue.poll();

            //  IF TIMESTAMP IS AHEAD OF CLOCK, SET CLOCK TO MATCH
            if(currentEvent.getTime() > clockTime)
                clockTime = currentEvent.getTime();

            elapsedTime = clockTime - lastClockTime;

            //  PRINT CLOCK TIME AND CURRENT EVENT
            displayTime(clockTime, lastClockTime);
            System.out.println(currentEvent);

            SimulatedProcess eventProcess = processList.get(currentEvent.getProcessId());

            //  PARSE CURRENT EVENT (HELPER)
            parseEvent(processQueue, currentEvent, eventProcess, clockTime);

            //  HEAD OF QUEUE IS CURRENT RUNNING PROCESS! REMEMBER
            if(!processQueue.isEmpty())
            {
                currentProcess = processQueue.peek();
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
                        // currentProcess.incrementServiceTime(durationCPU);

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
            //  debugDisplay("", processQueue, processList);
            debugDisplay(processQueue, processList, currentProcess);
        }
    }

    //  HELPER: DISPLAY FINAL STATISTICS
    private static void displayFinalStats(LinkedList<SimulatedProcess> processList)
    {
        double statisticNumber = 0;

        System.out.print("\n____\n\n");

        System.out.print("MEAN TURNAROUND TIME: ");
        for(SimulatedProcess process : processList)
            statisticNumber += process.getTurnaroundTime();

        System.out.println(statisticNumber / processList.size());
            statisticNumber = 0;

        System.out.print("MEAN NORMALIZED TURNAROUND TIME: ");
        for(SimulatedProcess process : processList)
            statisticNumber += process.getNormalizedTurnaroundTime();

        System.out.println(statisticNumber / processList.size());
            statisticNumber = 0;

        System.out.print("MEAN AVERAGE RESPONSE TIME: ");
        for(SimulatedProcess process : processList)
            statisticNumber += process.getAverageResponseTime();

        System.out.println(statisticNumber / processList.size());
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

    //  HELPER - SINCE FEEDBACK MAY USE MULTIPLE PROCESS QUEUES
    //      CHECK ALL PROCESS QUEUES
    private static boolean isProcessQueuesEmpty(LinkedList<Queue<SimulatedProcess>> processQueues)
    {
        for(Queue<SimulatedProcess> processQueue : processQueues)
        {
            if(!processQueue.isEmpty())
                return false;
        }

        return true;
    }

    //  HELPER - IN FEEDBACK ALGORITHM, THE TOP PROCESS IN THE HIGHEST PRIORITY
    //      QUEUE WILL BE RAN FIRST, GRAB THAT
    private static SimulatedProcess getHeadProcess(LinkedList<Queue<SimulatedProcess>> processQueues)
    {
        for(Queue<SimulatedProcess> processQueue : processQueues)
        {
            if(!processQueue.isEmpty())
                return processQueue.peek();
        }

        //  SHOULD NEVER REACH HERE
        return null;
    }

    //  DEBUG: DISPLAY STATE OF PROCESSES
    //      [FCFS]
    private static void debugDisplay(Queue<SimulatedProcess> processQueue, LinkedList<SimulatedProcess> processList, SimulatedProcess currentProcess)
    {
        System.out.println("CURRENT RUNNING PROCESS: ");
        if(currentProcess == null)
            System.out.println("\t--");
        else
            System.out.println(currentProcess);

        System.out.println("READY PROCESS QUEUE: ");
        displayProcessQueue(processQueue);

        displayCurrentProcessState(processList);
    }

    //  DEBUG: DISPLAY STATE OF PROCESSES
    //      [VRR]
    private static void debugDisplay(Queue<SimulatedProcess> readyProcessQueue, Queue<SimulatedProcess> auxiliaryProcessQueue, LinkedList<SimulatedProcess> processList, SimulatedProcess currentProcess)
    {
        System.out.println("CURRENT RUNNING PROCESS: ");
        if(currentProcess == null)
            System.out.println("\t--");
        else
            System.out.println(currentProcess);

        System.out.println("READY PROCESS QUEUE: ");
        displayProcessQueue(readyProcessQueue);

        System.out.println("AUXILIARY PROCESS QUEUE: ");
        displayProcessQueue(auxiliaryProcessQueue);

        displayCurrentProcessState(processList);
    }

    //  DEBUG: DISPLAY STATE OF PROCESSES
    //      [HRRN] [SPN]
    private static void debugDisplay(LinkedList<SimulatedProcess> processList, SimulatedProcess currentProcess)
    {
        System.out.println("CURRENT RUNNING PROCESS: ");
        if(currentProcess == null)
            System.out.println("\t--");
        else
            System.out.println(currentProcess);

        displayCurrentProcessState(processList);
    }

    //  DEBUG: DISPLAY STATE OF PROCESSES
    //      [FEEDBACK]
    private static void debugDisplay(LinkedList<Queue<SimulatedProcess>> processQueues, LinkedList<SimulatedProcess> processList, SimulatedProcess currentProcess)
    {
        System.out.println("CURRENT RUNNING PROCESS: ");
        if(currentProcess == null)
            System.out.println("\t--");
        else
            System.out.println(currentProcess);

        for(int index = 0; index < processQueues.size(); index++)
        {
            System.out.println("READY PROCESS QUEUE #" + index + ": ");
            displayProcessQueue(processQueues.get(index));
        }

        displayCurrentProcessState(processList);
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
        if(processQueue.isEmpty())
            System.out.println("\tEMPTY");
        else
        {
            for (SimulatedProcess process : processQueue)
                System.out.println(process);
        }
    }

    //  HELPER - Display time information
    private static void displayTime(int clockTime, int prevClockTime)
    {
        System.out.println("\nCURRENT CLOCK: " + clockTime);
        System.out.println("LAST CLOCK: " + prevClockTime);
        System.out.println("ELAPSED TIME: " + (clockTime - prevClockTime));
    }

}