
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

    private LinkedList<Integer> responseTimes;

    private int arrivalTime, startTime, finishTime, serviceTime;
    private int actualProcessId;

    private int currentWaitTime;

    //  CONSTRUCTOR - Default
    //      1.  SET State to UNINITIALIZED
    public SimulatedProcess()
    {
        activityList = new LinkedList<>();
        responseTimes = new LinkedList<>();

        currentState = PState.UNINITIALIZED;

        arrivalTime = startTime = finishTime = currentWaitTime = -1;

        serviceTime = 0;

        actualProcessId = processId++;
    }

    //  GETTER - Return activities the Process has 'yet to do'
    public Queue<Activity> getActivities()     {    return activityList;    }

    public void addActivity(Activity activity) {    activityList.add(activity); }
    public void incrementServiceTime(int duration)  {   serviceTime += duration;    }

    public void addResponseTime(int responseTime)
    {
        if(responseTime > 0)
            responseTimes.add(responseTime);
    }

    public int getTotalResponseTime()
    {
        int totalResponseTime = 0;

        for(int responseTime : responseTimes)
            totalResponseTime += responseTime;

        return totalResponseTime;
    }

    //  SETTER - Basic Setter methods for changing state(s), and time attributes
    public void setCurrentWaitTime(int currentWaitTime) {   this.currentWaitTime = currentWaitTime; }
    public void setArrivalTime(int arrivalTime) {   this.arrivalTime = arrivalTime; }
    public void setStartTime(int startTime)     {   this.startTime = startTime;     }
    public void setFinishTime(int finishTime)   {   this.finishTime = finishTime;   }
    public void setCurrentState(PState state)   {   currentState = state;           }

    //  GETTER - Basic Getter methods for Process attributes
    public int getProcessId()       {   return actualProcessId; }
    public int getArrivalTime()     {   return arrivalTime;     }
    public int getStartTime()       {   return startTime;       }
    public int getFinishTime()      {   return finishTime;      }
    public int getServiceTime()     {   return serviceTime;     }
    public int getCurrentWaitTime()    {   return currentWaitTime; }
    public PState getCurrentState() {   return currentState;    }

    //  GETTER - Derived attribute, from finish time and arrival time;
    public int getTurnaroundTime()  {   return finishTime - arrivalTime;    }

    //  ENUM - States the Process can be in
    enum PState
    {
        RUNNING, BLOCKING, TERMINATED,
        READY, UNINITIALIZED;
    }

    @Override
    public String toString()
    {
        String returnString = "\t[PROCESS ID: " + getProcessId() + " | PROCESS STATE: " + getCurrentState() + " | ARRIVAL TIME: " + getArrivalTime() + " | START TIME: " + getStartTime() +
                " | FINISH TIME: " + getFinishTime() + " | SERVICE TIME: " + getServiceTime();

        if(serviceTime > 0 && getTurnaroundTime() > 0)
            returnString += " | TURNAROUND TIME: " + getTurnaroundTime() + " | NORMALIZED TURNAROUND TIME: " + getTurnaroundTime() + "/" + serviceTime;

        if(responseTimes.size() > 0)
            returnString += " | AVERAGE RESPONSE TIME: " + getTotalResponseTime() + "/" + responseTimes.size();

        returnString += "]";

        if(!responseTimes.isEmpty())
            returnString += "\n\t\tRESPONSE TIMES: " + responseTimes;

        if(!activityList.isEmpty())
            returnString += "\n\t\tACTIVITIES: " + activityList;

        return returnString;
    }
}