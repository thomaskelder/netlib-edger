package org.networklibrary.edger.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.networklibrary.core.parsing.FileBasedParser;
import org.networklibrary.core.parsing.ParsingErrorException;
import org.networklibrary.core.types.EdgeData;

public class GmtParser extends FileBasedParser<EdgeData> {
	protected static final Logger log = Logger.getLogger(GmtParser.class.getName());
	
	protected String source = "unknown";
	protected String type = "gmt_association";
	protected int idcol = 0;
	protected String idprefix = ""; // a fix for reactome
	
	@Override
	public Collection<EdgeData> parse() throws ParsingErrorException {
		
		String line = readLine();
		List<EdgeData> result = null;
		
		if(line != null && !line.isEmpty()){
			
			result = new ArrayList<EdgeData>();
			
			String[] values = line.split("\\t",-1);
			
			if(values.length < 3)
				throw new ParsingErrorException("Pathway should at least contain 3 elements!");
			
			String[] col0 = values[idcol].split("%", -1);
			
			String id = idprefix + col0[col0.length-1];
			
			Map<String,Object> props = new HashMap<String,Object>();
			props.put("data_source", source);	
			
			for(int i = 2; i < values.length; ++i){
				String gene = values[i];
				
				result.add(new EdgeData(gene,id,type,props));
			}
			
		}
		return result;
	}

	@Override
	public boolean hasExtraParameters() {
		return true;
	}

	@Override
	public void takeExtraParameters(List<String> extras) {	
		if(extras != null){
			log.info("processing extra parameters: " + extras.toString());

			for(String extra : extras){
				String values[] = extra.split("=",-1);

				switch(values[0]) {
				case "source":
					source = values[1];
					break;
					
				case "type":
					type = values[1];
					break;
					
				case "idcol":
					idcol = Integer.valueOf(values[1]);
					break;
					
				case "idprefix":
					idprefix = values[1];
					break;
				}
			}

			log.info("using source =" + source);
		}
	}

	@Override
	protected boolean hasHeader() {
		return false;
	}

	@Override
	protected void parseHeader(String header) {
	}

}
