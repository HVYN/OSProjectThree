
//  JOHN ZHAO
//  CS 4348.003
//  PROJECT THREE

//  REPRESENTATION OF (SIMULATED) PROCESS

import java.util.LinkedList;
import java.util.Queue;

public class SimulatedProcess
{
    private static int processId = 0;

    private PState currentState;

    private Queue<Activity> activityList;

    private int arrivalTime, startTime, finishTime;
    private int actualProcessId;

    private boolean arrived;

    public SimulatedProcess()
    {
        currentState = PState.UNINITIALIZED;

        activityList = new LinkedList<>();

        arrivalTime = startTime = finishTime = -1;

        actualProcessId = processId++;

        arrived = false;
    }

    public Queue<Activity> getActivities() {    return activityList;    }

    public void addActivity(Activity activity)
    {
        activityList.add(activity);
    }

    public void arrived()       {   arrived = true; }
    public boolean hasArrived() {   return arrived; }

    public void setArrivalTime(int arrivalTime) {   this.arrivalTime = arrivalTime; }
    public void setStartTime(int startTime)     {   this.startTime = startTime;     }
    public void setFinishTime(int finishTime)   {   this.finishTime = finishTime;   }
    public void setCurrentState(PState state)   {   currentState = state;           }

    public int getProcessId()       {   return actualProcessId; }
    public int getArrivalTime()     {   return arrivalTime;     }
    public int getStartTime()       {   return startTime;       }
    public int getFinishTime()      {   return finishTime;      }
    public PState getCurrentState() {   return currentState;    }

    enum PState
    {
        RUNNING, BLOCKING, TERMINATED, READY, UNINITIALIZED, UNBLOCKING;
    }

    @Override
    public String toString()
    {
        String returnString = "\t[PROCESS ID: " + getProcessId() + " | PROCESS STATE: " + getCurrentState() + " | ARRIVAL TIME: " + getArrivalTime() + " | START TIME: " + getStartTime() + " | FINISH TIME: " + getFinishTime();

        if(!activityList.isEmpty())
            returnString += " | ACTIVITIES: " + activityList;

        return returnString + "]";
    }
}
