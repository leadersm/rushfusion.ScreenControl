package com.rushfusion.screencontroll;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rushfusion.screencontroll.bean.STB;
import com.rushfusion.screencontroll.util.MscpDataParser;
import com.rushfusion.screencontroll.util.XmlUtil;

public class ScreenControlActivity extends Activity {
	/** Called when the activity is first created. */
	TextView mIp;
	Button searchBtn,clearBtn;
	ListView lv;
	ProgressBar progress;
	LayoutInflater inflater;
	
	InetAddress stbIp;
	List<STB> stbs = new ArrayList<STB>();
	BaseAdapter ba;
	Handler handler;
	DatagramSocket s = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
	}


	private void init() {
		if(checkNetworking(this)){
			try {
				s = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
			findByIds();
			Thread mReceiveThread= new Thread(updateThread);  
			mReceiveThread.start();
		}else
			showDialog(0);
	}


	/**
	 * 检查网络连接是否可用
	 * @param context
	 * @return
	 */
	public static boolean checkNetworking(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nwi = cm.getActiveNetworkInfo();
		if(nwi!=null){
			return nwi.isAvailable();
		}
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("网络连接提示");
		builder.setMessage("当前没有可用网络，是否设置?") 
		.setCancelable(false) 
		.setPositiveButton("设置", new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int id) { 
				Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				startActivity(intent);
				init();
			}
		}) 
		.setNegativeButton("退出", new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int id) { 
				finish();
			} 
		}); 
		return builder.create();
	}
	
	
	private void findByIds() {
		inflater = LayoutInflater.from(this);
		mIp = (TextView) findViewById(R.id.mIp);
		searchBtn = (Button) findViewById(R.id.search);
		clearBtn = (Button) findViewById(R.id.clear);
		lv = (ListView) findViewById(R.id.listView1);
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		mIp.setText("本机ip-->" + getLocalIpAddress());
		ba = new MyAdapter();
		lv.setAdapter(ba);
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				ba.notifyDataSetChanged();
				super.handleMessage(msg);
			}
		};
		searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mip = getLocalIpAddress();//"192.168.2.xxx";
				final String destIp = mip.substring(0, mip.lastIndexOf(".")+1);
				System.out.println("destIp---->"+destIp);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						for(int i = 2;i<255;i++){
							search(destIp+i);
						}
					}
				}).start();
			}
		});
		clearBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stbs.clear();
				ba.notifyDataSetChanged();
			}
		});
		
	}



	public void search(String destip) {
		try {
			byte [] data = XmlUtil.SearchReq( "123456",getLocalIpAddress());
			stbIp = InetAddress.getByName(destip);
			DatagramPacket p = new DatagramPacket(data, data.length, stbIp,XmlUtil.stbPort);
			s.send(p);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	
    Runnable updateThread = new Runnable() {  
        public void run() {  
        	startReceive();
        }  
    };  
	protected void startReceive() {
		 try {  
            byte[] buffer = new byte[1024];  
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);  
            while (true) { 
                s.receive(packet);  
                if (packet.getLength() > 0) {  
                    String str = new String(buffer, 0, packet.getLength());
                    System.out.println("receive-->"+str);
                    MscpDataParser.getInstance().init(this);
	                MscpDataParser.getInstance().parse(packet,new MscpDataParser.CallBack() {
                      @Override
                      public void onParseCompleted(HashMap<String, String> map) {
                          if( map != null ){
                        	  String req = map.get("cmd");
                        	  System.out.println("req--->"+req);
                        	  if(req!=null&&req.equals("stbresp")){
                        		  STB stb = new STB(map.get("IP"), map.get("taskno"),
                        				  map.get("username"), map.get("password"), map.get("mcid"));
                        		  if(!checkStbIsExist(stb))
                        			  stbs.add(stb);
                        	  }
                          }
                      }
                   
                     private boolean checkStbIsExist(STB stb) {
						// TODO Auto-generated method stub
						for(STB temp:stbs){
							if(temp.getIp().equals(stb.getIp()))
								return true;
						}
						return false;
					}

					@Override
                      public void onError(int code, String desc) {
                    	 
                      }
                  });
                    handler.sendEmptyMessageDelayed(1, 200);
                }  
            }
        } catch (SocketException e) {  
            e.printStackTrace();
        } catch (IOException e) {  
            e.printStackTrace();
        }
	}
    
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return stbs.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = inflater.inflate(R.layout.stbitem, null);
			TextView stbName = (TextView) v.findViewById(R.id.stbName);
			final TextView stbIP = (TextView) v.findViewById(R.id.stbName);
			final EditText title  = (EditText) v.findViewById(R.id.title);
			final Button stopBtn = (Button) v.findViewById(R.id.stop);
			
			STB stb = stbs.get(position);
			stbName.setText(stb.getUsername());
			stbIP.setText(stb.getIp());

			stopBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String url = getUrl(stbIP, title);
					HttpGet request = new HttpGet(url);
					HttpClient client = new DefaultHttpClient();
					try {
						HttpResponse response = client.execute(request);
						if(response.getStatusLine().getStatusCode() == 200){
							if(stopBtn.getText().toString().equals("open"))
								stopBtn.setText("close");
							else
								stopBtn.setText("open");
						}else
							System.out.println("连接异常-statusCode-->"+response.getStatusLine().getStatusCode());
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

//				http://192.168.1.106:8888/interactive/ishow?title=%E
				private String getUrl(final TextView stbIP, final EditText title) {
					return "http://"+stbIP+":8888/interactive/ishow?title="+title.getText().toString().trim();
				}
			});
			return v;
			
		}
		
		
	}
}