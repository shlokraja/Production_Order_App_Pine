package com.ftl.mswipeinterface;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

// Custom logger class to write to file/normal log depending on the user inputted flag
public class MyLogger {
    private static MyLogger instance = null;
    private static String LogFileName = "Mswipe_log.txt";

    protected MyLogger() {
        // Exists only to defeat instantiation.
    }
    public synchronized static MyLogger getInstance() {
        if(instance == null) {
            instance = new MyLogger();
        }
        return instance;
    }

    public static void debug(String tag, String msg, Context ctx) {
        Log.d(tag, msg);
        writeToFile(tag, msg, ctx);
    }

    public static void error(String tag, String msg, Context ctx) {
        Log.e(tag, msg);
        writeToFile(tag, msg, ctx);
    }

    public static void info(String tag, String msg, Context ctx) {
        Log.i(tag, msg);
        writeToFile(tag, msg, ctx);
    }

    public static void clearLog(Context ctx) {
        try {
            boolean result = ctx.deleteFile(LogFileName);
            if (result) {
                Log.d("LOGGER", "Log file was successfully deleted");
            } else {
                Log.e("LOGGER", "Log file could not be deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String ReadFromFile(Context ctx)
    {
        try {
            FileInputStream fis = ctx.openFileInput(LogFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
                sb.append("\\n");
            }
            isr.close();
            bufferedReader.close();
            return sb.toString();
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    private static void writeToFile(String tag, String msg, Context ctx) {
        // write to file
        try {
            FileOutputStream outputStream;
            String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            msg = mydate + " " + tag + " " + msg;
            msg += "\n";
            outputStream = ctx.openFileOutput(LogFileName, Context.MODE_APPEND);
            outputStream.write(msg.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void writefile(String tag, String msg) {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        Log.i("writefile--","externalStorageDir");
        File myFile = new File(externalStorageDir, "OrderApplog.txt");
        Log.i("writefile--path",myFile.getPath());
        Log.i("writefile--path",myFile.getAbsolutePath());

        if (myFile.exists()) {
            try {
                Log.i("writefile--","started");
                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                msg = mydate + " " + tag + "--- " + msg;
                msg += "\n";
                String Msg=ReadFile(myFile,msg);
                Log.i("writefile--",Msg);
                FileOutputStream fostream = new FileOutputStream(myFile);
                OutputStreamWriter oswriter = new OutputStreamWriter(fostream);
                BufferedWriter bwriter = new BufferedWriter(oswriter);
                bwriter.write(Msg);
                Log.i("writefile--","writed");
                bwriter.newLine();
                bwriter.close();
                oswriter.close();
                fostream.close();
                Log.i("writefile--"," end");
            } catch (IOException e) {
                Log.i("","");
            }
        } else {
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                Log.i("createNewFile--","Error");
            }
        }
    }
    public static String ReadFile(File myFile,String msg)
    {
        try {
            Log.i("ReadFile--",msg);
            FileInputStream fis = new FileInputStream(myFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
                sb.append("\\n");
            }
            Log.i("ReadFile--before--", sb.toString());
            Log.i("ReadFile--","Read success");
            sb.append(msg);
            Log.i("ReadFile--after--", sb.toString());
            isr.close();
            bufferedReader.close();
            return sb.toString();
        }
        catch (Exception ex)
        {
            Log.i("ReadFile--","Read Error");
            return "";
        }
    }
}
