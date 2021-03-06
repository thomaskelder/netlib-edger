package org.networklibrary.edger.parsing.StringStitch;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.networklibrary.core.parsing.FileBasedParser;
import org.networklibrary.core.parsing.ParsingErrorException;
import org.networklibrary.core.types.EdgeData;

public class StitchChemChemParser extends FileBasedParser<EdgeData> {

	public static String EDGE_TYPE = "cci";
	public static String SOURCE_NAME = "stitch_chem_chem";
	
	private List<String> columns = null;
	
	public StitchChemChemParser(){
	}
	
	public Collection<EdgeData> parse() throws ParsingErrorException {
		String line = readLine();
		List<EdgeData> res = null;
		
		if(!line.isEmpty()){
			res = new LinkedList<EdgeData>();
			
			String[] values = line.split("\\t",-1);
			
			if(values.length != columns.size()){
				throw new IllegalArgumentException("number of elements in row does not match number of columns " + line);
			}
			
			Map<String,Object> props = new HashMap<String,Object>();
			for(int i = 2; i < values.length; ++i){
				if(!values[i].isEmpty()){
					props.put(columns.get(i), Integer.valueOf(values[i]));
				}
			}
			props.put("data_source",SOURCE_NAME);
			String from = values[0];
			String to = values[1];
			
			res.add(new EdgeData(from,to,EDGE_TYPE,props));
			
		}
		
		return res;
	}

	protected void parseHeader(String header) {
		columns = Arrays.asList(header.split("\\t",-1));
	}

	protected boolean hasHeader() {
		return true;
	}

	@Override
	public boolean hasExtraParameters() {
		return false;
	}
	
	@Override
	public void takeExtraParameters(List<String> extras) {
	}

}
