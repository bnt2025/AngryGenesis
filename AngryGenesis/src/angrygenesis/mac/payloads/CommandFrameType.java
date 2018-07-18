package angrygenesis.mac.payloads;

/**
 *
 * @author dools
 */
public enum CommandFrameType
{
    
    ASSOCIATION_REQ(0x01),
    ASSOCIATION_RESP(0x02),
    DISSASSOCIATION_NOTIFICATION(0x03),
    DATA_REQ(0X04),
    PANID_CONFLICT_NOTIFICATION(0x05),
    ORPHAN_NOTIFICATION(0x06),
    BEACON_REQ(0x07),
    COORDINATOR_REALIGNMENT(0x08),
    GTS_REQ(0x09);
    

    private final int val;
    
    private CommandFrameType(int value)
    {
        this.val = value;
    }

    public int getVal()
    {
        return val;
    }
    
    public static CommandFrameType getCommandFrameType(int rawData)
    {
        for(CommandFrameType fct: CommandFrameType.values())
        {
            if(rawData == fct.val) return fct;
        }
        
        return null;
    }
    
}
