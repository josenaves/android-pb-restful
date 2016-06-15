package com.josenaves.android.pb.restful.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.josenaves.android.pb.restful.Image;
import com.josenaves.android.pb.restful.ImageBase64;
import com.josenaves.android.pb.restful.api.UploadAPI;
import com.josenaves.android.pb.restful.utils.ByteUtils;
import com.josenaves.android.pb.restful.utils.PreferencesUtils;
import com.josenaves.android.pb.restful.R;
import com.josenaves.android.pb.restful.api.ImageAPI;
import com.josenaves.android.pb.restful.api.ImageBase64API;
import com.josenaves.android.pb.restful.api.SocketListener;
import com.josenaves.android.pb.restful.api.WebSocketService;
import com.josenaves.android.pb.restful.data.ImagesDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MAX_REQUESTS = 1;

    private ImagesDataSource dataSource;

    private TextView textId;
    private TextView textName;
    private TextView textDate;
    private TextView textSize;

    private WebSocketService webSocketAPI;
    private MaterialDialog wsDialog;

    class Benchmark {
        long totalDatabase = 0;
        long totalAPI = 0;
        long startTime = 0;
        long totalResponses = 0;
    }

    final Benchmark benchmark = new Benchmark();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        refreshActionbarTitle();

        // prepare database to use
        dataSource = new ImagesDataSource(getContext());

        textId = (TextView)findViewById(R.id.text_id);
        textName = (TextView)findViewById(R.id.text_name);
        textDate = (TextView)findViewById(R.id.text_date);
        textSize = (TextView)findViewById(R.id.text_size);

        Button buttonBatch64 = (Button) findViewById(R.id.button_base64_batch);
        buttonBatch64.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "::: Benchmark Batch Base64");
                Log.d(TAG, "Get tree image from server (500 times)...");

                final ImageBase64API imageBase64API = new ImageBase64API(getContext());

                final MaterialDialog dialog = showProgressDialog("Benchmarking JSON with Base64");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalApi = 0, totalDatabase = 0;
                        long ini, end;
                        ImageBase64 image;
                        for (int i = 0; i < MAX_REQUESTS; i++) {
                            ini = Calendar.getInstance().getTimeInMillis();
                            try {
                                image = imageBase64API.getTreeBase64Image();
                                end = Calendar.getInstance().getTimeInMillis();
                                totalApi += end - ini;

                                ini = Calendar.getInstance().getTimeInMillis();
                                save(image);
                                end = Calendar.getInstance().getTimeInMillis();

                                totalDatabase += end - ini;
                            } catch (Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Timeout !", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                                return;
                            }
                        }

                        dialog.dismiss();

                        long totalTime = totalApi + totalDatabase;

                        Log.i(TAG, ":::::::::::: Benchmark Base64");
                        Log.i(TAG, ":::::::::::: Total time = " + totalTime + " milliseconds");
                        Log.i(TAG, ":::::::::::: Total API = " + totalApi + " milliseconds");
                        Log.i(TAG, ":::::::::::: Total DB = " + totalDatabase + " milliseconds");

                        final String content = String.format(
                                "Total time : %s milliseconds\n" +
                                        "Total JSON Base64 : %s milliseconds\n" +
                                        "Total database : %s milliseconds",
                                totalTime, totalApi, totalDatabase);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showResultsDialog("JSON with Base64 results", content);
                            }
                        });
                    }
                }).start();
            }
        });

        Button buttonBatchProtocolBuffers = (Button) findViewById(R.id.button_pb_batch);
        buttonBatchProtocolBuffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "::: Benchmark Batch Protocol Buffers");
                Log.d(TAG, "Get tree image from server (500 times)...");

                final ImageAPI imageAPI = new ImageAPI(getContext());

                final MaterialDialog dialog = showProgressDialog("Benchmarking Protocol Buffers");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalApi = 0, totalDatabase = 0;
                        long ini, end;
                        Image image;
                        for (int i = 0; i < MAX_REQUESTS; i++) {
                            ini = Calendar.getInstance().getTimeInMillis();
                            try {
                                image = imageAPI.getTreeImage();
                                end = Calendar.getInstance().getTimeInMillis();
                                totalApi += end - ini;

                                ini = Calendar.getInstance().getTimeInMillis();
                                save(image);
                                end = Calendar.getInstance().getTimeInMillis();
                                totalDatabase += end - ini;
                            }
                            catch (Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Timeout !", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                        }

                        dialog.dismiss();

                        long totalTime = totalApi + totalDatabase;

                        Log.i(TAG, ":::::::::::: Benchmark Protocol Buffers");
                        Log.i(TAG, ":::::::::::: Total time = " + totalTime + " milliseconds");
                        Log.i(TAG, ":::::::::::: Total API = " + totalApi + " milliseconds");
                        Log.i(TAG, ":::::::::::: Total DB = " + totalDatabase + " milliseconds");

                        final String content = String.format(
                                "Total time : %s milliseconds\n" +
                                "Total ProtocolBuffers : %s milliseconds\n" +
                                "Total database : %s milliseconds",
                                totalTime, totalApi, totalDatabase);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showResultsDialog("Protocol Buffers over HTTP results", content);
                            }
                        });

                    }
                }).start();
            }
        });

        Button buttonBatchWebSocket = (Button) findViewById(R.id.button_websocket_batch);
        buttonBatchWebSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "::: Benchmark Batch WebSockets");
                Log.d(TAG, "Get florianopolis image from server (500 times)...");

                wsDialog = showProgressDialog("Benchmarking Protocol Buffers over Websockets");

                benchmark.startTime = Calendar.getInstance().getTimeInMillis();
                benchmark.totalResponses = 0;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Making a new websocket request - " + benchmark.totalResponses);
                        webSocketAPI.request();
                    }
                }).start();
            }
        });

        Button buttonBatchUploadPB = (Button) findViewById(R.id.button_upload_batch);
        buttonBatchUploadPB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "::: Benchmark Batch Upload PB");
                Log.d(TAG, "Upload tree image to server (500 times)...");

                final UploadAPI api = new UploadAPI(getContext());

                final MaterialDialog dialog = showProgressDialog("Benchmarking Upload PB");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalApi = 0;
                        long ini, end;
                        Image image;

                        for (int i = 0; i < MAX_REQUESTS; i++) {
                        //for (int i = 0; i < 1; i++) {
                            ini = Calendar.getInstance().getTimeInMillis();
                            try {
                                InputStream inStream = getContext().getResources().openRawResource(R.raw.tree);
                                byte[] imageBytes = new byte[inStream.available()];
                                imageBytes = ByteUtils.convertStreamToByteArray(inStream);
                                inStream.close();

                                String id = UUID.randomUUID().toString();
                                Calendar cal = Calendar.getInstance();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:S");
                                String date = sdf.format(cal.getTime());

                                ByteString bs = ByteString.of(imageBytes);
                                Log.d(TAG, "size of bytes:" + imageBytes.length);
                                Log.d(TAG, "size of ByteString:" + bs.size());

                                image = new Image(id, id + "-tree.jpg", date, bs);

                                api.uploadImage(image);

                                end = Calendar.getInstance().getTimeInMillis();
                                totalApi += end - ini;
                            }
                            catch (Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Timeout !", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                        }

                        dialog.dismiss();

                        long average = totalApi/MAX_REQUESTS;

                        Log.i(TAG, ":::::::::::: Benchmark Batch Upload PB");
                        Log.i(TAG, ":::::::::::: Total time = " + totalApi + " milliseconds");
                        Log.i(TAG, ":::::::::::: Average = " + average + " milliseconds");

                        final String content = String.format(
                                "Total time : %s milliseconds\n" +
                                        "Average: %s milliseconds",
                                totalApi, average);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showResultsDialog("Upload image results", content);
                            }
                        });

                    }
                }).start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent prefsIntent = new Intent(this, SettingsActivity.class);
            startActivity(prefsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save(final Image image) {
        // persist the image
        dataSource.open();
        dataSource.createImage(image);
        dataSource.close();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textId.setText("ID: " + image.id);
                textName.setText("Name: " + image.name);
                textDate.setText("Date: " + image.date);
                textSize.setText("Size: " + image.image_data.size() + " bytes");
            }
        });
    }

    private void save(final ImageBase64 image) {
        // persist the image
        dataSource.open();
        dataSource.createImageBase64(image);
        dataSource.close();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textId.setText("ID: " + image.id);
                textName.setText("Name: " + image.name);
                textDate.setText("Date: " + image.datetime);
                textSize.setText("Size: " + image.image_data.length() + " bytes");
            }
        });
    }

    private MaterialDialog showProgressDialog(String title) {
        return new MaterialDialog.Builder(MainActivity.this)
                .title(title)
                .content(R.string.please_wait)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();
    }

    private void showResultsDialog(final String title, final String content) {
        new MaterialDialog.Builder(MainActivity.this)
                .title(title)
                .content(content)
                .positiveText("Dismiss")
                .autoDismiss(true)
                .show();
    }


    private SocketListener mainSocketListener = new SocketListener() {
        @Override
        public void onTimeout(TimeoutException e) {
            Log.e(TAG, "WebSocket - timeout error:" + e.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wsDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Timeout !", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onException(Exception e) {
            Log.e(TAG, "WebSocket - error:" + e.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wsDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Error with websockets!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onResponse(byte[] response) {
            Log.d(TAG, "WS packet size: " + response.length);

            try {
                final Image image = Image.ADAPTER.decode(response);

                long ini = Calendar.getInstance().getTimeInMillis();
                save(image);
                long end = Calendar.getInstance().getTimeInMillis();

                benchmark.totalDatabase = benchmark.totalDatabase + (end - ini);
            }
            catch (IOException io) {
                final String msg = "Error decoding message from websocket server";
                Log.e(TAG, msg + " - " + io.getMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wsDialog.dismiss();
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            benchmark.totalResponses += 1;

            if (benchmark.totalResponses == MAX_REQUESTS) {

                long endTime = Calendar.getInstance().getTimeInMillis();
                long totalTime = endTime - benchmark.startTime;

                wsDialog.dismiss();

                benchmark.totalAPI = totalTime - benchmark.totalDatabase;

                Log.i(TAG, ":::::::::::: Benchmark WebSockets");
                Log.i(TAG, ":::::::::::: Total time = " + totalTime + " milliseconds");
                Log.i(TAG, ":::::::::::: Total API = " + benchmark.totalAPI + " milliseconds");
                Log.i(TAG, ":::::::::::: Total DB = " + benchmark.totalDatabase + " milliseconds");

                final String content = String.format(
                        "Total time : %s milliseconds\n" +
                                "Total ProtocolBuffers WS: %s milliseconds\n" +
                                "Total database : %s milliseconds",
                        totalTime, benchmark.totalAPI , benchmark.totalDatabase );

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResultsDialog("Protocol Buffers over Websockets results", content);
                    }
                });
            }
        }
    };

    private SocketListener requestSocketListener = new SocketListener() {
        @Override
        public void onResponse(byte[] response) {
            if (benchmark.totalResponses < MAX_REQUESTS) {
                Log.d(TAG, "Making a new websocket request - " + benchmark.totalResponses);
                webSocketAPI.request();
            }
        }

        @Override
        public void onTimeout(TimeoutException e) {
            Log.d(TAG, "TimeoutException on requestSocketListener!");
        }

        @Override
        public void onException(Exception e) {
            Log.d(TAG, "Exception on requestSocketListener!");
        }
    };

    public Context getContext() {
        return this.getApplicationContext();
    }

    public void refreshActionbarTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.app_name) + " - server : " +
                    PreferencesUtils.getHost(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActionbarTitle();
        webSocketAPI = new WebSocketService(getContext());
        webSocketAPI.registerListener(mainSocketListener);
        webSocketAPI.registerListener(requestSocketListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webSocketAPI.unregisterListener(mainSocketListener);
        webSocketAPI.unregisterListener(requestSocketListener);
        webSocketAPI = null;
    }
}