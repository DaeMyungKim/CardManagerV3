package com.cardmanager.kdml.cardmanagerv3;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by kdml on 2016-09-13.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String i = intent.getAction();
        if("android.provider.Telephony.SMS_RECEIVED".equals(i))
        {
            Log.d("KDMsss","SMS Received...........................................");
            setSMSData(context);
        }

    }

    private void setSMSData(Context context)
    {
        CustomerDatabase cd ;
        Cursor cs = null;
        try
        {
            cd = CustomerDatabase.getInstance(context);
            // DB 확인.
            if(!cd.open())
            {
                // db 열기 실패, 종료
                Log.e("KDMsss", "db 열기 실패");
                return;
            }
            String query = "select dataTime from "+cd.TABLE_SMS_DATA+" order by dataTime desc limit 1";
            cs = cd.rawQuery(query);
            cs.moveToNext();
            int lastData = cs.getInt(0);
            Log.e("KDMsss", "lastData = "+lastData);
            if(cs.getCount() == 0)
            {
                Log.e("KDMsss", "마지막 저장된 정보 없음.");
                // 전체 메시지를 인서트
                //setSMSDataToDataBase(context.getApplicationContext().getContentResolver(),lastData+"");
            }
            String selection = "address = ?";
            String[] selectionArgs = {""};
            String sortOrder = "date ASC";
            // SMS 문자 데이터를 전부 읽어옴
            ContentResolver cr = context.getApplicationContext().getContentResolver();
            Cursor c = cr.query(Uri.parse("content://sms/inbox"), null, selection, selectionArgs, sortOrder);
            if(c.moveToFirst()){
                while(c.moveToNext()){
                    String strAdd = c.getString(c.getColumnIndex("address"));

                    try {
                        String date = c.getString(c.getColumnIndex("date"));
                        long origin = (long) Double.parseDouble(date);
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(origin);
                        int yyyy = cal.get(Calendar.YEAR);
                        int MM = (cal.get(Calendar.MONTH) + 1);
                        int dd = cal.get(Calendar.DATE);
                        int HH = cal.get(Calendar.HOUR);
                        int mm = cal.get(Calendar.MINUTE);
                        int ss = cal.get(Calendar.SECOND);
                        String cardName = "",won = "";
                        String str = c.getString(c.getColumnIndex("body"));
                        if (strAdd.equals("15776200")) { // 현대카드
                            String[] spl = str.split("\\n");
                            won = getNum(spl[3].split(" ")[0]);
                            cardName = "현대카드"+ spl[1].substring(0, spl[1].indexOf(" "));
                        }
                        if (strAdd.equals("15447200")) { // 신한카드
                            String[] spl2 = str.split(" ");
                            won = getNum(spl2[4]);
                            cardName = "신한카드" + spl2[1];
                        }

                        String dateConvert = yyyy + "" + MM + "" + dd + " " + HH + ":" + mm + ":" + ss + "";
                        Log.d("shue", "body:" + str);
                        Log.d("shue", "date(origin):" + date);
                        Log.d("shue", "date:" + dateConvert);
                        Log.d("shue", "type:" + c.getString(c.getColumnIndex("type")));
                        Log.d("shue", "address:" + c.getString(c.getColumnIndex("address")));
                        ContentValues values = new ContentValues();
                        values.put("cardName",cardName);
                        values.put("dataTime",date);
                        values.put("cost",won);
                        values.put("text",str);
                        values.put("dateTimeConvert",dateConvert);
                        values.put("company",strAdd);
                        values.put("year",yyyy);
                        values.put("month",MM);
                        values.put("day",dd);
                        //CustomerDatabase.getInstance(null).insertSMSData(values);


                    } catch (Exception ex) {
                        Log.e("CardManagerClient", "Exception in getCardCostData()", ex);
                    }
                }
            }
            c.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e("KDMsss", "Exception in setTableCustomerInfo()", ex);
        }
        finally {
            if(cs != null)
                cs.close();
        }
    }
    public void setSMSDataToDataBase(ContentResolver cr,String lastData)
    {

        String selection = "address = ?";
        String[] selectionArgs = {""};
        String sortOrder = "date ASC";
        // SMS 문자 데이터를 전부 읽어옴
        Cursor c = cr.query(Uri.parse("content://sms/inbox"), null, selection, selectionArgs, sortOrder);
        if(c.moveToFirst()){
            while(c.moveToNext()){
                String strAdd = c.getString(c.getColumnIndex("address"));

                try {
                    String date = c.getString(c.getColumnIndex("date"));
                    long origin = (long) Double.parseDouble(date);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(origin);
                    int yyyy = cal.get(Calendar.YEAR);
                    int MM = (cal.get(Calendar.MONTH) + 1);
                    int dd = cal.get(Calendar.DATE);
                    int HH = cal.get(Calendar.HOUR);
                    int mm = cal.get(Calendar.MINUTE);
                    int ss = cal.get(Calendar.SECOND);
                    String cardName = "",won = "";
                    String str = c.getString(c.getColumnIndex("body"));
                    if (strAdd.equals("15776200")) { // 현대카드
                        String[] spl = str.split("\\n");
                        won = getNum(spl[3].split(" ")[0]);
                        cardName = "현대카드"+ spl[1].substring(0, spl[1].indexOf(" "));
                    }
                    if (strAdd.equals("15447200")) { // 신한카드
                        String[] spl2 = str.split(" ");
                        won = getNum(spl2[4]);
                        cardName = "신한카드" + spl2[1];
                    }

                    String dateConvert = yyyy + "" + MM + "" + dd + " " + HH + ":" + mm + ":" + ss + "";
                    Log.d("shue", "body:" + str);
                    Log.d("shue", "date(origin):" + date);
                    Log.d("shue", "date:" + dateConvert);
                    Log.d("shue", "type:" + c.getString(c.getColumnIndex("type")));
                    Log.d("shue", "address:" + c.getString(c.getColumnIndex("address")));
                    ContentValues values = new ContentValues();
                    values.put("cardName",cardName);
                    values.put("dataTime",date);
                    values.put("cost",won);
                    values.put("text",str);
                    values.put("dateTimeConvert",dateConvert);
                    values.put("company",strAdd);
                    values.put("year",yyyy);
                    values.put("month",MM);
                    values.put("day",dd);
                    //CustomerDatabase.getInstance(null).insertSMSData(values);


                } catch (Exception ex) {
                    Log.e("CardManagerClient", "Exception in getCardCostData()", ex);
                }
            }
        }
        c.close();
    }
    public String getNum(String strs)
    {
        String won="";
        for (int i = 0; i < strs.length(); i++) {
            char ch = strs.charAt(i);
            if ("0123456789".contains(ch + ""))
                won += strs.charAt(i);
        }
        return won;
    }
}
