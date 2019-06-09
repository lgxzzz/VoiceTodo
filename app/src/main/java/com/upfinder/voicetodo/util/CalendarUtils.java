package com.upfinder.voicetodo.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import com.upfinder.voicetodo.utils.UtilsKt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * @author: ywj
 * @date: 2019/2/23 15:54
 */
public class CalendarUtils {
//    private static String CALENDAR_URL = "content://com.android.calendar/calendars";
//    private static String CALENDAR_EVENT_URL = "content://com.android.calendar/events";
//    private static String CALENDAR_REMINDER_URL = "content://com.android.calendar/reminders";

    private static final String CALENDARS_NAME = "语音提醒器";
    private static final String CALENDARS_ACCOUNT_NAME = "语音提醒器";
    private static final String CALENDARS_ACCOUNT_TYPE = "com.upfinder.voicetodo";
    private static final String CALENDARS_DISPLAY_NAME = "语音提醒器";


    /**
     * 添加日历事件
     *
     * @param context
     * @param title             标题
     * @param description       描述
     * @param beginTimeMillis   开始毫秒值
     * @param endTimeMillis     结束毫秒值
     * @param repeatPeriod     重复周期 天
     * @param color             颜色
     * @param advanceMinutes    提前提醒分钟
     * @return
     */
    public static boolean insertCalendarEvent(Context context,
                                              String title,
                                              String description,
                                              long beginTimeMillis,
                                              long endTimeMillis,
                                              int repeatPeriod,
                                              long advanceMinutes,
                                              long color) {

        if (context == null) {
            UtilsKt.logE("context is null");
            return false;
        } else if (TextUtils.isEmpty(title) && TextUtils.isEmpty(description)) {
            UtilsKt.logE("title or description isEmpty");
            return false;
        }

        // 获取日历账户的id
        long acountId = checkAndAddCalendarAccount(context);

        if (acountId < 0) {
            UtilsKt.logE("can't get CalendarAccount");
            return false;
        }

        try {
            // 插入日程
            ContentValues eventValues = new ContentValues();
            eventValues.put(CalendarContract.Events.DTSTART, beginTimeMillis);
            eventValues.put(CalendarContract.Events.DTEND, endTimeMillis);
            eventValues.put(CalendarContract.Events.TITLE, title);
            eventValues.put(CalendarContract.Events.DESCRIPTION, description);
            eventValues.put(CalendarContract.Events.CALENDAR_ID, acountId);
            eventValues.put(CalendarContract.Events.EVENT_COLOR, color);//0xFFFF0000
//            eventValues.put(CalendarContract.Events.HAS_ALARM, 1);
            eventValues.put(CalendarContract.Events.RRULE, "FREQ=DAILY;INTERVAL="+repeatPeriod); //重复规则 https://blog.csdn.net/weixin_33676492/article/details/86936880
            eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            Uri eUri = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, eventValues);
            long eventId = ContentUris.parseId(eUri);

            if (eventId == 0) {
                // 插入失败
                UtilsKt.logE("insert events fail");
                return false;
            }

            //插入提醒
            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            reminderValues.put(CalendarContract.Reminders.MINUTES, advanceMinutes);
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri rUri = context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
            if (rUri == null || ContentUris.parseId(rUri) == 0) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再次进行查询
     * 获取账户成功返回账户id，否则返回-1
     */
    private static long checkAndAddCalendarAccount(Context context) {
        long accountId = checkCalendarAccount(context);
        //如果不存在appaccount
        if (accountId < 0) {
            accountId = addCalendarAccount(context);
        }
        //如果 创建appaccount失败
        if (accountId < 0) {
            accountId = queryDefaultCalendarAccount(context);
        }

        return accountId;
    }

    /**
     * 检查现在是否已经存在日历账户
     *
     * @param context
     * @return
     */
    public static int checkCalendarAccount(Context context) {
        String[] CALENDARS_COLUMNS = new String[]{CalendarContract.Calendars._ID,};
        int calendarId = -1;
        StringBuffer selection = new StringBuffer(" 1 = 1 ");
        List<String> selectionArgs = new ArrayList<String>();
        selection.append(" AND " + CalendarContract.Calendars.ACCOUNT_NAME + " = ? ");
        selectionArgs.add(CALENDARS_NAME);
        Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, CALENDARS_COLUMNS, selection.toString(),
                selectionArgs.toArray(new String[]{}), null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                calendarId = Integer.parseInt(cursor.getString(0));
            }
            cursor.close();
        }

