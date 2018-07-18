/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package angrygenesis.mac.payloads;

/**
 *
 * @author dools
 */
public class BeaconFramePayload implements MACFramePayload
{
    //---- SuperFrame Specification
    private final int ssBeaconOrder;
    private final int ssSuperframeOrder;
    private final int ssFinalCAPslot;
    private final boolean ssBatteryLifeExtension;
    private final boolean ssPANcoordinator;
    private final boolean ssAssociationPermit;

    //---- GTS fields
    // @todo
    
    //---- Pending address fields
    // @todo
    
    //---- Beacon payload
    // @todo
    
    public BeaconFramePayload(int[] rawData, int start)
    {
        int pointer = start;
        
        this.ssBeaconOrder = rawData[pointer] & 0x0F;
        this.ssSuperframeOrder = (rawData[pointer] >> 4) & 0x0F;
        pointer++;
        
        this.ssFinalCAPslot = rawData[pointer] & 0x0F;
        this.ssBatteryLifeExtension = ((rawData[pointer] >> 4) & 0x01) == 0x01;
        this.ssPANcoordinator = ((rawData[pointer] >> 6) & 0x01) == 0x01;
        this.ssAssociationPermit = ((rawData[pointer] >> 7) & 0x01) == 0x01;
        pointer++;
    }
    
    
    
    public String toString()
    {
        String rtn = "{\n";
        
        rtn += "\t\tBeacon Order = " + this.ssBeaconOrder + ",\n";
        rtn += "\t\tSuperframe Order = " + this.ssSuperframeOrder + ",\n";
        rtn += "\t\tFinal CAP Slot = " + this.ssFinalCAPslot + ",\n";
        rtn += "\t\tBattery Life Extension = " +ssBatteryLifeExtension + ",\n";
        rtn += "\t\tPAN Coordinator = " +ssPANcoordinator + ",\n";
        rtn += "\t\tAssociation Permit = " +ssAssociationPermit + "\n";
        
        rtn += "\t}";                
                
        return rtn;
    }
}
