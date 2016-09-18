package com.cardmanager.kdml.cardmanagerv3;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.cardmanager.kdml.cardmanagerv3.DTO.CostData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kdml on 2016-09-13.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    CustomerDatabase cd = null;
    private DatabaseReference mDatabase;
    @Override
    public void onReceive(Context context, Intent intent) {
        String i = intent.getAction();
        if("android.provider.Telephony.SMS_RECEIVED".equals(i))
        {
            Log.d("KDMsss","SMS Received...........................................");
            cd = CustomerDatabase.getInstance(context);
            setSMSData(context);
            setFBData(context);
        }

    }
    private void setFBData(Context context)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null)
            return;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Cursor cs = null;
        try{
            Calendar cal = Calendar.getInstance();
            String yyyy = String.valueOf(cal.get(Calendar.YEAR));
            String MM = String.valueOf((cal.get(Calendar.MONTH) + 1));

            String sql = "select cardName , sum(cost) from "+cd.TABLE_SMS_DATA+" where month = "+MM+" and  year = "+yyyy+" group by cardName";
            sql = "select cardName , sum(cost),year,month,customer.CUSTOMER_EMAIL,customer.CUSTOMER_NAME,customer.CUSTOMER_PHONE,customer.FireBase_ID from "+cd.TABLE_SMS_DATA+" sms\n" +
                    "left join "+cd.TABLE_CUSTOMER_INFO+" customer\n" +
                    "where month = "+MM+" and year = "+yyyy+" group by cardName";
            cs = cd.rawQuery(sql);
            if(cs == null)
                return;

            Map<String,HashMap> maptest = new HashMap<>();

            if(cs.moveToFirst()){
                while(cs.moveToNext()){
                    //cardInfoArrayList.add(new CardInfo(cs));
                    if(cs.getCount() > 0)
                    {
                        DecimalFormat fmt=new DecimalFormat("##,###");
                        //String cost = String.valueOf(cs.getString(1));
                        //String cardName, String yearMonth, String cost, String email, String name, String phone

                        String cardNameconvert = cs.getString(0).replace("[","(").replace("]",")");
                        CostData cdata = new CostData(cardNameconvert,cs.getString(2)+cs.getString(3),cs.getString(1),cs.getString(4),cs.getString(5),cs.getString(6));
                        Log.d("KDMsss", cardNameconvert+"^"+cs.getString(2)+cs.getString(3)+"^"+cs.getString(1)+"^"+cs.getString(4)+"^"+cs.getString(5)+"^"+cs.getString(6));
                        if(maptest.containsKey(cdata.getYearMonth()))
                        {
                            HashMap<String,CostData> submap = maptest.get(cdata.getYearMonth());
                            submap.put(cdata.getCardName(),cdata);
                        }
                        else
                        {
                            HashMap<String,CostData> submap = new HashMap<>();
                            submap.put(cdata.getCardName(),cdata);
                            maptest.put(cdata.getYearMonth(),submap);
                        }
                    }
                }
            }
            if(user.getUid().length() > 0)
                FirebaseDatabase.getInstance().getReference().child("costs").child(user.getUid()).setValue(maptest);
        }
        catch(Exception ex)
        {
            Log.e("KDMsss", "Exception in setTableCustomerInfo()", ex);

        }
        finally {
            if(cs != null)
                cs.close();
        }

    }


    private void setSMSData(Context context)
    {
        Cursor cs = null;
        try
        {
            // DB 확인.
            if(!cd.open())
            {
                // db 열기 실패, 종료
                Log.e("KDMsss", "db 열기 실패");
                return;
            }
            // db 업데이트
            //cd.onUpdateDatabase();

            String query = "select dataTime from "+cd.TABLE_SMS_DATA+" order by dataTime desc limit 1";
            cs = cd.rawQuery(query);
            cs.moveToFirst();
            if(cs.getCount() == 0)
            {
                Log.d("KDMsss", "마지막 저장된 정보 없음.");
            }
            else
            {
                String lastData = cs.getString(0);
                Log.d("KDMsss", "lastData = "+lastData);
            }
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

        Cursor c = null;
        try
        {
            String selection = "address = ? or address = ?";
            String[] selectionArgs = {"15776200","15447200"};
            String sortOrder = "date ASC";
            // SMS 문자 데이터를 전부 읽어옴
            ContentResolver cr = context.getApplicationContext().getContentResolver();
            c = cr.query(Uri.parse("content://sms/inbox"), null, selection, selectionArgs, sortOrder);
            if(c.moveToFirst()){
                while(c.moveToNext()){
                    try {
                        String strAdd = c.getString(c.getColumnIndex("address"));
                        String date = c.getString(c.getColumnIndex("date"));
                        Log.d("KDMsss", "strAdd = " + strAdd + "date = " + date);
                        long origin = (long) Double.parseDouble(date);
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(origin);
                        int yyyy = cal.get(Calendar.YEAR);
                        int MM = (cal.get(Calendar.MONTH) + 1);
                        int dd = cal.get(Calendar.DATE);
                        int HH = cal.get(Calendar.HOUR);
                        int mm = cal.get(Calendar.MINUTE);
                        int ss = cal.get(Calendar.SECOND);
                        boolean flag = false;
                        String cardName = "", won = "", type = "";
                        String str = c.getString(c.getColumnIndex("body"));
                        if (strAdd.equals("15776200")) { // 현대카드
                            String strCnvert = str.replace("\r", "");
                            String[] spl = strCnvert.split("\\n");
                            won = getNum(spl[3].substring(0, spl[3].indexOf("원")));

                            type = "승인";
                            if (spl[1].contains("취소"))
                                type = "취소";

                            cardName = spl[1].substring(0, spl[1].indexOf("]")).replace("[", "");
                            flag = true;
                        }
                        if (strAdd.equals("15447200")) { // 신한카드
                            String[] spl2 = str.split("\\n");
                            spl2 = spl2[1].split(" ");
                            won = getNum(spl2[4]);

                            type = "승인";
                            if (spl2[0].contains("취소"))
                                type = "취소";

                            cardName = "신한카드" + spl2[1];
                            flag = true;
                        }

                        if (flag) {
                            String dateConvert = yyyy + "" + (MM < 10 ? "0" + MM : MM) + "" + (dd < 10 ? "0" + dd : dd) + " " + HH + ":" + mm + ":" + ss + "";
                            ContentValues values = new ContentValues();
                            values.put("cardName", cardName);
                            values.put("dataTime", date);
                            values.put("cost", won);
                            values.put("text", str);
                            values.put("dateTimeConvert", dateConvert);
                            values.put("company", strAdd);
                            values.put("year", yyyy);
                            values.put("month", MM);
                            values.put("day", dd);
                            values.put("type", type);

                            if (!cd.insert(cd.TABLE_SMS_DATA, values))
                                Log.d("KDMsss", "DB insert (TABLE_SMS_DATA) 실패 = " + strAdd + "date = " + date);
                        }
                    }
                    catch (Exception ex)
                    {
                        Log.d("KDMsss", "SMS 파싱 실패");
                    }
                }
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e("KDMsss", "Exception in setTableCustomerInfo()", ex);
        }
        finally {
            if(c != null)
                c.close();
        }
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
