
//  JOHN ZHAO
//  CS 4348.003
//  PROJECT THREE

//  REPRESENTATION OF EVENT

//  EACH ONE ASSOCIATED WITH A PROCESS

public class ProcessEvent implements Comparable<ProcessEvent>
{
    //  TYPE - What kind of Event is it
    //  PROCESSID - What process the event is associated with
    //  TIME - When does the event take place
    private Type type;

    private int processId;
    private int time;

    //  CONSTRUCTOR
    public ProcessEvent(Type type, int processId, int time)
    {
        this.type = type;

        this.processId = processId;
        this.time = time;
    }

    //  GETTER - Return Event type
    public Type getType()   {   return type;    }

    //  GETTER - Process Id
    public int getProcessId()   {   return processId;   }

    //  GETTER - Return when event occurs
    public int getTime()    {   return time;    }

    //  ENUM - Event Type
    enum Type
    {
        ARRIVE, BLOCK,
        EXIT, UNBLOCK, TIMEOUT;
    }

    @Override
    public String toString()
    {
        return "\t[EVENT TYPE: " + getType() + " | PROCESS ID: " + getProcessId() +
                " | START TIME: " + getTime() + "]";
    }

    //  OVERRIDE - Help with PriorityQueue in comparing Events by time.
    @Override
    public int compareTo(ProcessEvent otherEvent)
    {
        if(this.getTime() >= otherEvent.getTime())
            return 1;
        else if(this.getTime() < otherEvent.getTime())
            return -1;

        return 0;
    }

}