        return calendarId;
    }

    /**
     * 查询默认日历账户
     *
     * @param context
     * @return
     */
    public static int queryDefaultCalendarAccount(Context context) {
        int acountId = -1;
        Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            try {

                int count = cursor.getCount();
                // 存在现有账户，取第一个账户的id返回
                if (count > 0) {
                    cursor.moveToFirst();
                    acountId = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID));
                }
            } finally {
                cursor.close();
            }
        }
        return acountId;
    }

    /**
     * 添加日历账户
     *
     * @param context
     * @return
     */
    private static long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }


    /**
     * 删除日历事件
     *
     * @param context
     * @return
     */
    public static void deleteCalendarEvent(Context context, String title) {
        if (context == null) {
            return;
        }
        Cursor eventCursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                null, null, null, null);
        try {
            if (eventCursor == null) { // 查询返回空值
                return;
            }
            if (eventCursor.getCount() > 0) {
                // 遍历所有事件，找到title跟需要查询的title一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
                        int id = eventCursor.getInt(eventCursor
                                .getColumnIndex(CalendarContract.Calendars._ID)); // 取得id
                        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) { // 事件删除失败
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
    }


    static class EventModel {
        String id;
        String time;
        String content;

        public EventModel() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * 根据账户查询账户日历
     *
     * @return List
     */
    public static List<EventModel> queryEvents(Context context) {
        List<EventModel> calendars = new ArrayList<>();
        Cursor cursor;
        // 本地帐户查询：ACCOUNT_TYPE_LOCAL是一个特殊的日历账号类型，它不跟设备账号关联。这种类型的日历不同步到服务器
        // 如果是谷歌的账户是可以同步到服务器的
//        cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, CalendarsResolver.EVENTS_COLUMNS,
//                CalendarContract.Calendars.ACCOUNT_NAME + " = ? ", new String[]{CalendarContract.Calendars.ACCOUNT_NAME}, null);
        cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, CalendarsResolver.EVENTS_COLUMNS,
                null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            EventModel eventModel = new EventModel();
            eventModel.setId(cursor.getString(0));
            eventModel.setTime(cursor.getString(5));
            eventModel.setContent(cursor.getString(2) + "-" + cursor.getString(3));
            calendars.add(eventModel);
        }
        return calendars;
    }

    /**
     * 更新某条Event
     *
     * @param model model
     */
    public static void updateEvent(EventModel model, Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Events.DTSTART, model.getTime());
        contentValues.put(CalendarContract.Events.DESCRIPTION, model.getContent());
        context.getContentResolver().update(CalendarContract.Events.CONTENT_URI, contentValues
                , CalendarContract.Events._ID + " =? ", new String[]{model.getId()});
    }

    /**
     * 删除某条Event
     *
     * @param id id
     * @return The number of rows deleted.
     */
    public static int deleteEvent(Context context, String id) {
        return context.getContentResolver()
                .delete(CalendarContract.Events.CONTENT_URI, CalendarContract.Events._ID + " =? ", new String[]{id});
    }

    /**
     * 删除所有Event
     *
     * @return The number of rows deleted.
     */
    public static int deleteAllEvent(Context context) {
        return context.getContentResolver()
                .delete(CalendarContract.Events.CONTENT_URI, CalendarContract.Events.CALENDAR_ID + " =? ", new String[]{queryCalId(context)});
    }

    /**
     * 查询 calendar_id
     *
     * @return calId
     */
    private static String queryCalId(Context context) {
        Cursor userCursor = null;
        try {
            userCursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null,
                    "name=?", new String[]{CalendarContract.Calendars.ACCOUNT_NAME}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (userCursor == null) {
            return null;
        }
        if (userCursor.getCount() > 0) {
            userCursor.moveToLast(); //是向符合条件的最后一个账户添加
            return userCursor.getString(userCursor.getColumnIndex("_id"));
        }
        return "";
    }


    /**
     * 检查版本是否低于Api 14   4.0
     *
     * @return
     */
    private boolean checkVersionSdk() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return true;
        }
        return false;
    }
