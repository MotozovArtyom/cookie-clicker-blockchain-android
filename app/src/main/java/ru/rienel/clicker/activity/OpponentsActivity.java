package ru.rienel.clicker.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import ru.rienel.clicker.ActivityWithNetwork;
import ru.rienel.clicker.R;
import ru.rienel.clicker.db.domain.Opponent;
import ru.rienel.clicker.db.factory.domain.OpponentFactory;
import ru.rienel.clicker.service.ConfigInfo;
import ru.rienel.clicker.service.DeviceActionListener;
import ru.rienel.clicker.service.NetworkService;
import ru.rienel.clicker.ui.view.OpponentListFragment;

import java.util.ArrayList;
import java.util.List;

public class OpponentsActivity extends AppCompatActivity implements DeviceActionListener, ActivityWithNetwork {
	public static final String TAG = OpponentsActivity.class.getName();

	private OpponentListFragment opponentListFragment;
	private ServiceConnection connection = getServiceConnection();
	private NetworkService networkService;

	private String selectHost = "";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opponents_activity);

		Toolbar opponentToolbar = findViewById(R.id.opponent_toolbar);
		opponentToolbar.setNavigationIcon(R.drawable.ic_back);
		setSupportActionBar(opponentToolbar);

		Intent serviceIntent = NetworkService.newIntent(this);
		startService(serviceIntent);

		bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentById(R.id.opponent_fragment_container);

		if (fragment == null) {
			fragment = new OpponentListFragment();
			fragmentManager.beginTransaction()
					.add(R.id.opponent_fragment_container, fragment)
					.commit();
		}
	}

	private ServiceConnection getServiceConnection() {
		return new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "onServiceConnected: called");
				// We've binded to LocalService, cast the IBinder and get LocalService instance
				NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
				networkService = binder.getService(); //Get instance of your service!
				networkService.registerActivity(OpponentsActivity.this); //Activity register in the service as client for callabcks!
				Log.d(TAG, "onServiceConnected: connected to service");
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "onServiceDisconnected: called");
			}
		};
	}

	private OpponentListFragment getFragment() {
		if (opponentListFragment == null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			opponentListFragment = (OpponentListFragment) fragmentManager
					.findFragmentById(R.id.opponent_fragment_container);
		}
		return opponentListFragment;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(connection);
		Log.d(TAG, "onDestroy: Service unbinded");
	}

	public void updateThisDevice(Opponent device) {
		getFragment().updateOpponent(device);
	}


	@Override
	protected void onStart() {
		super.onStart();


		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentById(R.id.opponent_fragment_container);
		if (fragment != null) {
//			networkService.discoverPeers();
//			List<WifiP2pDevice> p2pDevices = networkService.getP2pDevices();
//			List<Opponent> opponentList = new ArrayList<>(p2pDevices.size());
//			for (WifiP2pDevice device: p2pDevices) {
//				Opponent opponent = OpponentFactory.buildFromWifiP2pDevice(device);
//				opponentList.add(opponent);
//			}
//			((OpponentListFragment) fragment).setOpponentList(opponentList);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	static private class ActivityHandler extends Handler {
		private static final String TAG = "ActivityHandler";
		private OpponentsActivity activity;

		ActivityHandler(OpponentsActivity activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "handleMessage()  msg.what:" + msg.what);
			switch (msg.what) {
				case ConfigInfo.MSG_RECV_PEER_INFO:
//					activity.getDetailFragment().showSendFileVeiw();
					break;

				case ConfigInfo.MSG_REPORT_SEND_PEER_INFO_RESULT:
//					if (msg.arg1 == 0)
//						activity.showToastTips("send peer's info successed.");
//					else
//						activity.showToastTips("send peer's info failed.");
					break;
				case ConfigInfo.MSG_REPORT_RECV_PEER_LIST:
//					activity.showToastTips("receive peer list.");
				default:
//					activity.showToastTips("error msg id.");
			}
			super.handleMessage(msg);
		}
	}

	private Handler handler = new ActivityHandler(this);

	public Handler getHandler() {
		return handler;
	}

	public void resetPeers() {
		getFragment().clearOpponents();
	}

	@Override
	public void updatePeers(List<WifiP2pDevice> wifiP2pDeviceList) {
		List<Opponent> opponentList = new ArrayList<>(wifiP2pDeviceList.size());
		for (WifiP2pDevice device : wifiP2pDeviceList) {
			opponentList.add(OpponentFactory.buildFromWifiP2pDevice(device));
		}
		if (opponentListFragment == null) {
			opponentListFragment = getFragment();
		}
		opponentListFragment.updateOpponentList(opponentList);
	}

	@Override
	public void showDetails(WifiP2pDevice device) {
	}

	@Override
	public void cancelDisconnect() {
		Log.e(TAG, "cancelDisconnect.");
//		WifiP2pManager manager = networkService.getManager();
//		if (manager != null) {
		final OpponentListFragment fragment = getFragment();
//			if (fragment.getDevice() == null
//					|| fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
//				disconnect();
//			} else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
//					|| fragment.getDevice().status == WifiP2pDevice.INVITED) {
//				networkService.cancelDisconnect();
//			}
//		}
	}

	@Override
	public void connect(WifiP2pConfig config) {
//		networkService.connect(config);
	}

	@Override
	public void disconnect() {
		resetPeers();
//		networkService.removeGroup();
//		networkService.discoverPeers();
	}
}