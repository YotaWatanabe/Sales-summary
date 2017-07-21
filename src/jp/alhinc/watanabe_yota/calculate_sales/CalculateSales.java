package jp.alhinc.watanabe_yota.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class CalculateSales {
	public static void main(String[] args) {
		BufferedReader br = null;
		HashMap<String, String> branchMap = new HashMap<String, String>();
		HashMap<String, Long> branchSaleMap = new HashMap<String,Long>();
		HashMap<String, String> commodityMap = new HashMap<String, String>();
		HashMap<String, Long> commoditySaleMap = new HashMap<String,Long>();

		if(args.length !=1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		if(!bcRead(args[0],"branch.lst","支店","\\d{3}",branchMap,branchSaleMap)){
			return;
		}
		if(!bcRead(args[0],"commodity.lst","商品","\\w{8}",commodityMap,commoditySaleMap)){
			return;
		}

		ArrayList<File> rcdPath = new ArrayList<File>();
		File file = new File(args[0]);
		File[] files=file.listFiles();
		for (int i=0; i <files.length;i++){
			String rcdFile =files[i].getName();
			if (rcdFile.matches("\\d{1,8}.rcd") && files[i].isFile()){
				rcdPath.add(files[i]);
			}
		}
		//連番
		for(int i=0; i <rcdPath.size()-1;i++){
			String file2 =rcdPath.get(i).getName();
			String file3 =rcdPath.get(i+1).getName();
			int a =Integer.parseInt(file2.split("\\.")[0]);
			int b =Integer.parseInt(file3.split("\\.")[0]);
			if(a+1 != b){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}
		try {
			for (int i=0; i <rcdPath.size();i++){
				br = new BufferedReader(new FileReader(rcdPath.get(i)));
				ArrayList<String> totalCode = new ArrayList<String>();
				String s;
				while((s = br.readLine()) !=null){
					totalCode.add(s);
				}
				if( !branchSaleMap.containsKey(totalCode.get(0)) ){
					System.out.println(rcdPath.get(i).getName()+"の支店コードが不正です");
					return;
				}
				if(totalCode.size() != 3){
					System.out.println(rcdPath.get(i).getName()+"のフォーマットが不正です");
					return;
				}
				if( !commoditySaleMap.containsKey(totalCode.get(1)) ){
					System.out.println(rcdPath.get(i).getName()+"の商品コードが不正です");
					return;
				}
				Long y = branchSaleMap.get(totalCode.get(0));
				if(totalCode.get(2).matches("\\d{0,10}")){
					y +=Long.parseLong(totalCode.get(2));
				}else{
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
				if(y.toString().length() > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				branchSaleMap.put(totalCode.get(0),y);
				Long t = commoditySaleMap.get(totalCode.get(1));
				t +=Long.parseLong(totalCode.get(2));
				commoditySaleMap.put(totalCode.get(1),t);
				if(t.toString().length() > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			}
		}
		catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}
		if(!fileOut(args[0],branchMap,branchSaleMap,"branch.out")){
			return;
		}
		if(!fileOut(args[0],commodityMap,commoditySaleMap,"commodity.out")){
			return;
		}
	}

	public static boolean fileOut(String dirPath,
			HashMap<String, String> name,HashMap<String, Long> sale,String fileName){
		BufferedWriter bw = null;
		List<Map.Entry<String,Long>> entries =
				new ArrayList<Map.Entry<String,Long>>(sale.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {

			@Override
			public int compare(
					Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});

		try {
			File file1 =new File(dirPath,fileName);
			FileWriter fw =new FileWriter(file1);
			bw =new BufferedWriter(fw);

			for (Entry<String,Long> s : entries) {
				bw.write(s.getKey()+","+name.get(s.getKey())+","+s.getValue());
				bw.newLine();
			}
		}
		catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}
		finally {
			if (bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}

	public static boolean bcRead(String dirPath,String fileName,String brco,String regularExpression,
			HashMap<String, String> name,HashMap<String, Long> sale){
		BufferedReader br = null;
		try {
			File file = new File(dirPath,fileName);
			if(!file.exists()){
				System.out.println(brco+"定義ファイルが存在しません");
				return false;
			}
			FileReader fr = new FileReader(file);

			br = new BufferedReader(fr);
			String s;
			while ((s = br.readLine()) != null) {
				String[] rcdname = s.split(",");
				if (! rcdname[0].matches(regularExpression) || rcdname.length != 2) {
					System.out.println(brco + "定義ファイルのフォーマットが不正です");
					return false;
				}
				name.put(rcdname[0], rcdname[1]);
				sale.put(rcdname[0], 0L);
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しまし");
			return false;
		} finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}
}