//    /**
//     * 检测是否有读取日历的权限组
//     *
//     * @return
//     */
//    private boolean checkCalendarPermission(AppCompatActivity context) {
//        if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) ||
//                (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
//            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR}, 100);
//            return true;
//        }
//        return false;
//    }
//

    /**
     * 检查是否不符合条件 true 不符合
     *
     * @return
     */
    public boolean checkSdkPermission() {
        if (checkVersionSdk()) {
            return true;
        }
//        if (checkCalendarPermission()) {
//            return true;
//        }
        return true;
//        return false;
    }

    /**
     * 查询日历，获取ID，用于插入事件
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void myQueryCalendar(AppCompatActivity context) {
        long calID = -1;
        try {
            // Run query
            if (checkSdkPermission()) {
                return;
            }
            Cursor cur = null;
            ContentResolver cr = context.getContentResolver();
            Uri uri = null;
            uri = CalendarContract.Calendars.CONTENT_URI;
            cur = cr.query(uri, null, CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + ">= 700 ", null, null);
            while (cur.moveToNext()) {
                String displayName = null;
                String accountName = null;
                String ownerName = null;
                String accountType = null;
                String visible = null;
                String syncEnvents = null;

                // Get the field values
                calID = cur.getLong(cur.getColumnIndex(CalendarContract.Calendars._ID));
                displayName = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
                accountName = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME));
                ownerName = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT));
                accountType = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE));
                visible = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.VISIBLE));
                syncEnvents = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL));

                // Do something with the values...
                Log.d("TAG", "calID:" + calID + "\ndisplayName:" + displayName + "\naccountName:" + accountName + "\nownerName:" + ownerName
                        + "\naccountType:" + accountType
                        + "\nvisible:" + visible
                        + "\nCALENDAR_ACCESS_LEVEL:" + syncEnvents);
                break;
            }
            if (calID < 0) {
                return;
            }
            insertEvent(context, calID);
            queryEvents(context, "test");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    /**
     * 查询所有的事件，根据传入title模糊查询
     *
     * @param title
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void queryEvents(Context context, String title) {
        if (checkSdkPermission()) {
            return;
        }
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = null;
        String selection = "(" + CalendarContract.Events.TITLE + " like ?)";
        String[] selectionArgs = new String[]{"%" + title + "%"};
        uri = CalendarContract.Events.CONTENT_URI;
        cur = cr.query(uri, null, selection, selectionArgs, null);
        long clendId = 0, eventId = 0;
        String[] names = cur.getColumnNames();
        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            long ownerName;
            long ownerName2;
            long ownerName3;

            calID = cur.getLong(cur.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
            displayName = cur.getString(cur.getColumnIndex(CalendarContract.Events.TITLE));
            accountName = cur.getString(cur.getColumnIndex(CalendarContract.Events.DESCRIPTION));
            ownerName = cur.getLong(cur.getColumnIndex(CalendarContract.Events._ID));
            ownerName2 = cur.getLong(cur.getColumnIndex(CalendarContract.Events.DTSTART));
            ownerName3 = cur.getLong(cur.getColumnIndex(CalendarContract.Events.DTEND));
            clendId = calID;
            eventId = ownerName;
            Log.d("TAG", "CALENDAR_ID:" + calID + "\nTITLE:" + displayName + "\nDESCRIPTION:" + accountName + "\n_ID:" + ownerName
                    + "\nDTSTART:" + new Date(ownerName2) + "\nDTEND:" + new Date(ownerName3));


        }
    }


    /**
     * 插入事件到calender_ID中
     *
     * @param calID
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void insertEvent(Context context, long calID) {
        if (checkSdkPermission()) {
            return;
        }
        long startMillis = 0;
        long endMillis = 0;
//        Calendar beginTime = Calendar.getInstance();
//        Date date = new Date();
//        beginTime.setTime(date);
//        beginTime.set(date.getYear(), 6, 3, 11, 11);
//        startMillis = beginTime.getTimeInMillis();
//        Calendar endTime = Calendar.getInstance();
//        endTime.set(2017, 6, 3, 12, 11);
//        endMillis = endTime.getTimeInMillis();
        startMillis = System.currentTimeMillis() + 11 * 60 * 1000;
        endMillis = startMillis + 10 * 60 * 1000;
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, "test");
        values.put(CalendarContract.Events.DESCRIPTION, "Just For  Test");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        long eventID = Long.parseLong(uri.getLastPathSegment());
        Log.e("TAG", "insert eventid:" + eventID);
        insertReminders(context, eventID);
    }

    /**
     * 根据事件ID删除
     *
     * @param envId
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void deleteEventById(Context context, long envId) {
        if (envId <= 0) {
            return;
        }
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, envId);
        int rows = context.getContentResolver().delete(deleteUri, null, null);
        Log.i("TAG", "Rows deleted: " + rows);
    }

    /**
     * 加入提醒
     *
     * @param eventID
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void insertReminders(Context context, long eventID) {
        if (checkSdkPermission()) {
            return;
        }
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.MINUTES, 10);
        values.put(CalendarContract.Reminders.EVENT_ID, eventID);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
    }
}
