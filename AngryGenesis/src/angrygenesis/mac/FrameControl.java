package angrygenesis.mac;

/**
 *
 * @author dools
 */
public class FrameControl
{
    
    //---- field offset / masks
    private static final int FRAME_CONTROL_TYPE_MASK = 0x03;
    
    private static final int SECURITY_ENABLED_FIELD = 0x03;
    
    private static final int FRAME_PENDING_FIELD = 0x04;
    
    private static final int ACK_REQ_FIELD = 0x05;
    
    private static final int INTRA_PAN_FIELD = 0x06;
    
    private static final int DEST_ADDR_MODE_FIELD = 0x02;
    
    private static final int SRC_ADDR_MODE_FIELD = 0x06;
    
    
    //---- Variables
    
    private final FrameControlType frameType;
    
    
    // This bit indicates whether this frame is secured or not
    private final boolean securityEnabled;
    
    
    // This bit indicates whether the hub has pending frames for the device
    private final boolean framePending;
    
    
    // This bit indicates whether an Ack is requested for this frame or not
    private final boolean ackReq;
    
    
    // This bit is set when the source PAN is equal to the destination PAN and is thus omitted in the header.
    private final boolean intraPAN;
    
    
    //---- Addressing Modes
    private final AddressingMode destAddrMode;
    private final AddressingMode srcAddrMode;
    

            
    public FrameControl(final int[] rawData) throws Exception
    {
        //---- get frame control type
        this.frameType = FrameControlType.getFrameControlType(rawData[0] & FRAME_CONTROL_TYPE_MASK);
        
        if(this.frameType == null)
        {
            throw new Exception("FrameControl not recognised!");
        }
        
        
        //----  Security enabled?
        this.securityEnabled = ((rawData[0] >> SECURITY_ENABLED_FIELD) & 0x01) == 1; 
        
        //---- Frame Pendig field
        this.framePending = ((rawData[0] >> FRAME_PENDING_FIELD) & 0x01) == 1;
        
        //---- Ack Request field
        this.ackReq = ((rawData[0] >> ACK_REQ_FIELD) & 0x01) == 1;
        
        //---- Intra Pan field
        this.intraPAN = ((rawData[0] >> INTRA_PAN_FIELD) & 0x01) == 1;
        
        //---- Dest addressing mode field
        int amVal = (rawData[1] >> DEST_ADDR_MODE_FIELD) & 0x03;
        this.destAddrMode = AddressingMode.getAddressingMode(amVal);
        
        //---- Src addressing mode field
        amVal = (rawData[1] >> SRC_ADDR_MODE_FIELD) & 0x03;
        this.srcAddrMode = AddressingMode.getAddressingMode(amVal);
        
    }


    public FrameControlType getFrameType() {
        return frameType;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public boolean isFramePending() {
        return framePending;
    }

    public boolean isAckReq() {
        return ackReq;
    }

    public boolean isIntraPAN() {
        return intraPAN;
    }

    public AddressingMode getDestAddrMode() {
        return destAddrMode;
    }

    public AddressingMode getSrcAddrMode() {
        return srcAddrMode;
    }
    
    public String toString()
    {
        String rtn = "{\n\tFrameControl = "+this.getFrameType().toString()+",\n";
        rtn += "\tSecurity Enabled = "+this.isSecurityEnabled() +",\n";
        rtn += "\tFrame Pending = "+this.isFramePending() + ",\n";
        rtn += "\tAck Request = "+this.isAckReq()+ ",\n";
        rtn += "\tIntra Pan = "+this.isIntraPAN()+",\n";
        
        
        rtn += "\tDestination Addressing Mode = "+this.getDestAddrMode().toString() + ",\n";
        rtn += "\tSource Addressing Mode = "+this.getSrcAddrMode().toString() + ",\n";
        
        rtn += "}";
        
        return rtn;
    }
            
}
