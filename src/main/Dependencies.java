package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import visionCore.util.Lists;
import visionCore.util.Zipper;

public class Dependencies {
	
	
	public static Map<String, String[]> buildDependencyMap(File jar) {
		
		List<String> lines = new ArrayList<String>(32);
		
		try {
		
			ProcessBuilder pb = new ProcessBuilder(Main.javaHome+"/bin/jdeps", "-verbose:class", "-filter:none", "\""+jar.getAbsolutePath()+"\"");
			Process p = pb.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			for (String line = "", cur = ""; (line = reader.readLine()) != null;) {
				
				lines.add(line.trim());
			}
			
		} catch (Exception e) { e.printStackTrace(); }
		
		Map<String, String[]> map = new HashMap<String, String[]>();
		
		String lastKey = null;
		List<String> buffer = new ArrayList<String>();
		
		for (String line : lines) {
			
			if (line.contains(" java.")) { continue; }
			
			int ind = line.indexOf("->");
			int parInd = line.indexOf(" (");
			
			if (ind < 0 && parInd >= 0) {
				
				if (lastKey != null && buffer != null) {
					
					map.put(lastKey, buffer.toArray(new String[buffer.size()]));
				}
				
				lastKey = line.substring(0, parInd).trim();
				buffer.clear();
				
			} else if (ind == 0) {
				
				int start = -1, end = -1;
				
				for (int i = ind+3, len = line.length(); i < len; i++) {
					char c = line.charAt(i);
					
					if (c != ' ' && c != '\t') {
						
						if (start < 0) { start = i; }
						
					} else if (start > -1) { end = i; break; }
				}
				
				if (end == -1) { end = line.length(); }
				
				buffer.add(line.substring(start, end));
			}
		}
		
		if (lastKey != null && !buffer.isEmpty()) {
			
			map.put(lastKey, buffer.toArray(new String[buffer.size()]));
		}
		
		return map;
	}
	
	
	public static void removeRedundantClasses(File dir, Set<String> dependencies) {
		
		List<File> fl = Lists.asArrayList(dir.listFiles());
		
		for (int i = 0; i < fl.size(); i++) {
			File f = fl.get(i);
			
			if (f.getName().toLowerCase().endsWith(".class")) {
				
				String className = f.getAbsolutePath().substring(dir.getAbsolutePath().length()+1).replace('\\', '/').replace('/', '.');
				className = className.substring(0, className.lastIndexOf('.'));
				
				if (!dependencies.contains(className)) {
					
					f.delete();
				}
				
			} else if (f.isDirectory()) {
				
				Lists.addAll(fl, i+1, f.listFiles());
			}
		}
	}
	
}
