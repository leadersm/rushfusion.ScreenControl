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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
	
	
	private static final String testTitle = "寰宇MP40301";
	private static final int testDuration = 304;
	private static final String testUrl = "http://vodm3u8.test.itv.cn:1124/vod/3dec2ab9670543f3b42500548dee96b1.m3u8?pt=1&ra=1&version=20111201.171008";
	
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
	                //得到udp数据: datagramPacket
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
			Button playBtn = (Button) v.findViewById(R.id.playOrPause);
			Button stopBtn = (Button) v.findViewById(R.id.stop);
			SeekBar voice = (SeekBar) v.findViewById(R.id.voice);
			SeekBar duration = (SeekBar) v.findViewById(R.id.duration);
			
			STB stb = stbs.get(position);
			stbName.setText(stb.getUsername());
			stbIP.setText(stb.getIp());

			playBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					play(testTitle,testDuration,testUrl);
				}
			});
			stopBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
			voice.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub
					
				}
			});
			duration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub
					
				}
			});
			return v;
			
		}

		protected void play(String title, int duration, String url) {
			// TODO Auto-generated method stub
			
			
		}
		
	}
}