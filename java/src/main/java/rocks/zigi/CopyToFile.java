package rocks.zigi;

import java.io.*;
import java.util.*;
import com.ibm.jzos.*;

public class CopyToFile 
{
    public static void main(String[] args) throws IOException 
    {
    	List<CopyPair> todo = new ArrayList<>();
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	String filename;
    	while ((filename = br.readLine()) != null)
    	{
    		filename = filename.trim();
    		int firstSpace = filename.indexOf(" ");
    		String dsname = filename.substring(0, firstSpace);
    		String omvsFile = filename.substring(firstSpace+1).trim();
    		todo.add(new CopyPair(dsname, omvsFile));
    	}
    	
    	for (CopyPair entry : todo)
    	{
    		copy(entry.dataset, entry.omvsfile);
    	}
	}

	private static void copy(String inputName, String outputName)
			throws IOException 
	{
		final int RDWLENGTH = 4; 
    	RecordReader input = RecordReader.newReader("//'" + inputName + "'", RecordReader.FLAG_DISP_SHR);
    	OutputStream output = new FileOutputStream(outputName);
    	RDWOutputRecordStream rdwoutstream = null;
    	
    	boolean recfmV = 
        		(input.getRecfmBits() & RecordReader.RECFM_V) == RecordReader.RECFM_V;
    	
    	if (recfmV)
    	{
    		rdwoutstream = new RDWOutputRecordStream(new BufferedOutputStream(output));
    	}
    	
    	byte[] buffer = new byte[input.getLrecl()];         
    	int bytesRead = 0;
    	int bytesIn = 0;
    	int recordCount = 0;
    	try
    	{
        while ((bytesRead = input.read(buffer)) >= 0) 
        {
            recordCount++;
            bytesIn += recfmV ? bytesRead + RDWLENGTH : bytesRead;
            if (recfmV)
            {
                rdwoutstream.write(buffer, 0, bytesRead);
            }
            else
            {
            	output.write(buffer, 0, bytesRead);
            }
        }
    	}
        finally 
        {
        	if (input != null) 
        	{
        		input.close();   
        	}
        	if (rdwoutstream != null) 
        	{
        		rdwoutstream.close();  
        	}
        	else if (output != null) 
        	{
        		output.close();   
        	}
        }
        
        System.out.format("Copied %s to %s. %,d records, %,d bytes.%n", inputName, outputName, recordCount, bytesIn);
	}
	
	static class CopyPair
	{
		public CopyPair(String dataset, String omvsfile)
		{
			this.dataset = dataset;
			this.omvsfile = omvsfile;
		}
		
		String omvsfile;
		String dataset;
	}
}
