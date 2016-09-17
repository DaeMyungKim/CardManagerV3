package com.cardmanager.kdml.cardmanagerv3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cardmanager.kdml.cardmanagerv3.DTO.CostData;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ListView listView1;
    IconTextListAdapter adapter;
    //private DatabaseReference mDatabase;
    HashMap<String,CostData> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refresh...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                onResume();
            }
        });

        checkSMSPermissions();

        // 리스트뷰 객체 참조
        listView1 = (ListView) findViewById(R.id.listView1);
    }

    private void checkSMSPermissions(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"권한 있음",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this,"권한 없음",Toast.LENGTH_LONG).show();

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)){
                Toast.makeText(this,"권한 설명 필요함.",Toast.LENGTH_LONG).show();
            }
            else{
                String[] permissions = {Manifest.permission.RECEIVE_SMS};

                ActivityCompat.requestPermissions(this,permissions,1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            for(int i = 0; i < permissions.length; i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,permissions[i]+"권한 승인됨.",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this,permissions[i]+"권한 승인되지 않음.",Toast.LENGTH_LONG).show();
                }

            }
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onResume() {
        super.onResume();
        setListData();
        //setData();
    }
    /*
    public void setData()
    {
        if(map==null)
            map =new HashMap<String, CostData>();
        else
            map.clear();

        mDatabase = FirebaseDatabase.getInstance().getReference("costs");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    try{
                        String key = snapshot.getKey();
                        Object obj = snapshot.getValue(Object.class);
                        String[] spl = obj.toString().split("=\\{");
                        Log.d("kdml",key+"  "+spl[0]+"  "+spl[1]);
                        CostData cd = snapshot.getValue(CostData.class);
                        Log.d("kdml",cd.getCardName()+cd.getDateTime()+cd.getYearMonth()+cd.getYearMonth()+cd.getCardName()+cd.getCost());
                        //CostData lcd = map.get(cd.getCardName());
                        /*
                        if(lcd != null)
                        {
                            if(Long.valueOf(lcd.getDateTimeOrigin())<Long.valueOf(cd.getDateTimeOrigin()))
                            {
                                map.remove(lcd);
                                map.put(cd.getCardName(),cd);
                            }
                        }
                        else
                        //map.put(cd.getCardName()+cd.getEmail(),cd);

                    }
                    catch (ClassCastException e)
                    {
                        e.printStackTrace();
                    }
                }
                setListData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("kdml", "Failed to read value. : " + databaseError.toException());
            }
        });

    }
*/
    public void setListData()
    {
        CustomerDatabase cd ;
        Cursor cs = null;
        try {
            cd = CustomerDatabase.getInstance(this);
            // DB 확인.
            if (!cd.open()) {
                // db 열기 실패, 종료
                Log.e("KDMsss", "db 열기 실패");
                return;
            }

            if(map==null)
                map =new HashMap<String, CostData>();
            else
                map.clear();

            adapter = new IconTextListAdapter(this);

            // 아이템 데이터 만들기
            Resources res = getResources();
            String fmt=new DecimalFormat("##,###").format(23423.44);

            Calendar cal = Calendar.getInstance();
            String yyyy = String.valueOf(cal.get(Calendar.YEAR));
            String MM = String.valueOf((cal.get(Calendar.MONTH) + 1));

            String query = "select cardName,sum(cost) from TABLE_SMS_DATA where month = '"+MM+"' and year = '"+yyyy+"' and type = '승인' group by cardName";
            cs = cd.rawQuery(query);
            if(cs.moveToFirst()){

                adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon06), cs.getString(0), yyyy+"년 "+MM+"월", new DecimalFormat("##,###").format(cs.getDouble(1))+"원"));
                while(cs.moveToNext()){
                    adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon06), cs.getString(0), yyyy+"년 "+MM+"월", new DecimalFormat("##,###").format(cs.getDouble(1))+"원"));
                }
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e("KDMsss", "Exception in setListData()", ex);
        }
        finally {
            if(cs != null)
                cs.close();
        }



        /*
        for(CostData cd : map.values())
        {
            //adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon05), cd.getCardName(), cd.getEmail(), cd.getCost()));
        }
*/

        /*
        adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon05), "친구찾기 (Friends Seeker)", "300,000 다운로드", "900 원"));
        adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon06), "강좌 검색", "120,000 다운로드", "900 원"));
        adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon05), "지하철 노선도 - 서울", "4,000 다운로드", "1500 원"));
        adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon06), "지하철 노선도 - 도쿄", "6,000 다운로드", "1500 원"));
        adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon05), "지하철 노선도 - LA", "8,000 다운로드", "1500 원"));
        adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon06), "지하철 노선도 - 워싱턴", "7,000 다운로드", "1500 원"));
        adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon05), "지하철 노선도 - 파리", "9,000 다운로드", "1500 원"));
        adapter.addItem(new IconTextItem(res.getDrawable(R.drawable.icon06), "지하철 노선도 - 베를린", "38,000 다운로드", "1500 원"));
*/
        // 리스트뷰에 어댑터 설정
        listView1.setAdapter(adapter);

        // 새로 정의한 리스너로 객체를 만들어 설정
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IconTextItem curItem = (IconTextItem) adapter.getItem(position);
                String[] curData = curItem.getData();

                Toast.makeText(getApplicationContext(), "Selected : " + curData[0], Toast.LENGTH_LONG).show();
            }

        });
    }
}
