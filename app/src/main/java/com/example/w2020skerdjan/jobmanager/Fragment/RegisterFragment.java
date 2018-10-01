package com.example.w2020skerdjan.jobmanager.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.w2020skerdjan.jobmanager.Activities.LoginActivity;
import com.example.w2020skerdjan.jobmanager.Models.HttpRequest.LoginResponse;
import com.example.w2020skerdjan.jobmanager.Models.HttpRequest.RegisterResponse;
import com.example.w2020skerdjan.jobmanager.Models.HttpRequest.RegisterResponseSuccess;
import com.example.w2020skerdjan.jobmanager.R;
import com.example.w2020skerdjan.jobmanager.Retrofit.Requests.RequestsAPI;
import com.example.w2020skerdjan.jobmanager.Retrofit.RetrofitClient;
import com.example.w2020skerdjan.jobmanager.Utils.CodesUtil;
import com.example.w2020skerdjan.jobmanager.Utils.MySharedPref;
import com.example.w2020skerdjan.jobmanager.Utils.RetrofitParamGenerator;
import com.example.w2020skerdjan.jobmanager.Utils.Utils;

import org.angmarch.views.NiceSpinner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import in.goodiebag.carouselpicker.CarouselPicker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterFragment extends Fragment {

    private RelativeLayout relativeLayout;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private EditText email, password, confirmPassword;
    private NiceSpinner niceSpinner;
    private boolean passwordsMatch = false;
    private ProgressDialog progressDialog;
    private MySharedPref mySharedPref;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mySharedPref = new MySharedPref(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
         niceSpinner = (NiceSpinner) view.findViewById(R.id.nice_spinner);
        relativeLayout = view.findViewById(R.id.registerTrigger);
        email = view.findViewById(R.id.input_email);
        password = view.findViewById(R.id.input_password);
        confirmPassword = view.findViewById(R.id.input_confirm_password);
        List<String> dataset = new LinkedList<>(Arrays.asList("Employee", "Employer", "Manager", "Admin"));
        niceSpinner.attachDataSource(dataset);

        progressDialog= new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registering...");

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            registerHttpRequestNoRetrofit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            thread.start();

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordsMatch = doPasswordsMatch(confirmPassword.getText().toString(), charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordsMatch = doPasswordsMatch(password.getText().toString(), charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }
    private boolean validate() {
        boolean valid = true;
        String emailTxt = email.getText().toString();
        String passwordTxt = password.getText().toString();

        if (emailTxt.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            email.setError("Enter a valid email address");
            valid = false;
        } else {
            email.setError(null);
        }

        if (passwordTxt.isEmpty() || passwordTxt.length() < 4 || passwordTxt.length() > 30) {
            password.setError("Password must be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            password.setError(null);
        }

        if(!passwordsMatch){
            valid = false;
            confirmPassword.setError("Passwords must match!");
        }
        else {
            confirmPassword.setError(null);
        }
        return valid;
    }

    private boolean doPasswordsMatch(String password, String confirmPassword){
        if(password.equals(confirmPassword)){
            return true;
        }
        else{
            return false;
        }
    }

    private void registerHttpRequestNoRetrofit() throws IOException {
        URL url = null;

        try {
            url = new URL("http://www.oncallemployee.com/client/api/account/register");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection conn = null;
        BufferedWriter writer = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = null;
            os = conn.getOutputStream();
            writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(RetrofitParamGenerator.generateRegisterMap(email.getText().toString().trim(),password.getText().toString().trim(),confirmPassword.getText().toString().trim(),niceSpinner.getText().toString().toLowerCase().trim())));
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
        }

        String inputLine;
        StringBuffer content = new StringBuffer();

        if (in != null) {
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }

        if(conn.getResponseCode()==200){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),"Register successful!", Toast.LENGTH_SHORT).show();
                    ((LoginActivity)getActivity()).initLogin();
                }
            });

             mySharedPref.saveStringInSharedPref(CodesUtil.USERNAME, email.getText().toString().trim() );
             mySharedPref.saveStringInSharedPref(CodesUtil.PASSWORD,password.getText().toString().trim() );

        }
        else {
            progressDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Register failed with error! Please make sure you enter valid data", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private String getQuery(Map<String,String> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(params.get(key), "UTF-8"));
        }
        return result.toString();
    }


}
