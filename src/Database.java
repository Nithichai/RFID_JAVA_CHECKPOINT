import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Database{
	
	public static Map<String, Wrapper_database> get_database = new HashMap<String, Wrapper_database>();
	public boolean isUpThreadRun, isPutThreadRun, isPrintQueue;
	public static String status_server = "Wait For Connection";
	
	static JSONArray eventList;
	private static String event;
	public String[] eventIDList;
	
	public String[] get_event(){	
		String mainIP = String.valueOf(FrameGUI.serverTextField.getText());
		try {
			String ip = mainIP + "/select_event";
			URL url = new URL(ip);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			if (con.getInputStream() == null) {
				return new String[0];
			}
			BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			StringBuffer response = new StringBuffer();
			String inputLine;
			while ((inputLine = rd.readLine()) != null)
				response.append(inputLine);
			rd.close();
//			con.disconnect();
			eventList = new JSONArray(response.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return new String[0];
		} catch (IOException e) {
			e.printStackTrace();
			return new String[0];
		} catch (JSONException e) {
			e.printStackTrace();
			return new String[0];
		}
		eventIDList = new String[eventList.length()];
		String[] eventTable = new String[eventList.length()];
		try {
			if (eventList.length() > 0) {
				for (int i = 0; i < eventTable.length; i++) {
					JSONObject obj = new JSONObject(eventList.get(i).toString());
					eventTable[i] = obj.getString("event_name");
					eventIDList[i] = obj.get("event_id").toString();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return eventTable;
	}
	
	public boolean ntp_time(String gun_time, int type_runner) throws Exception{
		String ip = String.valueOf(FrameGUI.serverTextField.getText());
		JSONObject jo = new JSONObject();
		jo.put("running_cat_id", type_runner);
		jo.put("gun_time", gun_time);
		jo.put("event_id", event);
		JSONArray ja = new JSONArray();
		ja.put(jo);
		
		URL url = new URL(ip + "/set_gun_time");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		try {
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setReadTimeout(10000);
			con.setConnectTimeout(10000);
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(jo.toString());
			wr.flush();
			int statusCode = con.getResponseCode();
			if (statusCode != 200) {
				status_server = ("Error" + String.valueOf(statusCode));
				return false;
			}
			InputStream is = con.getInputStream();
			if(is==null)
				return false;
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null)
			    status_server = (line);
			System.out.println(status_server);
			rd.close();
			wr.close();
		} catch (SocketTimeoutException e) {
			int statusCode = con.getResponseCode();
			status_server = ("Error" + String.valueOf(statusCode));
			System.out.println(status_server);
			return false;
		}
		return true;
	}
	public boolean database(Object tag, Wrapper_database data) throws Exception{		
		System.out.println("Total queue : " + String.valueOf(get_database.size()));		
		String time = data.time;
		String reader = data.reader;
		String ip = String.valueOf(FrameGUI.serverTextField.getText());
		JSONObject jo = new JSONObject();
		jo.put("Tag_id", tag.toString());
		jo.put("last_time", time);
		jo.put("Reader", reader);
		JSONArray ja = new JSONArray();
		ja.put(jo);
		URL url = new URL(ip);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		try {
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setReadTimeout(30000);
			con.setConnectTimeout(30000);
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(ja.toString());
			wr.flush();
			int statusCode = con.getResponseCode();
			if (statusCode != 200 && statusCode != 201) {
				status_server = ("Error " + String.valueOf(statusCode));
				System.out.println(status_server);
				return false;
			}
			if (statusCode == 201){
				status_server = ("Created Success " + String.valueOf(statusCode));
			}
			InputStream is = con.getInputStream();
			if(is==null)
				return false;
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null)
			    status_server = (line);
			rd.close();
			wr.close();
		} catch (SocketTimeoutException e) {
			int statusCode = con.getResponseCode();
			status_server = ("Error" + String.valueOf(statusCode));
			return false;
		}
		return true;
	}
	
	public void put_data(String data, String time, String reader) {
//		isPutThreadRun = true;
		get_database.put(data, new Wrapper_database(time, reader));
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		isPutThreadRun = false;
	}
	
	public void updata_database() throws Exception {
		Runnable runnable = new Runnable () {
			@Override
			public void run() {
				isUpThreadRun = true;
				try {
					int len = get_database.keySet().toArray().length;
					int index = 0;
					for (int i = 0; i < len; i++) {
						Object key = get_database.keySet().toArray()[index];
						Wrapper_database get_data = get_database.get(key);
						boolean isSend = database(key, get_data);
						if (isSend) {
							get_database.remove(key);
						} else {
							index++;
						}
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				isUpThreadRun = false;
			}
		};
		new Thread(runnable).start();
	}
		
	public void setEvent(String event) {
		Database.event = event;
	}
}

