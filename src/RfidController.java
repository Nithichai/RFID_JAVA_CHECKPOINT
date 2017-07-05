import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.jlrfid.service.AntStruct;
import com.jlrfid.service.GetReadData;
import com.jlrfid.service.MainHandler;
import com.jlrfid.service.RFIDException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RfidController implements GetReadData {
	
	private String ip;
	private int baudrate, port;
	private boolean isConnect;
	private boolean isLoop;
//	private boolean isStart;
	private byte[] antEnable = new byte[4];
	private String r2KPath = System.getProperty("user.dir") + "\\lib\\R2k.dll";
	
	private static String reader;
	
	public static Database database = new Database();
	public static Map<String, Wrapper> all_data = new HashMap<String, Wrapper>();

	public boolean connectRFID(String ip, int baudrate, int port) {
		MainHandler handler = new MainHandler();
		if(handler.dllInit(r2KPath)){
			if(handler.deviceInit(ip, baudrate, port)){
				this.ip = ip;
				this.baudrate = baudrate;
				this.port = port;
				this.isConnect = true;
				return true;
			}
		}
		return false;
	}
	
	public void disconnect() {
		MainHandler handler = new MainHandler();
		if(handler.dllInit(r2KPath)){
			if(handler.deviceInit(ip, baudrate, port)){
				handler.StopInv();
			}
		}
	}
	
	public boolean getAntenna(int index) throws RFIDException{
		MainHandler handler = new MainHandler();
		if(handler.dllInit(r2KPath)){
			if(handler.deviceInit(ip, baudrate, port)){
				AntStruct struct = handler.GetAnt();
				for(int i=0; i<4; i++){
					System.out.println(
							"antenna" + (i+1) 
							+ (struct.antEnable[i]==1 ? "connected":"disconnected") 
							+ "work time:" + struct.dwellTime[i] 
							+ "power:" + struct.power[i].longValue()/10 +"dBm"
					);
				}
			}
			return false;
		}
		return true;
	}
	
	public boolean setAntenna(boolean isSelected, int index) throws RFIDException {
		MainHandler handler = new MainHandler();
		if(handler.dllInit(r2KPath)){
			if(handler.deviceInit(ip, baudrate, port)){
				if (isSelected)
					antEnable[index] = 1;
				else
					antEnable[index] = 0;
				long[] dwellTime = new long[] {20, 20, 20, 20};
				long[] power = new long[] {300, 300, 300, 300};
				if(handler.SetAnt(antEnable,dwellTime,power)){
					System.out.println("succeed to set antenna parameter");
				}else{
					System.out.println("failed to set antenna parameter");
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean setStart(boolean isSelected) throws RFIDException {
		MainHandler handler = new MainHandler();
		if(handler.dllInit(r2KPath)){
			if(handler.deviceInit(ip, baudrate, port)){
				if (isSelected) {
					if (!isLoop) {
						handler.InvOnce(new RfidController());
					} else {
						handler.BeginInv(new RfidController());
					}
				} else {
					handler.StopInv();
				}
				return true;
			}
		}
		return false;
	}
	
	private long maxTime = 0;
	private int ant;
	private long antStart = 0;
	public void getReadData(String data, int antNo) {
		if (antNo != ant) {
			System.out.println("Antenna " + ant + " time read : " + String.valueOf((System.nanoTime() - antStart)/1000));
			ant = antNo;
			antStart = System.nanoTime();
		}
		long start = System.nanoTime();
		if ("F0".equals(data)) {
			System.out.println("Antenna 1 finished inventory");
		}else if ("F1".equals(data)) {
			System.out.println("Antenna 2 finished inventory");
		}else if ("F2".equals(data)) {
			System.out.println("Antenna 3 finished inventory");
		}else if ("F3".equals(data)) {
			System.out.println("Antenna 4 finished inventory");
		}else if(!"".equals(data)){
			System.out.println(data + "  antenna" + antNo);
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
			String time = format.format(date);
			String boxSelected = FrameGUI.comboBox.getSelectedItem().toString();
			if (!boxSelected.equals("Reader1")) {
				data = data.substring(0, data.length()-8);
			}
			all_data.put(data, new Wrapper(time, reader, String.valueOf(antNo)));
			database.put_data(data, time, reader);
			long t = (System.nanoTime() - start) / 1000;
			if (t > maxTime) {
				maxTime = t;
//				System.out.println("Antenna read " + data + " : " + String.valueOf(t));
			}
//			System.out.println("Antenna read " + data + " : " + String.valueOf(maxTime));
		}
		
	}
	
	public void convertJson() throws IOException, JSONException{
		JSONArray ja = new JSONArray();
		for (int i = 0; i < all_data.keySet().size(); i++){
			JSONObject jo = new JSONObject();
			Object key = all_data.keySet().toArray()[i];
			Wrapper wrapper = all_data.get(key);
			String time = wrapper.time;
			jo.put("Tag_id", key);
			jo.put("time", time);
			jo.put("Reader", reader);
			ja.put(jo);
		}
		try (FileWriter file = new FileWriter("logger.json")) {
			file.write(ja.toString());
		}
	}
	
	public String[][] getTable() {
		int tableColSize = 5;
		String[][] dataList = new String[all_data.keySet().size()][tableColSize];
		for (int i = 0; i < all_data.keySet().size(); i++){
			String[] colList = new String[tableColSize];
			Object key = all_data.keySet().toArray()[i];			
			Wrapper wrapper = all_data.get(key);
			String time  = wrapper.time;
			String read = wrapper.reader;
			String antNo = wrapper.antno;
			colList[0] = String.valueOf(i+1);
			colList[1] = String.valueOf(time);
			colList[2] = String.valueOf(key);
			colList[3] = read;
			colList[4] = antNo;
			dataList[i] = colList;
		}
		return dataList;
	}
	
	public void resetHashMap(){
		all_data = new HashMap<String, Wrapper>();
	}
	
	public boolean isConnect() {
		return isConnect;
	}
	
	public boolean isLoop() {
		return isLoop;
	}
	
//	public boolean isStart() {
//		return isStart;
//	}
	
	public String getReader() {
		return reader;
	}
	
	public void setReader(String reader) {
		RfidController.reader = reader;
	}
	
	public void setOnceLoop(boolean isSelected) {
		isLoop = isSelected;
	}
	
}
