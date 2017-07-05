import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class Database extends Thread {
	
	public static Map<String, Wrapper_database> get_database = new HashMap<String, Wrapper_database>();
	public boolean isUpThreadRun, isPutThreadRun;
	
	
	public boolean database(Object tag, Wrapper_database data) throws Exception {
		String time = data.time;
		String reader = data.reader;
		String ip = "http://192.168.1.198:7777";
		
		JSONObject jo = new JSONObject();
		jo.put("Tag_id", tag.toString());
		jo.put("time", time);
		jo.put("Reader", reader);
		JSONArray ja = new JSONArray();
		ja.put(jo);
		try {
			URL url = new URL(ip);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setReadTimeout(10000);
			con.setConnectTimeout(10000);
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(ja.toString());
			wr.flush();
			InputStream is = con.getInputStream();
			if(is==null)
				return false;
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null)
				System.out.println(line);
			rd.close();
			wr.close();
			con.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void put_data(String data, String time, String reader) {
		long start = System.nanoTime();
		isPutThreadRun = true;
		get_database.put(data, new Wrapper_database(time, reader));
		isPutThreadRun = false;
		long t = (System.nanoTime() - start) / 1000;
		if (t > maxTime) {
			maxTime = t;
		}
		System.out.println("Database send " + data + " : " + String.valueOf(maxTime));
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	private long maxTime = 0;
	public void updata_database() throws Exception {
		Runnable runnable = new Runnable () {
			@Override
			public void run() {
				isUpThreadRun = true;
				try {
					int len = get_database.keySet().toArray().length;
					int index = 0;
					for (int i = 0; i < len; i++) {
						long start = System.nanoTime();
						Object key = get_database.keySet().toArray()[index];
						Wrapper_database get_data = get_database.get(key);
						boolean isSend = database(key, get_data);
						if (isSend) {
							get_database.remove(key);
						} else {
							index++;
						}
						long t = (System.nanoTime() - start) / 1000;
						if (t > maxTime) {
							maxTime = t;
						}
//						System.out.println("Database send " + key.toString() + " : " + String.valueOf(maxTime));
						Thread.sleep(100);
					}
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
}
