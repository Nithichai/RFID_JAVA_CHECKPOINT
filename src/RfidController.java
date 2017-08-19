import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.jlrfid.service.GetReadData;
import com.jlrfid.service.MainHandler;
import com.jlrfid.service.RFIDException;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RfidController implements GetReadData {
	
	private String ip;
	private int baudrate, port;
	private boolean isConnect;
	private boolean isLoop;
	private byte[] antEnable = new byte[4];
	private String r2KPath = System.getProperty("user.dir") + "\\R2k.dll";
//	private String r2KPath = "R2k.dll";
	
	private static String reader;
	public static Database database = new Database();
	public static Map<String, Wrapper> all_data = new HashMap<String, Wrapper>();
	public boolean isThreadNTP;
	public static String ntpTime;

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
	
	public void disconnect() throws RFIDException {
		MainHandler handler = new MainHandler();
		handler.StopInv();
		if(handler.dllInit(r2KPath)){
			if(handler.deviceInit(ip, baudrate, port)){
				for(int index = 0; index < 4; index++){
					if(antEnable[index]==1){
						setAntenna(false, index);
					}
				}
				handler.deviceDisconnect();
			}
		}
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
				long[] power = new long[] {330, 330, 330, 330};
				handler.SetAnt(antEnable,dwellTime,power);
				return true;
			}
		}
		return false;
	}
	
	public boolean setStart(boolean isSelected) throws RFIDException, Exception {
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

	public void set_guntime(int type) throws Exception{
		database.ntp_time(ntpTime, type);	
	}
	
	public void getReadData(String data, int antNo){
		if(!"".equals(data) && !data.startsWith("F")){
			String boxSelected = FrameGUI.comboBox.getSelectedItem().toString();
			if (!boxSelected.equals("Reader1") && data.length() > 30) {
				data = data.substring(0, data.length()-14);
			}else{
				data = data.substring(0, data.length()-6);
			}
			all_data.put(data, new Wrapper(ntpTime, reader, String.valueOf(antNo)));
			database.put_data(data, ntpTime, reader);
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
		String[][] dataList = new String[Database.get_database.size()][tableColSize];
		for (int i = 0; i < Database.get_database.size(); i++){
			String[] colList = new String[tableColSize];
			Object key = Database.get_database.keySet().toArray()[i];			
			Wrapper_database wrapper_database = Database.get_database.get(key);
			Wrapper wrapper = all_data.get(key);
			String time  = wrapper_database.time;
			String read = wrapper_database.reader;
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
	
	public void get_time() {
		Runnable runnable = new Runnable () {
			@Override
			public void run() {
				isThreadNTP = true;
				try {
					String TIME_SERVER = "time.navy.mi.th";   
					NTPUDPClient timeClient = new NTPUDPClient();
					InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
					TimeInfo timeInfo = timeClient.getTime(inetAddress);
					long returnTime = timeInfo.getReturnTime();
					SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
					ntpTime = format.format(returnTime);
					System.out.println(ntpTime);
					Thread.sleep(800);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				isThreadNTP = false;
			}
		};
		new Thread(runnable).start();
	}
	
	public boolean isConnect() {
		return isConnect;
	}
	
	public boolean isLoop() {
		return isLoop;
	}
	
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
