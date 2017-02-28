package fileHandle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.mathworks.toolbox.javabuilder.MWException;

public class SplitFile {
	
	/* 
	 * ����measdatainfo.txt�ļ�����measdata.txt�ָ��ͬ���ļ���
	 */
	public static void SplitFileToDifferentDir() throws IOException, MWException
	{
		File fileReadInfo = new File("src/srcdata/measdatainfo.txt");
		File fileReadData = new File("src/srcdata/measdata.txt");
		File fileWrite;
		// �����ļ�������
		if(fileReadInfo.exists() && fileReadData.exists()){
			// ��ȡinfo��ֵ
			BufferedReader inInfo = new BufferedReader(new FileReader(fileReadInfo));
			BufferedReader inData = new BufferedReader(new FileReader(fileReadData));
			String lineInfo = inInfo.readLine();
			while(lineInfo != null) // �ܴ���Ϣ�ļ��ж�ȡ����
			{
				// �ö��ŷָ��ַ���
				// 1,162,128,11,1,2016-06-20 15:32:21,1,5890
				// 0  1   2   3 4          5          6  7 
				// 3 ��ҽ��id��4�ǲ���id
				lineInfo =  lineInfo.replaceAll(":", "~");
 				String[] arrInfoSplit = lineInfo.split(",");
 				String fileName = arrInfoSplit[3]+","+arrInfoSplit[4]+","+arrInfoSplit[5]+".txt";
				switch(arrInfoSplit[1]) { // ���ݱ�ʶ������д����ļ�
				case "161":fileWrite = new File("src/srcdata/Tempr/" + fileName); break;// ����
				case "162":fileWrite = new File("src/srcdata/ECG/" + fileName); break;// �ĵ�
				case "163":fileWrite = new File("src/srcdata/BP/" + fileName); break;// Ѫѹ
				case "166":fileWrite = new File("src/srcdata/SpO2/"+ "bf_handle_"+fileName); break;// Ѫ��
				default:lineInfo = inInfo.readLine();continue;	// ֱ�ӽ����´ζ�ȡ
				}
				long startLine = Long.parseLong(arrInfoSplit[6])-1;
				long endLine = Long.parseLong(arrInfoSplit[7])+startLine;
				// �ж�Ŀ¼�Ƿ���ڣ��������򴴽�
				if(!fileWrite.getParentFile().exists())
					fileWrite.getParentFile().mkdirs();
				// д������
				FileOutputStream fos =  new FileOutputStream(fileWrite); 
 				for(long i = startLine; i < endLine; ++i)
 				{
 					String lineData = inData.readLine();
 					 // д���ļ�
	       			fos.write(lineData.getBytes());
	       			fos.write("\r\n".getBytes());
 				}
       			fos.close();
       			
       			if(arrInfoSplit[1].equals("166")) // �����Ѫ������һ������
       			{
       				SpO2ChangeDataOrder(fileWrite,fileName);
       				//ʹ���㷨�����ļ�
       				SpO2Algorithm(fileName);
       			}
       			
       			if(arrInfoSplit[1].equals("162")) // ������ĵ磬��һ������
       			{
       				
       			}
 				// ��ȡ��һ������
 				lineInfo = inInfo.readLine();
				
			}
			inInfo.close();
			inData.close();
		}		
	}
	
	/**
	 * 
	 * @param fileBfHandle
	 * @param fileName
	 * @throws IOException
	 * �����㷨���ļ��ͻ�ͼ���ļ�
	 */
	private static void SpO2ChangeDataOrder(File fileBfHandle,String fileName) throws IOException
	{
		File fileWrite_afHandle; // �㷨���ļ�
		File fileWrite_draw;     // ��ͼ���ļ�
		int i = 0;
		String[] dataTemp = new String[18];
		//��������
		BufferedReader bfDataFile = new BufferedReader(new FileReader(fileBfHandle));
		fileWrite_afHandle = new File("src/srcdata/SpO2/algorithm_" + fileName);
		fileWrite_draw = new File("src/srcdata/SpO2/" + fileName);
		// д������
		FileOutputStream fos =  new FileOutputStream(fileWrite_afHandle);
		FileOutputStream fos_draw =  new FileOutputStream(fileWrite_draw);
		
		 // д���ļ�
		fos_draw.write("SpO2".getBytes());
		fos_draw.write("\r\n".getBytes());
		
		String bfData = bfDataFile.readLine();
		while(bfData != null)
		{
			dataTemp[i] = bfData;
			i++;
			if(i == 18) // ����һ�飬д��
			{
				i = 0;
				for(int j = 2; j < 10; ++j)
 				{
 					 // д���㷨���ļ�
	       			fos.write(dataTemp[j].getBytes());
	       			fos.write("\r\n".getBytes());
	       			fos.write(dataTemp[j+8].getBytes());
	       			fos.write("\r\n".getBytes());
	       			
					 // д�뻭ͼ���ļ�
	       			fos_draw.write(dataTemp[j].getBytes());
	       			fos_draw.write("\r\n".getBytes());
	       			fos_draw.write(dataTemp[j+8].getBytes());
	       			fos_draw.write("\r\n".getBytes());
 				}				
			}
			bfData = bfDataFile.readLine();
		}
		
		bfDataFile.close();
		fos.close();
		fos_draw.close();
		fileBfHandle.delete();
	}
	
	/**
	 * 
	 * @param fileName
	 * @throws IOException
	 * ���ô����㷨�Բɼ������ļ����д���
	 * @throws MWException 
	 */
	private static void SpO2Algorithm(String fileName) throws IOException, MWException
	{
		String fileAlgorithmPath = "src/srcdata/SpO2/algorithm_" + fileName;
		fileAlgorithmPath = fileAlgorithmPath.replace("/", "\\");
		
		SpO2parameters.Class1 c1 = new SpO2parameters.Class1();
		
		// �õ����㷨�Ľ��
		Object[] result_HRVparameters= null;
		Object[] result_SpO2= null;
		result_HRVparameters = c1.HRVparameters(11,fileAlgorithmPath);
		result_SpO2 = c1.SpO2_Val_acVdc(1,fileAlgorithmPath);
		
		
		// �����д�뵽�ļ�,�ö��Ÿ���������Ϊ
		// SpO2,HI_RATE,LO_RATE,MEAN_RATE,SDNN,R_MSSD,LF,HF,TP, lfnorm ,hfnorm,lf_hf
		File AlgorithmResult = new File("src/algorithmdata/SpO2/" + fileName);
		FileOutputStream fos =  new FileOutputStream(AlgorithmResult);
		
		fos.write(result_SpO2[0].toString().getBytes());
		fos.write(",".getBytes());
		
		
		for(int i = 0; i < 11; ++i)
		{
			fos.write(result_HRVparameters[i].toString().getBytes());
			if(i != 10)
				fos.write(",".getBytes());
		}
		
		fos.close();
		// ɾ���㷨���ļ�
		File fileAlgorithm = new File("src/srcdata/SpO2/algorithm_" + fileName);
		fileAlgorithm.delete();

	}
}
