package com.dzcx.core.log.logdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.text.TextUtils;
import android.util.Log;

import com.dzcx.core.log.LogService;
import com.dzcx.core.log.bean.MsgModel;
import com.dzcx.core.log.config.Constants;
import com.dzcx.core.log.config.EventType;
import com.dzcx.core.log.loginterface.ILogStore;
import com.dzcx.core.log.utils.ALog;
import com.dzcx.core.log.utils.LogFileUtils;
import com.dzcx.core.log.utils.SystemUtils;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chen3 on 2017/9/14.
 */

public class LogDBManager implements ILogStore {
    private final static String TAG = "LogDBManager";
    private static final long DATE_TIME = 24 * 60 * 60 * 1000;
    private static LogDBManager mManager;
    private LogDBHelper mHelper;

    private LogDBManager() {

    }

    public void init(Context context) {
        if (mHelper == null) {
            mHelper = new LogDBHelper(context, 1);
        }
    }

    public static LogDBManager getInstance() {
        if (mManager == null) {
            mManager = new LogDBManager();
        }
        return mManager;
    }

    /**
     * 存储数据到数据库
     *
     * @param list  log的集合
     * @param count 存储多少条数据
     * @return
     */
    public synchronized boolean storeLog(LinkedList<MsgModel> list, int count) {
        if (count <= 0) {
            return true;
        }
        SQLiteDatabase db = null;
        String filePath = null;
        try {
            db = mHelper.getWritableDatabase();
            filePath = db.getPath();
            db.beginTransaction();
            ContentValues values = new ContentValues();
            while (count > 0) {
                values.clear();
                MsgModel model = list.removeFirst();
                fillValues(values, model);
                db.insert(LogDBHelper.TABLE_NAME, null, values);
                --count;
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        } catch (Exception e) {
            Log.i(TAG, "异常数据库文件的地址：" + filePath);
            if (e instanceof SQLiteFullException && !TextUtils.isEmpty(filePath)) {
                deleteCacheFile(new File(filePath), -1);
            }
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private void fillValues(ContentValues values, MsgModel model) {
        values.put(LogDBHelper.ROW_LOGID, model.getId());
        values.put(LogDBHelper.ROW_MSG, model.getMsg());
        values.put(LogDBHelper.ROW_TIME, model.getFormatTime());
        values.put(LogDBHelper.ROW_TYPE, model.getType());
    }

    /**
     * 检查数据库，删除过期的数据
     */
    public synchronized void checkLog() {
        LogState logState = checkLogByDate();
        checkLogBySize(logState);
    }

    /**
     * 检查数据库，删除过期的数据
     **/
    private synchronized LogState checkLogByDate() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        LogState logState = null;
        try {
            db = mHelper.getWritableDatabase();
            db.beginTransaction();
            String time = LogDBHelper.ROW_TIME;
            cursor = db.rawQuery("select min(" + time + ") as minTime,max(" + time + ") as maxTime,min(id) as minId,max(id) as maxId from " + LogDBHelper.TABLE_NAME + " where " + LogDBHelper.ROW_TYPE + "!=" + EventType.TYPE_DEVICE_INFO, null);
            if (cursor != null && cursor.getCount() > 0) {
                String minTime = null;
                String maxTime = null;
                int minId = -1;
                int maxId = -1;
                while (cursor.moveToNext()) {
                    minTime = cursor.getString(cursor.getColumnIndex("minTime"));
                    maxTime = cursor.getString(cursor.getColumnIndex("maxTime"));
                    maxId   = cursor.getInt(cursor.getColumnIndex("maxId"));
                    minId   = cursor.getInt(cursor.getColumnIndex("minId"));
                }
                if (!TextUtils.isEmpty(minTime) && !TextUtils.isEmpty(maxTime)) {
                    String filePath = db.getPath();
                    Log.i(TAG, "数据库文件的地址：" + filePath);
                    logState = new LogState(minTime, minId, maxTime, maxId, filePath);
                    String minTimeShort = minTime.split(" ")[0];
                    String maxTimeShort = maxTime.split(" ")[0];
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date minDate = format.parse(minTimeShort);
                    Date maxDate = format.parse(maxTimeShort);
                    final int STORE_DAY = 7;
                    long currentMinTime = maxDate.getTime() - DATE_TIME * STORE_DAY;
                    if (currentMinTime > minDate.getTime()) {
                        //不删除设备信息
                        String currentMinTimeStr = format.format(new Date(currentMinTime));
                        db.delete(LogDBHelper.TABLE_NAME, time + " < ? and " + LogDBHelper.ROW_TYPE + "!=?", new String[]{currentMinTimeStr, EventType.TYPE_DEVICE_INFO + ""});
                    }
                    deleteCacheFile(new File(filePath), currentMinTime);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            ALog.i("checkLog error:" + e.getMessage() + "," + e.getClass());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return logState;
    }

    private synchronized void checkLogBySize(LogState logState) {
        if (logState == null) {
            return;
        }
        SQLiteDatabase db = null;
        File logFile = new File(logState.getFilePath());
        Log.i(TAG, "需要检查的文件的路径是：" + logFile.getAbsolutePath());
        try {
            //判断文件的大小是否在规定范围之内
            if (logFile != null && logFile.exists() && logFile.length() > Constants.MAX_SIZE_FILE) {
                SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                Date minFullDate = fullFormat.parse(logState.getStartTime());
                Date maxFullDate = fullFormat.parse(logState.getEndTime());
                Log.i(TAG, "压缩前文件的大小是：" + logFile.length());
                String zipFilePath = LogFileUtils.compressToZip(new File[]{logFile}, logFile.getParentFile().getAbsolutePath(), minFullDate.getTime() + "_" + maxFullDate.getTime());
                Log.i(TAG, "压缩后的文件的地址：" + zipFilePath);
                db = mHelper.getWritableDatabase();
                db.beginTransaction();
                if (TextUtils.isEmpty(zipFilePath)) {
                    //压缩文件失败：删除一半的数据，然后添加 一条标识的数据
                    Log.i(TAG, "minId:" + logState.getMinId() + ",maxId:" + logState.getMaxId());
                    //删除数据库一半的数据
                    if (logState.getMinId() > -1 && logState.getMaxId() > -1) {
                        db.delete(LogDBHelper.TABLE_NAME, LogDBHelper.ROW_TYPE + "!=? and id <?", new String[]{EventType.TYPE_DEVICE_INFO + "", (logState.getMaxId() + logState.getMinId()) / 2 + ""});
                        MsgModel model = new MsgModel();
                        model.setType(EventType.TYPE_DELETE);
                        StringBuilder builder = new StringBuilder();
                        builder.append("{\"type\":\"delete\",\"msg\":{\"size\":");
                        builder.append(logFile.length());
                        builder.append(",\"totalSize\":");
                        builder.append(SystemUtils.getRomAvailableSize());
                        builder.append("}}");
                        model.setMsg(builder.toString());
                        ContentValues values = new ContentValues();
                        fillValues(values, model);
                        db.insert(LogDBHelper.TABLE_NAME, null, values);
                    }
                } else {
                    //压缩成功 ，清除数据库中的数据
                    db.delete(LogDBHelper.TABLE_NAME, LogDBHelper.ROW_TYPE + "!=?", new String[]{EventType.TYPE_DEVICE_INFO + ""});
                }
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 删除缓存的文件
     *
     * @param logFile
     * @param currentTime
     * @return 返回删除文件的个数
     */
    private int deleteCacheFile(final File logFile, final long currentTime) {
        final File parentFile = logFile.getParentFile();
        File[] files = parentFile.listFiles(pathname -> {
            Pattern pattern = Pattern.compile(Constants.REGEX_PATTERN);
            String fileName = pathname.getName();
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches()) {
                if (currentTime < 0) {
                    return true;
                }
                String[] split = fileName.split("\\.")[0].split("_");
                if (split.length == 2) {
                    String minTime = split[1];
                    if (minTime.compareTo(currentTime + "") < 0) {
                        return true;
                    }
                }
            }
            return false;
        });
        int count = 0;
        if (files != null && files.length > 0) {
            for (File file : files) {
                file.delete();
                ++count;
            }
        }
        return count;
    }

    public void storeDeviceInfo() {
        SQLiteDatabase db = null;
        Cursor     cursor = null;
        try {
            db = mHelper.getWritableDatabase();
            db.beginTransaction();
            cursor = db.query(LogDBHelper.TABLE_NAME, null, LogDBHelper.ROW_TYPE + "=?", new String[]{EventType.TYPE_DEVICE_INFO + ""}, null, null, null);
            if (cursor != null && cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                MsgModel      model  = MsgModel.createDeviceInfoMsg();
                fillValues(values, model);
                db.insert(LogDBHelper.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            ALog.i("storeDeviceInfo error:" + e.getMessage() + "," + e.getClass());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }
}
