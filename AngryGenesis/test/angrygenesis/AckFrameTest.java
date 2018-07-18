package angrygenesis;

import angrygenesis.mac.MACFrame;
import angrygenesis.*;


/**
 *
 * @author dools
 */
public class AckFrameTest
{
    
    private static final int[] exampleAckFrame = {0x02, 0x00, 0x40};
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            MACFrame p = new MACFrame(exampleAckFrame);
            System.out.println(p);
            
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
}
