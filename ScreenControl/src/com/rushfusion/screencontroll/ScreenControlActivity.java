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
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
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
	Button searchBtn, clearBtn;
	ListView lv;
	ProgressBar progress;
	LayoutInflater inflater;
	String localIp = "";

	InetAddress stbIp;
	List<STB> stbs = new ArrayList<STB>();
	BaseAdapter ba;
	Handler handler;
	DatagramSocket s = null;
	SharedPreferences sp;
	SharedPreferences.Editor editor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
	}

	private void init() {
		if (checkNetworking(this)) {
			try {
				s = new DatagramSocket(6802);
				sp = getSharedPreferences("interact_title",
						MODE_WORLD_WRITEABLE);
				editor = sp.edit();
				findByIds();
				Thread mReceiveThread = new Thread(updateThread);
				mReceiveThread.start();
			} catch (SocketException e) {
				e.printStackTrace();
			}
		} else
			showDialog(0);
	}

	/**
	 * 检查网络连接是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNetworking(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nwi = cm.getActiveNetworkInfo();
		if (nwi != null) {
			return nwi.isAvailable();
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("网络连接提示");
			builder.setMessage("当前没有可用网络，是否设置?")
					.setCancelable(false)
					.setPositiveButton("设置",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent intent = new Intent(
											Settings.ACTION_WIRELESS_SETTINGS);
									startActivity(intent);
									init();
								}
							})
					.setNegativeButton("退出",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							});
			return builder.create();
		case 1:

			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle("提示!");
			dialog.setMessage("正在搜索,请稍后...");
			return dialog;

		default:
			break;
		}
		return null;
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
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				ba.notifyDataSetChanged();
				super.handleMessage(msg);
			}
		};
		searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchBtn.setEnabled(false);
				showDialog(1);
				localIp = getLocalIpAddress();// "192.168.2.xxx";
				final String destIp = localIp.substring(0,
						localIp.lastIndexOf(".") + 1);
				System.out.println("destIp---->" + destIp);
				new Thread(new Runnable() {

					@Override
					public void run() {
						for (int i = 2; i < 255; i++) {
							if (!localIp.equals(destIp + i))
								search(destIp + i);
						}
						dismissDialog(1);
					}
				}).start();
			}
		});
		clearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				searchBtn.setEnabled(true);
				stbs.clear();
				ba.notifyDataSetChanged();
			}
		});

	}

	public void search(String destip) {
		try {
			byte[] data = XmlUtil.SearchReq("123456", getLocalIpAddress());
			stbIp = InetAddress.getByName(destip);
			DatagramPacket p = new DatagramPacket(data, data.length, stbIp,
					XmlUtil.stbPort);
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
			System.out.println("the receive-thread is running");
			startReceive();
		}
	};

	protected void startReceive() {
		try {
			byte[] buffer = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (true) {
				s.receive(packet);
				if (packet.getLength() > 0) {
					String str = new String(buffer, 0, packet.getLength());
					System.out.println("receive-->" + str);
					MscpDataParser.getInstance().init(this);
					MscpDataParser.getInstance().parse(packet,
							new MscpDataParser.CallBack() {
								@Override
								public void onParseCompleted(
										HashMap<String, String> map) {
									if (map != null) {
										System.out.println("IP===>"
												+ map.get("IP"));
										if (map.get("IP") != null
												&& !map.get("IP").equals(
														localIp)) {
											STB stb = new STB(map.get("IP"),
													"test", "test", "test",
													"test");
											if (!checkStbIsExist(stb))
												stbs.add(stb);
										}
										// String req = map.get("cmd");
										// System.out.println("req--->"+req);
										// if(req!=null&&req.equals("stbresp")){
										// }
									}
								}

								private boolean checkStbIsExist(STB stb) {
									// TODO Auto-generated method stub
									for (STB temp : stbs) {
										if (temp.getIp().equals(stb.getIp()))
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

	@Override
	protected void onStop() {
		super.onStop();
		if (s != null)
			s.close();
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return stbs.size();
		}

		@Override
		public Object getItem(int arg0) {
			return stbs.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.stbitem, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.stbName);
				holder.ip = (TextView) convertView.findViewById(R.id.stbIp);
				holder.title = (EditText) convertView.findViewById(R.id.title);
				holder.btn = (Button) convertView.findViewById(R.id.stop);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.init();
			return convertView;
		}
	}
	HashMap<String, String> data = new HashMap<String, String>();

	private class ViewHolder {

		TextView name;
		TextView ip;
		EditText title;
		Button btn;
		int position;

		public ViewHolder() {

		}

		public void init() {
			STB stb = stbs.get(position);
			name.setText(stb.getUsername());
			ip.setText(stb.getIp());
			title.setText(sp.getString("title"+position, "满秋"));
//			title.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					title.requestFocus();
//					InputMethodManager m = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//					m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//				}
//			});
			title.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					data.put("title" + position, s.toString());
				}
			});

			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String url = getUrl(stbs.get(position).getIp(),data.get("title" + position));
					System.out.println("open/close url==>" + url);
					HttpGet request = new HttpGet(url);
					request.setHeader("Content-Type", "charset:UTF-8");
					HttpClient client = new DefaultHttpClient();
					HttpParams params = client.getParams();
					HttpConnectionParams.setConnectionTimeout(params, 3000);
					HttpConnectionParams.setSoTimeout(params, 5000);
					request.setParams(params);
					try {
						HttpResponse response = client.execute(request);
						if (response.getStatusLine().getStatusCode() == 200) {
							if (btn.getText().toString().equals("open")) {
								editor.putString("title" + position,title.getText().toString());
								editor.commit();
								btn.setText("close");
							} else
								btn.setText("open");
						} else
							System.out.println("连接异常-statusCode-->"
									+ response.getStatusLine().getStatusCode());
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				private String getUrl(String stbIP, String title) {
					return "http://" + stbIP + ":8888/interactive/ishow?title="+ title;
				}
			});
		}
	}

}
