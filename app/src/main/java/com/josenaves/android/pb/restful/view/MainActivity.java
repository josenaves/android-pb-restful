package com.josenaves.android.pb.restful.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.josenaves.android.pb.restful.PreferencesUtils;
import com.josenaves.android.pb.restful.R;
import com.josenaves.android.pb.restful.api.ImageAPI;
import com.josenaves.android.pb.restful.api.ImageBase64API;
import com.josenaves.android.pb.restful.api.WebSocketAPI;
import com.josenaves.android.pb.restful.api.WebSocketService;
import com.josenaves.android.pb.restful.data.ImagesDataSource;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements WebSocketAPI {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int TOTAL_REQUESTS = 500;

    private ImagesDataSource dataSource;

    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fabProtocolBuffers;
    private FloatingActionButton fabBase64;

    private Button buttonBatch64;
    private Button buttonBatchProtocolBuffers;
    private Button buttonBatchWebSocket;

    private TextView textId;
    private TextView textName;
    private TextView textDate;
    private TextView textSize;

    private ImageAPI imageAPI;
    private ImageBase64API imageBase64API;
    private WebSocketService webSocketAPI;

    private MaterialDialog wsDialog;


    class Benchmark {
        long totalDatabase = 0;
        long totalAPI = 0;
        long startTime = 0;
        long totalResponses = 0;
        boolean isFinished = false;
    };

    final Benchmark benchmark = new Benchmark();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        refreshActionbarTitle();

        // prepare database to use
        dataSource = new ImagesDataSource(this);

        // make api client
        imageAPI = new ImageAPI(this);
        imageBase64API = new ImageBase64API(this);
        webSocketAPI = new WebSocketService(this);

        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinator_layout);

        textId = (TextView)findViewById(R.id.text_id);
        textName = (TextView)findViewById(R.id.text_name);
        textDate = (TextView)findViewById(R.id.text_date);
        textSize = (TextView)findViewById(R.id.text_size);

        fabProtocolBuffers = (FloatingActionButton) findViewById(R.id.fab);
        fabProtocolBuffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Getting random image from server...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //save(imageAPI.getRandomImage());
                    }
                }).start();
            }
        });

        fabBase64 = (FloatingActionButton) findViewById(R.id.fabSaveBatch);
        fabBase64.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Getting base64 image from server...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //save(imageBase64API.getFlorianopolisBase64Image());
                    }
                }).start();
            }
        });

        buttonBatch64 = (Button) findViewById(R.id.button_base64_batch);
        buttonBatch64.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "::: Benchmark Batch Base64");
                Log.d(TAG, "Get florianopolis image from server (500 times)...");

                final MaterialDialog dialog = showProgressDialog("Benchmarking JSON with Base64");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalApi = 0, totalDatabase = 0;
                        long ini, end;
                        ImageBase64 image;
                        for (int i = 0; i < TOTAL_REQUESTS; i++) {
                            ini = Calendar.getInstance().getTimeInMillis();
                            try {
                                image = imageBase64API.getFlorianopolisBase64Image();
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

        buttonBatchProtocolBuffers = (Button) findViewById(R.id.button_pb_batch);
        buttonBatchProtocolBuffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "::: Benchmark Batch Protocol Buffers");
                Log.d(TAG, "Get florianopolis image from server (500 times)...");

                final MaterialDialog dialog = showProgressDialog("Benchmarking Protocol Buffers");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long totalApi = 0, totalDatabase = 0;
                        long ini, end;
                        Image image;
                        for (int i = 0; i < TOTAL_REQUESTS; i++) {
                            ini = Calendar.getInstance().getTimeInMillis();
                            try {
                                image = imageAPI.getFlorianoplisImage();
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

        buttonBatchWebSocket = (Button) findViewById(R.id.button_websocket_batch);
        buttonBatchWebSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "::: Benchmark Batch WebSockets");
                Log.d(TAG, "Get florianopolis image from server (500 times)...");

                wsDialog = showProgressDialog("Benchmarking Protocol Buffers over Websockets");

                benchmark.isFinished = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        benchmark.startTime = Calendar.getInstance().getTimeInMillis();
                        for (int i = 0; i < TOTAL_REQUESTS; i++) {
                            webSocketAPI.request();
                        }
                        benchmark.isFinished = true;
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
        final MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                .title(title)
                .content(R.string.please_wait)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();

        return dialog;
    }

    private void showResultsDialog(final String title, final String content) {
        new MaterialDialog.Builder(MainActivity.this)
                .title(title)
                .content(content)
                .positiveText("Dismiss")
                .autoDismiss(true)
                .show();
    }

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
            wsDialog.dismiss();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

        benchmark.totalResponses += 1;

        if (benchmark.isFinished && benchmark.totalResponses == TOTAL_REQUESTS) {

            wsDialog.dismiss();

            long end = Calendar.getInstance().getTimeInMillis();
            benchmark.totalAPI = end - benchmark.startTime - benchmark.totalDatabase ;

            long totalTime = benchmark.totalAPI + benchmark.totalDatabase;
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

    @Override
    public Context getContext() {
        return this;
    }

    public void refreshActionbarTitle() {
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " - server : " +
                PreferencesUtils.getHost(this));
    }

}