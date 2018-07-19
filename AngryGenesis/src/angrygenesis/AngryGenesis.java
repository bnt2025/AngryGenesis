package angrygenesis;

import angrygenesis.mac.MACFrame;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author dools
 */
public class AngryGenesis implements Runnable
{
    private static final String SNIFFER_CMD_LINE = "./zigbee_sniffer/startsniff.sh";
    
    
    private final Process snifferProcess;
    
    private final PrintStream log;
    

    public AngryGenesis(Process snifferProcess) throws IOException
    {
        this.snifferProcess = snifferProcess;
        
        LocalDateTime timePoint = LocalDateTime.now();     // The current date and time
        String logFileName = String.format("%d%d%02d-%02d%02d-log.jsonl", timePoint.getYear(), timePoint.getMonth().getValue(),
                timePoint.getDayOfMonth(), timePoint.getHour(), timePoint.getMinute());
        
        File logDataDir = new File("logdata");
        File logFile = new File(logDataDir, logFileName);
        
        this.log = new PrintStream(new FileOutputStream(logFile));
        
    }
    
    public static int[] hexStringToByteArray(String s)
    {
        int len = s.length();
        int[] data = new int[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (int) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    private void processSnifferLine(String line) throws Exception
    {
        // construct JSON object from string
        JSONObject json = new JSONObject(line);
        
        // extract raw frame data
        String rawFrameData = (String)json.remove("frame");
        
        
        // decode MAC frame from raw data
        MACFrame frame = new MACFrame(hexStringToByteArray(rawFrameData));
        
        switch(frame.getFrameControl().getFrameType())
        {    
            case FRAME_TYPE_DATA:
                System.out.println("Data frame Rx");
                
                System.out.println("    Seq Num="+frame.getSeqNum());
                System.out.printf("    SrcPAN= 0x%04X\n",frame.getSrcPAN());
                System.out.println("    SrcAddr="+frame.getSrcAddress());
                System.out.printf("    DestPAN= 0x%04X\n", frame.getDestPAN());
                System.out.println("    DestAddr="+frame.getDestAddress());
                
                json.put("tx_id", frame.getSrcAddress());
                json.put("rx_id", frame.getDestAddress());
                json.put("networkid", String.format("0x%04X",frame.getSrcPAN()));

                log.println(json.toString());
                                
                break;
                
            case FRAME_TYPE_MAC_CMD:
                System.out.println("MAC Cmd frame Rx: "+frame.getPayload());
                break;
                
            case FRAME_TYPE_BEACON:
                System.out.println("Beacon frame Rx");
                
                break;
                
            case FRAME_TYPE_ACK:
                System.out.println("ACK frame Rx");
                break;
                
            default:
                // dont care
        }
        
//        System.out.println(p);
    }
    
    
    @Override
    public void run()
    {
        // get input stream
        InputStreamReader in = new InputStreamReader(snifferProcess.getInputStream());
        BufferedReader snifferOut = new BufferedReader(in);
        
        // get error stream
        in = new InputStreamReader(snifferProcess.getErrorStream());
        BufferedReader snifferErr = new BufferedReader(in);

        // read sniffer output and process
        String line;
        while(snifferProcess.isAlive())
        {
            try
            {
                // process line output
                if(snifferOut.ready())
                {
                    line = snifferOut.readLine();
                    //System.out.println("SNIFFER SAYS: "+line);
                    processSnifferLine(line);
                }
                
                // process error output
                if(snifferErr.ready())
                {
                    line = snifferErr.readLine();
                    System.out.println("SNIFFER ERR SAYS: "+line);
                }
                
                Thread.sleep(500);
            }
            catch(Exception e)
            {
                //e.printStackTrace();
            }
        }
        
    }
    
    private static AngryGenesis startSnifferProcess() throws Exception
    {
        // build the process
        ProcessBuilder pb = new ProcessBuilder(SNIFFER_CMD_LINE);
        Map<String, String> env = pb.environment();
        env.put("TERM", "xterm");
        
        
        // start process and hook the error/output stream
        Process snifferProcess = pb.start();
        
        
        if(snifferProcess.isAlive())
        {
            // process is alive... so create AG object and launch new thread
            AngryGenesis ag = new AngryGenesis(snifferProcess);
            Thread th = new Thread(ag);
            th.start();
            
            return ag;
        }
        else 
        {
            throw new Exception("Sniffer process did not start!");
        }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            // spawn sniffer process
            AngryGenesis ag = startSnifferProcess();
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }

    
    
}
