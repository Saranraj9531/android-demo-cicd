package com.sparkout.chat.common.workmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.opencsv.CSVReader;

import org.greenrobot.eventbus.EventBus;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nivetha S on 08-03-2021.
 */
public class ImportWorker extends Worker {

    ArrayList<String> dataList = new ArrayList<>();

    public ImportWorker(@NonNull Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    String mTableName;

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {
        mTableName = getInputData().getString("Table");
        Log.e("Nivi ", "doWork: " + mTableName);
        importData();
        return new Result.Success();
    }


    public void importData() {

        /*try {
            CSVReader csvReader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath() + "/BeeBushDB/" + tableName + ".csv"));
            String[] nextLine = new String[0];
            int count = 0;
            StringBuilder columns = new StringBuilder();
            StringBuilder value = new StringBuilder();

            do {
                // nextLine[] is an array of values from the line
                nextLine = csvReader.readNext();
                Log.e("Nivi ", "importData:NextLine " + nextLine);
                for (int i = 0; i <= nextLine.length - 1; i++) {
                    if (count == 0) {
                        if (i == nextLine.length - 1) {
                            columns.append(nextLine[i]);
                            count = 1;
                        } else {
                            columns.append(nextLine[i]).append(",");
                        }
                    } else {
                        if (i == nextLine.length - 1) {
                            value.append("'").append(nextLine[i]).append("'");
                            count = 2;
                            EventBus.getDefault().post(new ImportDB(columns, value, tableName));
                            value = new StringBuilder();

                        } else
                            value.append("'").append(nextLine[i]).append("',");
                    }
                }


            } while (nextLine != null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        try {
            // Create an object of filereader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath() + "/BeeBushDB/" + mTableName + ".csv");
            try (CSVReader reader = new CSVReader(filereader)) {
                List<String[]> r = reader.readAll();
                for (String[] content :
                        r) {
                    dataList.add(Arrays.toString(content));
                }
            }
            String mCol = dataList.get(0);
            String mColAfterReplace = mCol.replaceAll("\\[|\\]", "");
            if (mTableName.equals("Chat")) {
                mColAfterReplace = mColAfterReplace + ", " + "isPlaying, " + "isPaused";
                for (int i = 1; i < dataList.size(); i++) {
                    String mStr = dataList.get(i);
                    String mNewVal = mStr.replaceAll("\\[|\\]", "");
                    String[] mSplitValue = mNewVal.split(",");
                    StringBuilder mStringBuilder = new StringBuilder();
                    for (int j = 0; j < mSplitValue.length; j++) {
                        if (mSplitValue.length - 1 == j) {
                            mStringBuilder.append("'").append(mSplitValue[j].trim()).append("',").append("'").append("false").append("',").append("'").append("false").append("'");
                        } else {
                            mStringBuilder.append("'").append(mSplitValue[j].trim()).append("',");
                        }
                    }
                    EventBus.getDefault().post(new ImportDB(mColAfterReplace, mStringBuilder, mTableName));
                }
            } else {
                for (int i = 1; i < dataList.size(); i++) {
                    String mStr = dataList.get(i);
                    String mNewVal = mStr.replaceAll("\\[|\\]", "");
                    String[] mSplitValue = mNewVal.split(",");
                    StringBuilder mStringBuilder = new StringBuilder();
                    for (int j = 0; j < mSplitValue.length; j++) {
                        if (mSplitValue.length - 1 == j) {
                            mStringBuilder.append("'").append(mSplitValue[j].trim()).append("'");
                        } else {
                            mStringBuilder.append("'").append(mSplitValue[j].trim()).append("',");
                        }
                    }
                    EventBus.getDefault().post(new ImportDB(mColAfterReplace, mStringBuilder, mTableName));
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
