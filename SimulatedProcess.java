
//  JOHN ZHAO
//  CS 4348.003
//  PROJECT THREE

//  REPRESENTATION OF (SIMULATED) PROCESS

import javax.accessibility.AccessibleIcon;
import java.util.LinkedList;
import java.util.Queue;

public class SimulatedProcess implements Comparable<SimulatedProcess>
{
    private static int processId = 0;

    private PState currentState;

    private Queue<Activity> activityList;

    private Queue<Activity> backupActivityList;

    private LinkedList<Integer> responseTimes;

    private int arrivalTime, startTime, finishTime, serviceTime;
    private int actualProcessId;

    private int priority;

    private int currentWaitTime, startedRunningTime;

    private double remainingBurstTime;

    //  CONSTRUCTOR - Default
    //      1.  SET State to UNINITIALIZED
    public SimulatedProcess()
    {
        activityList = new LinkedList<>();
        backupActivityList = new LinkedList<>();
        responseTimes = new LinkedList<>();

        currentState = PState.UNINITIALIZED;

        arrivalTime = startTime = finishTime = currentWaitTime = startedRunningTime = -1;

        serviceTime = 0;

        priority = 0;

        remainingBurstTime = 0;

        actualProcessId = processId++;
    }

    //  GETTER - Return activities the Process has 'yet to do'
    public Queue<Activity> getActivities()     {    return activityList;    }

    public Queue<Activity> getBackupActivities()    {   return backupActivityList;  }

    public void clearBackup()   {   backupActivityList.clear(); }

    public void calculateRemainingBurstTime()
    {
        for(Activity activity : getActivities())
        {
            if(activity.getType() == Activity.Type.CPU)
                remainingBurstTime += activity.getActivityDuration();
        }
    }

    public void calculateServiceTime()
    {
        for(Activity activity : getActivities())
        {
            if(activity.getType() == Activity.Type.CPU)
                serviceTime += activity.getActivityDuration();
        }
    }

    //  LOWER REMAINING BURST TIME
    public void decrementRemainingBurstTime(int duration)
    {
        remainingBurstTime -= duration;
    }

    //  FOR USE WITH SPN ALGORITHM
    public void restoreBackup()
    {
        remainingBurstTime = 0;

        while(!activityList.isEmpty())
        {
            Activity activity = activityList.poll();

            backupActivityList.add(activity);
        }

        while(!backupActivityList.isEmpty())
        {
            Activity activity = backupActivityList.poll();

            if(activity.getType() == Activity.Type.CPU)
                remainingBurstTime += activity.getActivityDuration();

            activityList.add(activity);
        }
    }

    public void backupActivity(Activity activity)   {   backupActivityList.add(activity);   }
    public void addActivity(Activity activity) {    activityList.add(activity); }

    public void incrementPriority(int priority)     {   this.priority += priority;  }

    //  HELPER - ADD RESPONSE TIME (AFTER THE PROCESS FINALLY STOPS WAITING)
    public void addResponseTime(int responseTime)
    {
        if(responseTime > 0)
            responseTimes.add(responseTime);
    }

    //  HELPER - GET TOTAL RESPONSE TIMES
    public double getTotalResponseTime()
    {
        double totalResponseTime = 0;

        if(responseTimes.isEmpty())
            return 0;

        for(int responseTime : responseTimes)
            totalResponseTime += responseTime;

        return totalResponseTime;
    }

    //  HELPER - CALCULATE RESPONSE RATIO [HRRN], BY ADDING SERVICE TIME(S).
    public double getResponseRatio(int clockTime)
    {
        int responseTime = 0;

        if(!responseTimes.isEmpty())
        {
            for(int prevResponseTime : responseTimes)
                responseTime += prevResponseTime;
        }

        return (responseTime + (clockTime - getCurrentWaitTime()) + getRemainingBurstTime()) / getRemainingBurstTime();
    }

    //  HELPER - CALCULATE RESPONSE RATIO [HRRN], USING ALPHA/PREDICTED SERVICE TIME.
    public double getResponseRatio(int clockTime, double alpha)
    {
        int responseTime = 0;
        double predictedBurstTime = 0;

        if(!responseTimes.isEmpty())
        {
            for(int prevResponseTime : responseTimes)
                responseTime += prevResponseTime;
        }

        int burstNumber = 0;

        System.out.println("PROCESS #" + getProcessId() + " | ");

        for(Activity activity : getActivities())
        {
            if(activity.getType() == Activity.Type.CPU)
            {
                if (burstNumber == 0)
                    predictedBurstTime = activity.getActivityDuration();
                else
                    predictedBurstTime = (alpha * activity.getActivityDuration()) + ((1 - alpha) * predictedBurstTime);

                System.out.println("\tPREDICTED BURST TIME: " + predictedBurstTime);

                burstNumber++;
            }
        }

        return (responseTime + (clockTime - getCurrentWaitTime()) + predictedBurstTime) / predictedBurstTime;
    }

