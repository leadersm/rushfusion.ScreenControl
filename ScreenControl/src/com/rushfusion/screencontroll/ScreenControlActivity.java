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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rushfusion.screencontroll.bean.STB;
import com.rushfusion.screencontroll.util.MscpDataParser;
import com.rushfusion.screencontroll.util.XmlUtil;

public class ScreenControlActivity extends Activity {
	/** Called when the activity is first created. */
	TextView mIp;
	Button searchBtn;
	ListView lv;
	ProgressBar progress;
	LayoutInflater inflater;
	
	InetAddress stbIp;
	List<STB> stbs;
	BaseAdapter ba;
	Handler handler;
	DatagramSocket s = null;
	List<String> tests;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		stbs = new ArrayList<STB>();
		tests = new ArrayList<String>();
		try {
			s = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		findByIds();
		Thread mReceiveThread= new Thread(updateThread);  
        mReceiveThread.start();
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
		lv = (ListView) findViewById(R.id.listView1);
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		mIp.setText("本机ip：" + getLocalIpAddress());
		searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mip = getLocalIpAddress();//"192.168.2.xxx";
				String destIp = mip.substring(0, mip.lastIndexOf(".")+1);
				System.out.println("destIp---->"+destIp);
				for(int i = 2;i<255;i++){
					search(destIp+i);
				}
			}
		});
		ba = new MyAdapter();
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				ba.notifyDataSetChanged();
				lv.setAdapter(ba);
				super.handleMessage(msg);
			}
		};
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
                    String str = new String(buffer, 0, packet  
                            .getLength());
                    System.out.println("receive-->"+str);
//                    tests.add(str);
                    MscpDataParser.getInstance().init(this);
	                //�õ�udp���: datagramPacket
	                MscpDataParser.getInstance().parse(packet,new MscpDataParser.CallBack() {
                      @Override
                      public void onParseCompleted(HashMap<String, String> map) {
                          // TODO Auto-generated method stub
                          if( map != null ){
                              // handle your map
                        	  String req = map.get("cmd");
                        	  System.out.println("req--->"+req);
                        	  if(req!=null&&req.equals("stbresp")){
                        		  STB stb = new STB(map.get("IP"), map.get("taskno"),
                        				  map.get("username"), map.get("password"), map.get("mcid"));
                        		  stbs.add(stb);
                        	  }
                          }
                      }
                   
                     @Override
                      public void onError(int code, String desc) {
                         // TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			return stbs.size();
//			return tests.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = inflater.inflate(R.layout.stbitem, null);
			TextView stbName = (TextView) v.findViewById(R.id.stbName);
			TextView stbIP = (TextView) v.findViewById(R.id.stbName);
			Button stopBtn = (Button) v.findViewById(R.id.stop);
			
			STB stb = stbs.get(position);
			stbName.setText(stb.getUsername());
			stbIP.setText(stb.getIp());

			stopBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
			return v;
			
		}
		
	}
}