package com.keamanan_rumah.sistemkeamananrumah;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class FragmentTambahPengguna extends Fragment {

    List<NameValuePair> data_daftar = new ArrayList<NameValuePair>(9);
    boolean loaddata;
    EditText etUsername,etPassword,etNama,etAlamat;
    Spinner spSebagai;
    Button btnSimpan;
    ProgressDialog pDialog;
    LinearLayout llNotif;
    TextView tvNotif;

    String[] array_id_parent;
    String[] array_nama_parent;
    String parent_id;

    List<String> spinnerArray =  new ArrayList<String>();
    String JSON_data;

    String status_cek,message,message_severity;

    public FragmentTambahPengguna() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View inflaterTambahPengguna = inflater.inflate(R.layout.fragment_tambah_pengguna, container, false);
        llNotif = (LinearLayout) inflaterTambahPengguna.findViewById(R.id.llNotif);
        tvNotif = (TextView) inflaterTambahPengguna.findViewById(R.id.tvNotif);
        etUsername = (EditText) inflaterTambahPengguna.findViewById(R.id.etUsername);
        etPassword = (EditText) inflaterTambahPengguna.findViewById(R.id.etPassword);
        etNama = (EditText) inflaterTambahPengguna.findViewById(R.id.etNama);
        etAlamat = (EditText) inflaterTambahPengguna.findViewById(R.id.etAlamat);
        spSebagai = (Spinner) inflaterTambahPengguna.findViewById(R.id.spSebagai);
        btnSimpan = (Button) inflaterTambahPengguna.findViewById(R.id.btnSimpan);
        return inflaterTambahPengguna;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinnerArray.add("Jadikan sebagai Koordinator");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSebagai.setAdapter(adapter);
        new AsyncAllParent().execute();
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_username = etUsername.getText().toString().trim();
                String str_password = etPassword.getText().toString().trim();
                String str_nama = etNama.getText().toString().trim();
                String str_alamat = etAlamat.getText().toString().trim();
                String selected = spSebagai.getSelectedItem().toString();
                String str_tipe = "";
                llNotif.setVisibility(View.VISIBLE);
                if(str_username.equals("") || str_username == null ||
                str_password.equals("") || str_password == null ||
                str_nama.equals("") || str_nama == null ||
                str_alamat.equals("") || str_alamat == null ){
                    tvNotif.setText("Semua field harus diisi");
                    tvNotif.setBackgroundColor(Color.parseColor("#FFF59D"));
                }else{
                    if(selected.equals("Jadikan sebagai Koordinator")){
                        parent_id = "0";
                        str_tipe = "2";
                    }else{
                        String[] splited = selected.split("Jadikan sibling dari ");
                        for(int x=0;x<array_nama_parent.length;x++){
                            if(array_nama_parent[x].equals(splited[1])){
                                parent_id = array_id_parent[x];
                                str_tipe = "3";
                            }
                        }
                    }
                    Toast.makeText(getContext(),parent_id.toString(),Toast.LENGTH_LONG).show();
                    data_daftar.add(new BasicNameValuePair("username", str_username));
                    data_daftar.add(new BasicNameValuePair("password", str_password));
                    data_daftar.add(new BasicNameValuePair("nama", str_nama));
                    data_daftar.add(new BasicNameValuePair("alamat", str_alamat));
                    data_daftar.add(new BasicNameValuePair("tipe_user", str_tipe));
                    data_daftar.add(new BasicNameValuePair("parent", parent_id));
                    new AsyncDaftar().execute();
                }
            }
        });
    }

    private class AsyncAllParent extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Mohon menunggu...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.d(TAG, "Do in background");
            HTTPSvc sh = new HTTPSvc();
            String url = RootActivity.api_load_all_parent;
            JSON_data = sh.makeServiceCall(url, HTTPSvc.GET);
            if(JSON_data!=null){
                try {
                    JSONObject jsonObj = new JSONObject(JSON_data);
                    JSONArray response = jsonObj.getJSONArray("response");
                    if(response.length() > 0){
                        array_id_parent = new String[response.length()];
                        array_nama_parent = new String[response.length()];
                        for(int x = 0;x<response.length();x++){
                            JSONObject obj_parent = response.getJSONObject(x);
                            array_id_parent[x] = obj_parent.getString("id");
                            array_nama_parent[x] = obj_parent.getString("nama");
                        }
                    }else{
                        array_id_parent = null;
                        array_nama_parent = null;
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
                loaddata=true;
            }
            else{
                loaddata=false;
            }
            Log.d(TAG, "JSON data : " + JSON_data);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if(loaddata){
                for(int x=0;x<array_nama_parent.length;x++){
                    spinnerArray.add("Jadikan sibling dari " + array_nama_parent[x] );
                }
            }else{
                Toast.makeText(getActivity().getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncDaftar extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Mohon menunggu...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.d(TAG, "Do in background");
            HTTPSvc sh = new HTTPSvc();
            String url = RootActivity.api_daftar.concat(RootActivity.pref_id);
            String JSON_data = sh.makeServiceCall(url, HTTPSvc.POST, data_daftar);
            if(JSON_data!=null){
                try {
                    JSONObject jsonObj = new JSONObject(JSON_data);
                    JSONObject response = jsonObj.getJSONObject("response");
                    status_cek = response.getString("status_cek");
                    message = response.getString("message");
                    message_severity = response.getString("message_severity");
                } catch (final JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
                loaddata=true;
            }
            else{
                loaddata=false;
            }
            Log.d(TAG, "JSON data : " + JSON_data);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if(loaddata){
                tvNotif.setText(message);
                if(status_cek.equals("MATCH")){
                    etUsername.setText("");
                    etPassword.setText("");
                    etNama.setText("");
                    etAlamat.setText("");
                }
                if(message_severity.equals("success")){
                    tvNotif.setBackgroundColor(Color.parseColor("#A5D6A7"));
                }else
                if(message_severity.equals("warning")){
                    tvNotif.setBackgroundColor(Color.parseColor("#FFF59D"));
                }else
                if(message_severity.equals("danger")){
                    tvNotif.setBackgroundColor(Color.parseColor("#EF9A9A"));
                }
            }else{
                tvNotif.setText("Error !");
                tvNotif.setBackgroundColor(Color.parseColor("#EF9A9A"));
            }
        }
    }

}