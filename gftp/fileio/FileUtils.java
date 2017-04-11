package gftp.fileio;

import java.io.File;
import java.util.HashMap;

import gcore.tuples.Pair;
import gcore.util.ArrayUtils;


public class FileUtils {
	private static HashMap<File, Long> fileSizeMap = new HashMap<>();
	private static HashMap<File, String[]> fileWalkMap = new HashMap<>();
	
	
	public static long getFileSize(File root){
		if (fileSizeMap.containsKey(root)){
			return fileSizeMap.get(root);
		}else{
			walkFile(root);
		}
		return fileSizeMap.get(root);
	}
	
	public static String[] generateFileWalk(File root) {
		if (fileWalkMap.containsKey(root)){
			return fileWalkMap.get(root);
		}else{
			walkFile(root);
		}
		return fileWalkMap.get(root);
	}
	
	public static void resetFileWalks(){
		fileSizeMap.clear();
		fileWalkMap.clear();
	}
	
	private static void walkFile(File root){
		Pair<String[], Long> results = generateFileWalk(root, true);
		fileSizeMap.put(root, results.getSecond());
		fileWalkMap.put(root, results.getFirst());
	}
	private static Pair<String[], Long> generateFileWalk(File root, boolean isParent){
		long size = 0;
		if (root.isHidden() && !isParent) return new Pair<>(new String[] {}, 0L);
		String[] results = new String[1];
		results[0] = root.getAbsolutePath();
		String[] list = root.list();
		if (list == null) {
			if (root.isFile()) {
				size = root.length();
			}
			return new Pair<>(new String[]{root.getAbsolutePath()}, size);
		}
		for (String item : list) {
			Pair<String[], Long> walk = generateFileWalk(new File(root, item), false);
			size += walk.getSecond();
			results = ArrayUtils.concat(results, walk.getFirst());
		}
		return new Pair<>(results, size);
	}
	
	
	
}
