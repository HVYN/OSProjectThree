
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

public class SchedulerDriver
{
    public static void main(String[] args) throws FileNotFoundException
    {
        //  CHECK IF ARGS EXIST
        if(args.length > 0)
        {
            //  PRIMARY WAY OF READING FILE
            Scanner fileReader = new Scanner(new File(args[0]));

            //  PRIORITY QUEUE: STORE EVENTS
            PriorityQueue<ProcessEvent> eventQueue = new PriorityQueue<>();

            //  LINKED LIST: STORE PROCESSES, WHICH ARE QUEUES
            //  OF ACTIVITY INSTANCES.
            LinkedList<Queue<Activity>> processList = new LinkedList<>();

            int processId = 0;

            while(fileReader.hasNextLine())
            {
                String[] lineElements = fileReader.nextLine().split("\\s+");

                //  ADD NEW PROCESS (ID: processId)
                processList.add(new LinkedList<>());

                //  ITERATE THRU EACH LINE ELEMENT
                for(int element = 0; element < lineElements.length; element++)
                {
                    if(element == 0)
                        eventQueue.add(new ProcessEvent(ProcessEvent.Type.ARRIVE, processId,
                                Integer.parseInt(lineElements[element])));
                    else
                    {
                        if(lineElements[element].equals("CPU"))
                            processList.get(processId).add(new Activity(Activity.Type.CPU,
                                    Integer.parseInt(lineElements[++element])));
                        else if(lineElements[element].equals("IO"))
                            processList.get(processId).add(new Activity(Activity.Type.IO,
                                    Integer.parseInt(lineElements[++element])));
                    }
                }

                //  INCREMENT PROCESS ID
                processId++;
            }

            //  DEBUG: PRINT THRU QUEUE TO CHECK IF EVERYTHING'S ORDERED RIGHT
            if(eventQueue.size() > 0)
            {
                while(!eventQueue.isEmpty())
                {
                    System.out.println(eventQueue.poll());
                }
            }

            System.out.println();

            //  DEBUG: PRINT THRU LINKEDLIST OF PROCESSES (Represented as QUEUES)
            for(Queue<Activity> process : processList)
            {
                while(!process.isEmpty())
                {
                    System.out.print(process.poll() + " ");
                }

                System.out.println();
            }

        }
        else
            System.out.println("MISSING ARGUMENTS!");
    }
}
