package angrygenesis.mac;

import angrygenesis.mac.payloads.MACFramePayload;
import angrygenesis.mac.payloads.BeaconFramePayload;
import angrygenesis.mac.payloads.CommandFramePayload;

/**
 *
 * @author dools
 */
public class MACFrame
{
    private final int[] rawData;
    
    private final FrameControl frameControl;
    
    // The sequence number is a number incremented by one for each frame sent. 
    // It is used to as a reference number to acknowledge frames. This counter
    // is allowed to roll over (i.e. after 255 it will be 0 again).
    private final int seqNum;
    
    private int destPAN;
    private String destAddress;
    
    private int srcPAN;
    private String srcAddress;
    
    private MACFramePayload payload = null;
    

    public MACFrame(int[] _rawData) throws Exception
    {
        this.rawData = _rawData;
        
        //---- Frame control
        this.frameControl = new FrameControl(rawData);
        
        //---- Packet sequence number
        this.seqNum = rawData[2];
        
        //---- Addressing fields and payload content
        int pointer = 3;
        switch(this.frameControl.getFrameType())
        {
            case FRAME_TYPE_BEACON:
                
                // no destination address/pan in beacon frames
                pointer = this.getAddresses(pointer, false);
                
                this.payload = new BeaconFramePayload(this.rawData, pointer);
                break;
                
                
            case FRAME_TYPE_DATA:            
                this.getAddresses(pointer, true);
                break;
                
                
            case FRAME_TYPE_MAC_CMD:
                pointer = this.getAddresses(pointer, true);
                
                this.payload = new CommandFramePayload(this.rawData, pointer);
                break;
                
            
            case FRAME_TYPE_ACK:
                // no addresses in ACK frames
            default:
                this.srcPAN = 0x00;
                this.srcAddress = "";
                this.destPAN = 0x00;
                this.destAddress = "";
        }
    }
    
    private int getAddresses(int start, boolean getDest)
    {
        int pointer = start;
        
        //---- Destination PAN and address
        if(getDest)
        {
            this.destPAN = getPAN(pointer);
            pointer += 2;
            
            switch(frameControl.getDestAddrMode())
            {
                // 64-bit
                case EXTENDED:

                    this.destAddress = String.format("%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X",
                            rawData[pointer+7], rawData[pointer+6], rawData[pointer+5], rawData[pointer+4],
                            rawData[pointer+3], rawData[pointer+2], rawData[pointer+1], rawData[pointer]);
                    
                    pointer += 8;
                      
                    break;

                // 16-bit
                case SHORT:
                    this.destAddress = String.format("0x%04X", (rawData[pointer+1] << 8) | rawData[pointer]);
                    pointer += 2;
                    break;
            }
        }
        else
        {
            this.destPAN = 0x00;
            this.destAddress = "";
        }
        
        
        
        //----  Source PAN and address
        
        // if intra PAN field is set, then the src and dest PAN IDs are the same
        if(this.frameControl.isIntraPAN())
        {
            this.srcPAN = this.destPAN;
        }
        else
        {
            this.srcPAN = getPAN(pointer);
            pointer += 2;
        }

        switch(frameControl.getSrcAddrMode())
        {
            // 64-bit
            case EXTENDED:

                this.srcAddress = String.format("%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X",
                            rawData[pointer+7], rawData[pointer+6], rawData[pointer+5], rawData[pointer+4],
                            rawData[pointer+3], rawData[pointer+2], rawData[pointer+1], rawData[pointer]);
                    
                pointer += 8;

                break;

            // 16-bit
            case SHORT:
                this.srcAddress = String.format("0x%04X", (rawData[pointer+1] << 8) | rawData[pointer]);
                pointer += 2;
                break;
        }
        
        return pointer;
    }
    
    private int getPAN(int offset)
    {
        return (rawData[offset+1] << 8) | rawData[offset];
    }
    
    
    public String toString()
    {
        String rtn = "{\n\tFrameControl = "+this.frameControl.getFrameType().toString()+",\n";
        rtn += "\tSecurity Enabled = "+this.frameControl.isSecurityEnabled() +",\n";
        rtn += "\tFrame Pending = "+this.frameControl.isFramePending() + ",\n";
        rtn += "\tAck Request = "+this.frameControl.isAckReq()+ ",\n";
        rtn += "\tIntra Pan = "+this.frameControl.isIntraPAN()+",\n";
        
        
        rtn += "\tDestination Addressing Mode = "+this.frameControl.getDestAddrMode().toString() + ",\n";
        rtn += "\tSource Addressing Mode = "+this.frameControl.getSrcAddrMode().toString() + ",\n";
        
        rtn += "\tSequence Number = "+this.seqNum + ",\n";
        
        
        if( (this.frameControl.getDestAddrMode() == AddressingMode.EXTENDED) ||
            (this.frameControl.getDestAddrMode() == AddressingMode.SHORT))
        {
            rtn += String.format("\tDestination Address = %s\n",this.destAddress);
            rtn += String.format("\tDestination PAN = 0x%04X\n", this.destPAN) ;
        }
        
        
        if( (this.frameControl.getSrcAddrMode() == AddressingMode.EXTENDED) ||
            (this.frameControl.getSrcAddrMode() == AddressingMode.SHORT))
        {
            rtn += String.format("\tSource Address = %s\n",this.srcAddress);
            rtn += String.format("\tSource PAN = 0x%04X\n", this.srcPAN) ;
        }
        
        if(this.payload != null)
        {
            rtn += "\tPayload = "+this.payload.toString();
        }
        
        
        rtn += "\n}";
        
        return rtn;
    
    }

    public int[] getRawData() {
        return rawData;
    }

    public FrameControl getFrameControl() {
        return frameControl;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public int getDestPAN() {
        return destPAN;
    }

    public String getDestAddress() {
        return destAddress;
    }

    public int getSrcPAN() {
        return srcPAN;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public MACFramePayload getPayload() {
        return payload;
    }
    
    
    
}
