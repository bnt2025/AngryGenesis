package angrygenesis.mac;

/**
 *
 * @author dools
 */
public enum FrameControlType
{
    
    FRAME_TYPE_BEACON(0x00),
    FRAME_TYPE_DATA(0x01),
    FRAME_TYPE_ACK(0x02),
    FRAME_TYPE_MAC_CMD(0x03);
    
 
    private final int val;
    
    private FrameControlType(int value)
    {
        this.val = value;
    }

    public int getVal()
    {
        return val;
    }
    
    public static FrameControlType getFrameControlType(int rawData)
    {
        // 3-bit frame-type
        for(FrameControlType fct: FrameControlType.values())
        {
            if(rawData == fct.val) return fct;
        }
        
        return null;
    }
    
}
