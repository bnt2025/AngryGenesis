package angrygenesis.mac;



/**
 *
 * @author dools
 */
public enum AddressingMode
{
    
    NONE(0x00),         //PAN and address not present
    SHORT(0x02),        //Short address (16-bit)
    EXTENDED(0x03);     //Extended address (64-bit)
      
    
    private final int value;
    
    private AddressingMode(int val)
    {
        this.value = val;
    }
    
    
    public static AddressingMode getAddressingMode(int value)
    {
        
        // 3-bit frame-type
        for(AddressingMode am: AddressingMode.values())
        {
            if(value == am.value) return am;
        }
        
        return null;
    }
    
}