    public double getRemainingBurstTime()   {   return remainingBurstTime;    }

    //  HELPER - Calculate service time with alpha given (Exponential Averaging)
    public double getRemainingBurstTime(double alpha)
    {
        int burstNumber = 0;
        double predictedBurstTime = 0;

        System.out.println("PROCESS #" + getProcessId() + ": ");

        for(Activity activity : backupActivityList)
        {
            if(activity.getType() == Activity.Type.CPU)
            {
                predictedBurstTime = activity.getActivityDuration();

                System.out.println("\tPREDICTED BURST TIME: " + predictedBurstTime);

                burstNumber++;
            }
        }

        for(Activity activity : getActivities())
        {
            if(activity.getType() == Activity.Type.CPU)
            {
                if(burstNumber == 0)
                    predictedBurstTime = activity.getActivityDuration();
                else
                    predictedBurstTime = (alpha * activity.getActivityDuration()) + ((1 - alpha) * predictedBurstTime);

                System.out.println("\tPREDICTED BURST TIME: " + predictedBurstTime);

                burstNumber++;
            }
        }

        return predictedBurstTime;
    }


    //  SETTER - Basic Setter methods for changing state(s), and time attributes
    public void setCurrentWaitTime(int currentWaitTime) {   this.currentWaitTime = currentWaitTime; }
    public void setArrivalTime(int arrivalTime) {   this.arrivalTime = arrivalTime; }
    public void setStartTime(int startTime)     {   this.startTime = startTime;     }
    public void setFinishTime(int finishTime)   {   this.finishTime = finishTime;   }
    public void setCurrentState(PState state)   {   currentState = state;           }
    public void setStartedRunningTime(int runningTime)  {   startedRunningTime = runningTime;   }

    //  GETTER - Basic Getter methods for Process attributes
    public int getProcessId()       {   return actualProcessId; }
    public int getArrivalTime()     {   return arrivalTime;     }
    public int getStartTime()       {   return startTime;       }
    public int getFinishTime()      {   return finishTime;      }
    public int getServiceTime()     {   return serviceTime;     }
    public int getCurrentWaitTime()    {    return currentWaitTime; }
    public int getStartedRunningTime() {    return startedRunningTime;   }
    public int getPriority()           {    return priority;    }

    /*
    public double getRemainingBurstTime()
    {
        double remainingBurstTime = 0;

        for(Activity activity : getActivities())
        {
            if(activity.getType() == Activity.Type.CPU)
                remainingBurstTime += activity.getActivityDuration();
        }

        return remainingBurstTime;
    }
     */

    public PState getCurrentState() {   return currentState;    }

    //  GETTER - Derived attribute, from finish time and arrival time
    public double getTurnaroundTime()  {   return finishTime - arrivalTime;    }

    //  GETTER - Derived attribute, from turnaround time and service time
    public double getNormalizedTurnaroundTime()    {    return getTurnaroundTime() / serviceTime;  }

    //  HELPER - Take average of response times (if none, return 0)
    public double getAverageResponseTime()
    {
        if(responseTimes.isEmpty())
            return 0;

        return getTotalResponseTime() / responseTimes.size();
    }

    //  ENUM - States the Process can be in
    enum PState
    {
        RUNNING, BLOCKING, TERMINATED,
        READY, UNINITIALIZED;
    }

    //  HELPER - Debugging is easier using this toString override
    @Override
    public String toString()
    {
        String returnString = "\t[PROCESS ID: " + getProcessId() + " | PROCESS STATE: " + getCurrentState() + " | ARRIVAL TIME: " + getArrivalTime() + " | START TIME: " + getStartTime() +
                " | FINISH TIME: " + getFinishTime() + " | SERVICE TIME: " + getServiceTime() + " | REMAINING CPU BURST TIME: " + getRemainingBurstTime();

        if(serviceTime > 0 && getTurnaroundTime() > 0)
            returnString += " | TURNAROUND TIME: " + getTurnaroundTime() + " | NORMALIZED TURNAROUND TIME: " + getNormalizedTurnaroundTime();

        if(responseTimes.size() > 0)
            returnString += " | AVERAGE RESPONSE TIME: " + getAverageResponseTime();

        returnString += "]";

        if(!responseTimes.isEmpty())
            returnString += "\n\t\tRESPONSE TIMES: " + responseTimes;

        if(!activityList.isEmpty())
            returnString += "\n\t\tACTIVITIES: " + activityList;

        if(!backupActivityList.isEmpty())
            returnString += "\n\t\tBACKUP ACTIVITIES: " + backupActivityList;

        return returnString;
    }

    //  OVERRIDE - Compare Process by total service time.
    @Override
    public int compareTo(SimulatedProcess otherProcess)
    {
        if(this.getRemainingBurstTime() >= otherProcess.getRemainingBurstTime())
            return 1;
        else if(this.getRemainingBurstTime() < otherProcess.getRemainingBurstTime())
            return -1;

        return 0;
    }

}