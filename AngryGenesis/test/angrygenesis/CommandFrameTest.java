package angrygenesis;

import angrygenesis.mac.MACFrame;
import angrygenesis.*;


/**
 *
 * @author dools
 */
public class CommandFrameTest
{
    
    
    private static final int[] exampleCommandFrame = {0x63, 0xcc, 0x4b, 0xdd, 0x1c, 0xc1, 0xe9, 0x1f, 0x00, 0x00, 0xff, 0x0f, 0x00, 0xdf, 0x1b, 0x1b,
   0x00, 0x00, 0xff, 0x0f, 0x00, 0x02, 0x6a, 0x6a, 0x00, 0xe0, 0x7c};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            MACFrame p = new MACFrame(exampleCommandFrame);
            System.out.println(p);
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
}
