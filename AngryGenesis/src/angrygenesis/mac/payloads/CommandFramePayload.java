package angrygenesis.mac.payloads;

/**
 *
 * @author dools
 */
public class CommandFramePayload implements MACFramePayload
{
    //---- Command type
    private final CommandFrameType commandType;
    
    //---- Command Payload
    // @todo

    public CommandFramePayload(int[] rawData, int start)
    {
        this.commandType = CommandFrameType.getCommandFrameType(rawData[start]);
    }
    
    public String toString()
    {
        String rtn = "{\n";
        
        rtn += "\t\tCommand Type = " + this.commandType + ",\n";
        
        
        rtn += "\t}";                
                
        return rtn;
    }
    
}
