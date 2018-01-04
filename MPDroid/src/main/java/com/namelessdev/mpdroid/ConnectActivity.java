package com.namelessdev.mpdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.namelessdev.mpdroid.tools.Tools;
import com.tsengvn.typekit.TypekitContextWrapper;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jcifs.smb.SmbNamedPipe;
import org.a0z.mpd.BuildConfig;
import uk.co.senab.actionbarpulltorefresh.library.R;
import uk.co.senab.actionbarpulltorefresh.library.listeners.HeaderViewListener;

public class ConnectActivity extends Activity {
    private int mBackPressExitCount;
    private DisplayStatus mDisplayStatus = DisplayStatus.CONNECT_STATUS;
    private Handler mHandler;
    protected ImageView mLoadingImage;
    protected ProgressBar mLoadingProgress;
    private final WifiManager mWifiManager = ((WifiManager) MPDApplication.getInstance().getApplicationContext().getSystemService("wifi"));
    private UdpThread udpReceive = null;

    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$namelessdev$mpdroid$ConnectActivity$DisplayStatus = new int[DisplayStatus.values().length];

        static {
            try {
                $SwitchMap$com$namelessdev$mpdroid$ConnectActivity$DisplayStatus[DisplayStatus.CONNECT_STATUS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
                NoSuchFieldError noSuchFieldError = e;
            }
            try {
                $SwitchMap$com$namelessdev$mpdroid$ConnectActivity$DisplayStatus[DisplayStatus.SEARCH_STATUS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
                noSuchFieldError = e2;
            }
            try {
                $SwitchMap$com$namelessdev$mpdroid$ConnectActivity$DisplayStatus[DisplayStatus.FAIL_STATUS.ordinal()] = 3;
            } catch (NoSuchFieldError e22) {
                noSuchFieldError = e22;
            }
        }
    }

    public enum DisplayStatus {
    }

    class UdpThread extends Thread {
        private boolean mCancel = false;

        public UdpThread() {
            Thread thread = this;
        }

        public void cancelThread() {
            this.mCancel = true;
        }

        public void run() {
            Handler access$500;
            boolean post;
            super.run();
            int e = Log.e("microwaver", "UdpThread - start!!!");
            try {
                String[] data;
                DatagramSocket datagramSocket = r14;
                DatagramSocket datagramSocket2 = new DatagramSocket();
                DatagramSocket socket = datagramSocket;
                String str = r14;
                String str2 = new String("BrickReq");
                byte[] buffer = str.getBytes();
                DatagramPacket datagramPacket = r14;
                DatagramPacket datagramPacket2 = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 51121);
                socket.send(datagramPacket);
                e = Log.w("microwaver", "Send UDP Packet");
                buffer = new byte[SmbNamedPipe.PIPE_TYPE_CALL];
                datagramPacket = r14;
                datagramPacket2 = new DatagramPacket(buffer, buffer.length);
                DatagramPacket packet = datagramPacket;
                socket.setSoTimeout(5000);
                while (true) {
                    try {
                        socket.receive(packet);
                        if (isInterrupted() && this.mCancel) {
                            e = Log.e("microwaver", "UDP receive Packet... & Interrupted");
                        } else {
                            e = Log.e("microwaver", "UDP receive Packet...");
                            str = r14;
                            str2 = new String(packet.getData());
                            String temp = str.trim();
                            StringBuilder stringBuilder = r14;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            e = Log.w("microwaver", stringBuilder.append("temp : ").append(temp).toString());
                            data = temp.split("\\|");
                            for (int i = 0; i < data.length; i++) {
                                stringBuilder = r14;
                                stringBuilder2 = new StringBuilder();
                                e = Log.w("microwaver", stringBuilder.append("data[").append(i).append("] : ").append(data[i]).toString());
                            }
                            if (data[0].equals("BrickAck")) {
                                break;
                            }
                        }
                    } catch (SocketTimeoutException e2) {
                        if (isInterrupted() && this.mCancel) {
                            e = Log.e("microwaver", "UDP receive Time out... & Interrupted");
                        } else {
                            e = Log.w("microwaver", "UDP receive Time out...");
                            access$500 = ConnectActivity.this.mHandler;
                            AnonymousClass2 anonymousClass2 = r14;
                            AnonymousClass2 anonymousClass22 = new Runnable() {
                                {
                                    AnonymousClass2 anonymousClass2 = this;
                                }

                                public void run() {
                                    ConnectActivity.this.setDisplayStatus(DisplayStatus.FAIL_STATUS);
                                }
                            };
                            post = access$500.post(anonymousClass2);
                        }
                    }
                }
                e = Log.e("microwaver", "UDP receive Name & IP Addr");
                access$500 = ConnectActivity.this.mHandler;
                AnonymousClass1 anonymousClass1 = r14;
                final String[] strArr = data;
                AnonymousClass1 anonymousClass12 = new Runnable(this) {
                    final /* synthetic */ UdpThread this$1;

                    public void run() {
                        if (this.this$1.mCancel) {
                            int e = Log.e("microwaver", "Cancel Thread");
                            return;
                        }
                        boolean commit = PreferenceManager.getDefaultSharedPreferences(MPDApplication.getInstance()).edit().putString(ConnectActivity.getStringWithSSID("hostname", ConnectActivity.this.getCurrentSSID()), strArr[2]).commit();
                        Intent intent = r8;
                        Intent intent2 = new Intent(ConnectActivity.this.getApplicationContext(), MainMenuActivity.class);
                        ConnectActivity.this.startActivity(intent);
                        ConnectActivity.this.finish();
                    }
                };
                post = access$500.postDelayed(anonymousClass1, 2000);
                socket.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            e = Log.e("microwaver", "UdpThread - END!!!");
        }
    }

    static /* synthetic */ int access$002(ConnectActivity connectActivity, int i) {
        int i2 = i;
        int i3 = i2;
        int i4 = i2;
        connectActivity.mBackPressExitCount = i4;
        return i3;
    }

    public ConnectActivity() {
        Activity activity = this;
        Handler handler = r4;
        Handler handler2 = new Handler();
        this.mHandler = handler;
    }

    protected void attachBaseContext(Context context) {
        super.attachBaseContext(TypekitContextWrapper.wrap(context));
    }

    public void onBackPressed() {
        if (this.mBackPressExitCount < 1) {
            Tools.notifyUser((int) R.string.backpressToQuit);
            this.mBackPressExitCount++;
            Handler handler = this.mHandler;
            AnonymousClass1 anonymousClass1 = r6;
            AnonymousClass1 anonymousClass12 = new Runnable() {
                {
                    AnonymousClass1 anonymousClass1 = this;
                }

                public void run() {
                    int access$002 = ConnectActivity.access$002(ConnectActivity.this, 0);
                }
            };
            boolean postDelayed = handler.postDelayed(anonymousClass1, 3000);
            return;
        }
        finish();
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Handler handler = this.mHandler;
        AnonymousClass2 anonymousClass2 = r6;
        AnonymousClass2 anonymousClass22 = new Runnable() {
            {
                AnonymousClass2 anonymousClass2 = this;
            }

            public void run() {
                ConnectActivity.this.setTheme(R.style.AppTheme);
                ConnectActivity.this.setContentView(R.layout.layout_connect);
                ConnectActivity.this.mLoadingProgress = (ProgressBar) ConnectActivity.this.findViewById(R.id.progress_loading);
                ConnectActivity.this.mLoadingImage = (ImageView) ConnectActivity.this.findViewById(R.id.img_loading);
                TextView textView = (TextView) ConnectActivity.this.findViewById(R.id.connect_pagelink);
                AnonymousClass1 anonymousClass1 = r9;
                AnonymousClass1 anonymousClass12 = new TransformFilter() {
                    {
                        AnonymousClass1 anonymousClass1 = this;
                    }

                    public String transformUrl(Matcher matcher, String str) {
                        Matcher match = matcher;
                        String url = str;
                        return BuildConfig.VERSION_NAME;
                    }
                };
                TransformFilter mTransform = anonymousClass1;
                Linkify.addLinks(textView, Pattern.compile("VIEW MANUAL"), "http://m.brickstudio.co.kr", null, mTransform);
                ConnectActivity.this.setDisplayStatus(DisplayStatus.CONNECT_STATUS);
            }
        };
        boolean postDelayed = handler.postDelayed(anonymousClass2, 1000);
    }

    public void onBtnConnect(View view) {
        View v = view;
        if (isWifiConnected()) {
            UdpThread udpThread = r7;
            UdpThread udpThread2 = new UdpThread();
            this.udpReceive = udpThread;
            this.udpReceive.start();
            setDisplayStatus(DisplayStatus.SEARCH_STATUS);
            return;
        }
        Builder builder = r7;
        Builder builder2 = new Builder(this);
        AnonymousClass3 anonymousClass3 = r7;
        AnonymousClass3 anonymousClass32 = new OnClickListener() {
            {
                AnonymousClass3 anonymousClass3 = this;
            }

            public void onClick(DialogInterface dialogInterface, int i) {
            }
        };
        AlertDialog show = builder.setTitle("\uc640\uc774\ud30c\uc774 \uc5f0\uacb0\ud655\uc778").setMessage("\ud604\uc7ac \uc640\uc774\ud30c\uc774\uc5d0 \uc5f0\uacb0\ub418\uc5b4 \uc788\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4. Wi-Fi Setting \ubc84\ud2bc\uc744 \ub20c\ub7ec \uc5f0\uacb0\uc0c1\ud0dc\ub97c \ud655\uc778\ud574 \uc8fc\uc138\uc694.").setNegativeButton("\ub2eb\uae30", anonymousClass3).setCancelable(true).show();
    }

    public void onBtnWiFi(View view) {
        View v = view;
        Intent intent = r6;
        Intent intent2 = new Intent();
        Intent intent3 = intent;
        intent = intent3.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
        startActivity(intent3);
    }

    public void onBtnCancel(View view) {
        View v = view;
        setDisplayStatus(DisplayStatus.CONNECT_STATUS);
        this.udpReceive.cancelThread();
        this.udpReceive.interrupt();
    }

    private boolean isWifiConnected() {
        return ((ConnectivityManager) getSystemService("connectivity")).getNetworkInfo(1).isConnected();
    }

    private void setDisplayStatus(DisplayStatus displayStatus) {
        DisplayStatus displayStatus2 = displayStatus;
        if (this.mDisplayStatus != displayStatus2) {
            this.mDisplayStatus = displayStatus2;
            switch (AnonymousClass4.$SwitchMap$com$namelessdev$mpdroid$ConnectActivity$DisplayStatus[this.mDisplayStatus.ordinal()]) {
                case HeaderViewListener.STATE_MINIMIZED /*1*/:
                    ((TextView) findViewById(R.id.connect_title)).setText("Connect your\nRASNET Device..");
                    ((TextView) findViewById(R.id.connect_pagelink)).setVisibility(0);
                    ((Button) findViewById(R.id.btn_connect)).setVisibility(0);
                    ((Button) findViewById(R.id.btn_wifi)).setVisibility(0);
                    ((Button) findViewById(R.id.btn_cancel)).setVisibility(8);
                    this.mLoadingImage.setImageResource(R.drawable.device);
                    this.mLoadingImage.setVisibility(0);
                    this.mLoadingProgress.setVisibility(4);
                    return;
                case HeaderViewListener.STATE_HIDDEN /*2*/:
                    ((TextView) findViewById(R.id.connect_title)).setText("Looking for\nRASNET Device..");
                    ((TextView) findViewById(R.id.connect_pagelink)).setVisibility(8);
                    ((Button) findViewById(R.id.btn_connect)).setVisibility(8);
                    ((Button) findViewById(R.id.btn_wifi)).setVisibility(8);
                    ((Button) findViewById(R.id.btn_cancel)).setVisibility(0);
                    this.mLoadingImage.setVisibility(4);
                    this.mLoadingProgress.setVisibility(0);
                    return;
                case R.styleable.SmoothProgressBar_spb_stroke_separator_length /*3*/:
                    ((TextView) findViewById(R.id.connect_title)).setText("RASNET device\nconnection failed..");
                    ((TextView) findViewById(R.id.connect_pagelink)).setVisibility(0);
                    ((Button) findViewById(R.id.btn_connect)).setVisibility(0);
                    ((Button) findViewById(R.id.btn_wifi)).setVisibility(0);
                    ((Button) findViewById(R.id.btn_cancel)).setVisibility(8);
                    this.mLoadingImage.setImageResource(R.drawable.ic_failed);
                    this.mLoadingImage.setVisibility(0);
                    this.mLoadingProgress.setVisibility(4);
                    return;
                default:
                    return;
            }
        }
    }

    private String getCurrentSSID() {
        String ssid = this.mWifiManager.getConnectionInfo().getSSID();
        return ssid == null ? null : ssid.replace("\"", BuildConfig.VERSION_NAME);
    }

    private static String getStringWithSSID(String str, String str2) {
        String param = str;
        String wifiSSID = str2;
        if (wifiSSID == null) {
            return param;
        }
        StringBuilder stringBuilder = r4;
        StringBuilder stringBuilder2 = new StringBuilder();
        return stringBuilder.append(wifiSSID).append(param).toString();
    }
}
