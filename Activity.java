
//  JOHN ZHAO
//  CS 4348.003
//  PROJECT THREE

//  REPRESENTATION OF ACTIVITY

//  TWO TYPES: CPU and IO

public class Activity
{
    //  TYPE - What kind of activity it is
    //  ACTIVITYDURATION - How long the activity takes
    private Type type;

    private int activityDuration;

    //  CONSTRUCTOR
    public Activity(Type type, int activityDuration)
    {
        this.type = type;
        this.activityDuration = activityDuration;
    }

    //  GETTER - Activity Duration
    public int getActivityDuration()    {   return activityDuration;    }

    //  GETTER - Return activity Type
    public Type getType()   {   return type;    }

    //  ENUM - Activity Type
    enum Type
    {
        CPU, IO;
    }

    @Override
    public String toString()
    {
        return "[ACTIVITY TYPE: " + getType() + " | ACTIVITY DURATION: " + getActivityDuration() + "]";
    }
}
