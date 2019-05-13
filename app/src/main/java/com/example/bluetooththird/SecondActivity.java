package com.example.bluetooththird;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;


public class SecondActivity extends Activity {


    private static final int SAMPLING_RATE_IN_HZ = 44100;

    private static final int REQUEST_BABY = 7;

    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final int BUFFER_SIZE_FACTOR = 2;

    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR;

    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

    private AudioRecord recorder = null;

    private Thread recordingThread = null;

    private Button startButton;

    double difference_calculated = 0;

    boolean delaythemedia = true;

    private Button stopButton;

    String tempCalcResult = "";

    private Button detectButton;

    byte[] bytes = new byte[10000000];

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    long byte_size = 0;

    File file2 = new File("/storage/emulated/0/zirp.wav");
    File file1 = new File("/storage/emulated/0/recording.pcm");


    double[] result_snipped = SoundDataUtils.load16BitPCMRawDataFileAsDoubleArray(file2);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle extras = getIntent().getExtras();
        if(extras !=null){
            if(extras.getString("getMessage")!= null){
                String msg=extras.getString("getMessage");
                sometry(msg);
                return;
            }
        }
        setContentView(R.layout.second_view);



        impelemntSecondView();


    }







    public void impelemntSecondView(){
        requestAllPermissions();

        startButton = (Button) findViewById(R.id.btnStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                try {
                    String string = "start";
             //       sendReceive.write(string.getBytes());

                    Intent intent = new Intent(SecondActivity.this,  MainActivity.class);
                    intent.putExtra("message", string);
                    startActivity(intent);
                }catch (Exception e){

                }

                startRecording();
            }
        });

        stopButton = (Button) findViewById(R.id.btnStop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                delaythemedia = true;

                startButton.setEnabled(true);
                stopButton.setEnabled(false);



                try {
                    String string = "stop";
             //       sendReceive.write(string.getBytes());
                    Intent intent = new Intent(SecondActivity.this,  MainActivity.class);
                    intent.putExtra("message", string);
                    startActivity(intent);
                }catch (Exception e){

                }

                stopRecording();
            }
        });

        detectButton = (Button) findViewById(R.id.btnDetect);
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detect();
            }
        });


    }



    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    int PERMISSION_ALL = 1;

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO
    };


    public void requestAllPermissions() {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }


    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE_IN_HZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        recorder.startRecording();

        difference_calculated = 0;
        recordingInProgress.set(true);

        try {
            baos.reset();
        } catch (Exception e) {
        }


        recordingThread = new Thread(new RecordingRunnable(), "Recording Thread");
        recordingThread.start();

/*
        if (delaythemedia == true) {


            try {
                recordingThread.sleep(1000);
                final MediaPlayer mo = MediaPlayer.create(this, R.raw.zirp);
                mo.start();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } else {

            try {
                recordingThread.sleep(300);
                final MediaPlayer mo = MediaPlayer.create(this, R.raw.zirp);
                mo.start();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    */
    }



    private void stopRecording() {
        if (null == recorder) {
            return;
        }

        recordingInProgress.set(false);

        recorder.stop();

        recorder.release();

        recorder = null;

        recordingThread = null;


        detect();


        try {
            recordingThread.sleep(300);

            try {
                String tempMsg = String.valueOf(difference_calculated);
            //    sendReceive.write(tempMsg.getBytes());
                Intent intent = new Intent(SecondActivity.this,  MainActivity.class);
                intent.putExtra("message", tempMsg);
                startActivity(intent);
            }catch (Exception e){
            }



        } catch (InterruptedException e) {
        }


        //distance(difference);
    }


    private double detect() {
        //File file = new File("/storage/emulated/0/recording.pcm");
        //double[] result = SoundDataUtils.load16BitPCMRawDataFileAsDoubleArray(file);
        //bytes[1] = 0;


        //ByteArrayInputStream inStream = new ByteArrayInputStream(baos.toByteArray());
        //double[] result = new double[baos.size()];
//
        //try {
        //    result = SoundDataUtils.readStreamAsDoubleArray(inStream, baos.size());
        //} catch (Exception e) {
        //}

        double[] result = SoundDataUtils.load16BitPCMRawDataFileAsDoubleArray(file1);

        difference_calculated = Detect.isit(result, result_snipped);

        return difference_calculated;
    }


    private void distance(double difference_received) {

        while(difference_calculated == 0){
            distance(difference_received);
        }
        double difference_all = Math.abs(difference_received - difference_calculated);
        double constantDistance = 0.00;
        double speedOfSound = 343.2;

        double endergebnis = ((difference_all) / 44100) * speedOfSound / 2 + constantDistance;

        if(endergebnis > 15.0){
            endergebnis = 15.0;
        }else if(endergebnis< 0){
            endergebnis = 0;
        }

        final TextView textViewToChange = (TextView) findViewById(R.id.textViewCorr);

        String newtext = Double.toString(endergebnis);

        textViewToChange.setText(newtext);
    }

    private class RecordingRunnable implements Runnable {

        @Override
        public void run() {

            final File file = new File(Environment.getExternalStorageDirectory(), "recording.pcm");
            final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            try (final FileOutputStream outStream = new FileOutputStream(file)) {
                while (recordingInProgress.get()) {
                    int result = recorder.read(buffer, BUFFER_SIZE);
                    if (result < 0) {
                        throw new RuntimeException("Reading of audio buffer failed: " +
                                getBufferReadFailureReason(result));
                    }
                    outStream.write(buffer.array(), 0, BUFFER_SIZE);
                    buffer.clear();
                }
            } catch (IOException e) {
                throw new RuntimeException("Writing of recorded audio failed", e);
            }
        }
        /*

            //final File file = new File(Environment.getExternalStorageDirectory(), "recording.pcm");
            final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);


            //try (final FileOutputStream outStream = new FileOutputStream(file)) {


            while (recordingInProgress.get()) {
                int result = recorder.read(buffer, BUFFER_SIZE);
                if (result < 0) {
                    throw new RuntimeException("Reading of audio buffer failed: " +
                            getBufferReadFailureReason(result));
                }
                //dooooooooomed
                //outStream.write(buffer.array(), 0, BUFFER_SIZE);
                //buffer.array().get(bytes,0,BUFFER_SIZE);
                baos.write(buffer.array(), 0, BUFFER_SIZE);
                //byte_size++;
                buffer.clear();
            }


            //} catch (IOException e) {
            //throw new RuntimeException("Writing of recorded audio failed", e);
            //}
        }*/

        private String getBufferReadFailureReason(int errorCode) {
            switch (errorCode) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    return "ERROR_INVALID_OPERATION";
                case AudioRecord.ERROR_BAD_VALUE:
                    return "ERROR_BAD_VALUE";
                case AudioRecord.ERROR_DEAD_OBJECT:
                    return "ERROR_DEAD_OBJECT";
                case AudioRecord.ERROR:
                    return "ERROR";
                default:
                    return "Unknown (" + errorCode + ")";
            }
        }
    }

    private void sometry(String temp) {


        switch (temp) {
            case "start":
                //difference_calculated = 0;
                delaythemedia = false;

                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                startRecording();
                break;
            case "stop":
                delaythemedia = true;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);

                stopRecording();
                break;
            case "calc":
                //distance();
                break;
            default:
                double difference = Double.parseDouble(temp);
                distance(difference);
                break;
        }


    }



}
