package angrygenesis;

import angrygenesis.mac.MACFrame;
import angrygenesis.*;


/**
 *
 * @author dools
 */
public class BeaconFrameTest
{
    
    
    private static final int[] exampleBeaconFrame = { 0x00, 0x80, 0x4c, 0xdd, 0x1c, 0x00, 0x00, 0xff, 0xcf, 0x00,
            0x00, 0x00, 0x22, 0x84, 0xd1, 0x83, 0x9b, 0xb7, 0xf2, 0xf2, 0x9f, 0x85, 0xff, 0xff, 0xff, 0x00, 0xc4, 0xd6 };
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            MACFrame p = new MACFrame(exampleBeaconFrame);
            System.out.println(p);
            
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
}
