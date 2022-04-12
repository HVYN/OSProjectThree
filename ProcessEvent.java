
//  JOHN ZHAO
//  CS 4348.003
//  PROJECT THREE

//  REPRESENTATION OF EVENT

//  EACH ONE ASSOCIATED WITH A PROCESS

public class ProcessEvent implements Comparable<ProcessEvent>
{
    private Type type;

    private int processId;
    private int time;

    public ProcessEvent(Type type, int processId, int time)
    {
        this.type = type;

        this.processId = processId;
        this.time = time;
    }

    public Type getType()   {   return type;    }

    public int getProcessId()   {   return processId;   }
    public int getTime()    {   return time;    }

    enum Type
    {
        ARRIVE, BLOCK,
        EXIT, UNBLOCK, TIMEOUT;
    }

    @Override
    public String toString()
    {
        return "[EVENT TYPE: " + getType() + " | PROCESS ID: " + getProcessId() +
                " | TIME: " + getTime() + "]";
    }

    @Override
    public int compareTo(ProcessEvent otherEvent)
    {
        if(this.getTime() > otherEvent.getTime())
            return 1;
        else if(this.getTime() < otherEvent.getTime())
            return -1;
        else
            return 0;
    }

}
