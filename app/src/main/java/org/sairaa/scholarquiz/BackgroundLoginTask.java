package org.sairaa.scholarquiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class BackgroundLoginTask extends AsyncTask<String, Void, String> {
    private final String REGISTER_URL = "http://sairaa.org/ScholarQuiz/register_test.php";
    Context context;
    Activity activity;
    ProgressDialog progressDialog;
    AlertDialog.Builder alertBuilder;
    public BackgroundLoginTask(Context ctx) {
        this.context = ctx;
        activity = (Activity)ctx;
    }

    @Override
    protected void onPreExecute() {
        alertBuilder = new AlertDialog.Builder(activity);
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Connecting to Server");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }


    @Override
    protected String doInBackground(String... params) {
        String method = params[0];

        if(method.equals("register")){
            try {
                URL register_url = new URL(REGISTER_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) register_url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

                String name = params[1];
                String email = params[2];
                String slack = params[3];
                String password = params[4];
                String info = params[5];
                Log.i("background",info);
                String data = URLEncoder.encode("user_name", "UTF-8")+"="+URLEncoder.encode(name,"UTF-8")+"&"+
                        URLEncoder.encode("mail_id", "UTF-8")+"="+URLEncoder.encode(email,"UTF-8")+"&"+
                        URLEncoder.encode("slack_id", "UTF-8")+"="+URLEncoder.encode(slack,"UTF-8")+"&"+
                        URLEncoder.encode("password", "UTF-8")+"="+URLEncoder.encode(password,"UTF-8")+"&"+
                        URLEncoder.encode("info", "UTF-8")+"="+URLEncoder.encode(info,"UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";

                while((line = bufferedReader.readLine())!= null){
                    stringBuilder.append(line+"\n");
                    Log.i("background",line);
                }
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String json) {

        try {
            progressDialog.dismiss();
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
            JSONObject object = jsonArray.getJSONObject(0);
            String code = object.getString("code");
            String message = object.getString("message");

            if(code.equals("reg_true")){
                showDialog("Registration Successfull",message,code);
            }else if(code.equals("reg_false")){
                showDialog("Registration Failed",message,code);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showDialog(String result_message, String message, String code) {

        alertBuilder.setTitle(result_message);
        alertBuilder.setMessage(message);
        if(code.equals("reg_true")||code.equals("reg_false")){
            alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    activity.finish();
                }
            });

            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();
        }

    }
}
