package com.cardmanager.kdml.cardmanagerv3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by kdml on 2016-05-24.
 */
public class SplashActivity extends Activity{
    public static final int REQUEST_CODE_LOGIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this,MainActivity.class));
        /*
        CustomerDatabase cd = CustomerDatabase.getInstance(this);
        try
        {
            Intent intent = new Intent(getBaseContext(),EmailPasswordActivity.class);
            Bundle bnd = new Bundle();
            bnd.putInt("loginFlag",1);
            intent.putExtras(bnd);
            startActivityForResult(intent,REQUEST_CODE_LOGIN);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }
    protected void onActivityResult(int requestCode,int resultCode,Intent Data){
        super.onActivityResult(requestCode,resultCode,Data);

        switch (requestCode)
        {
            case REQUEST_CODE_LOGIN:
                if(resultCode == RESULT_OK)
                {
                    int i = Data.getExtras().getInt("data");
                    if(i == 1)
                    {
                        startActivity(new Intent(this,MainActivity.class));
                        finish();
                    }
                }

                break;
        }
    }
}
