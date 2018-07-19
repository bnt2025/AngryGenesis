package angrygenesis;

import angrygenesis.mac.MACFrame;
import angrygenesis.*;


/**
 *
 * @author dools
 */
public class BeaconReqTest
{
    
    private static final int[] beaconData = {0x03, 0x08, 0x5a, 0xff, 0xff, 0xff, 0xff, 0x07};
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            MACFrame p = new MACFrame(beaconData);
            System.out.println(p);
            
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
}
