package com.grayzemon.currencyconverter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import okhttp3.HttpUrl;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String HOST = "free-currency-converter.herokuapp.com";
    public static final String PATH_SEGMENTS = "list/convert";
    public static final String HTTPS = "https";
    public static final String BASE = "source";
    public static final String SYMBOLS = "destination";
    public static final String CONVERT_FROM = "convertFrom";
    public static final String CONVERT_TO = "convertTo";
    public static final String AMOUNT = "amount";
    public static final String RATES = "converted_value";
    private final String TAG = getClass().getSimpleName();
    private Spinner convertFrom;
    private Spinner convertTo;
    private List<String> currencyNames;
    private URL url;
    private String currencyFrom;
    private String currencyTo;

    private double rate;
    private Double amount =1.00d;
    private Double convertedAmount =0.00d;

    private TextView textBaseAmount;
    private TextView textRateAmount;
    private TextView textConvertedAmount;
    private Button buttonConvert;
    private DecimalFormat df = new DecimalFormat("###,##0.00");
    private DecimalFormat df2 = new DecimalFormat("0.00");

    interface VolleyCallback {
        public void onSuccess(JSONObject response);
        public void onErrorResponse(VolleyError error);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        setPreferences();
//    }

    @Override
    protected void onPause() {
        super.onPause();
        setPreferences();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        convertFrom = (Spinner) findViewById(R.id.spinner_convert_from);
        convertTo = (Spinner) findViewById(R.id.spinner_convert_to);
        textRateAmount = (TextView) findViewById(R.id.rate_converted_amount);
        textBaseAmount = (TextView) findViewById(R.id.number_amount);
        textConvertedAmount = (TextView) findViewById(R.id.text_converted_amount);

        buttonConvert = (Button) findViewById(R.id.button_convert);
        buttonConvert.setOnClickListener(this);
        getCurrencyList();

        ArrayAdapter<String> currencies =
                new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,currencyNames);
        convertFrom.setAdapter(currencies);
        convertTo.setAdapter(currencies);
        getPreferences();
    }

    private void getCurrencyList() {
        final String currencyCodeList =
                "AUD,BGN,BRL,CAD,CHF,CNY,CZK,DKK,GBP,EUR,HKD,HRK,HUF,IDR,ILS," +
                "INR,ISK,JPY,KRW,MXN,MYR,NOK,NZD,PHP,PLN,RON,RUB,SEK,SGD,THB,TRY,USD,ZAR";
        final List<String> currencyCodes = Arrays.asList(currencyCodeList.split(","));

        Function<String,String> mapCode2Desc = currencyCode -> Currency.getInstance(currencyCode).getDisplayName() + " " + currencyCode;
        //Consumer<String> desc = d -> Log.v(TAG,"currency after sort & map = " + d);
        Stream<String> stream = currencyCodes.stream();
        currencyNames = stream.map(mapCode2Desc).sorted().collect(Collectors.toList());

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"onClick: Button pressed");
        if (!validateInputAmount(false))
            return;
        buttonConvert.setEnabled(false);
        getCurrencyCodesFromDropdowns();
        buildURL();
        getCurrencyRate(new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                buttonConvert.setEnabled(true);
                setTextRate(response);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                buttonConvert.setEnabled(true);
                checkNetworkConnection(error);
            }
        });
    }

    private void checkNetworkConnection(VolleyError error) {
        Log.d(TAG,error.getMessage());
        if (isNetworkAvailable())
            showMessage("Error:" + error.getMessage());
        else
            showMessage("No network connection");
    }

    private void setTextRate(JSONObject response) {
        try {
            buttonConvert.setEnabled(true);
            //rate = response.getJSONObject(RATES).getDouble(currencyTo);
            rate = response.getDouble(RATES);
            Log.d(TAG,"Converted=" + rate);
            textRateAmount.setText(String.valueOf(rate));
            calculateConvertedAmount();
        } catch (JSONException e) {
            Log.d(TAG,"Error: " + e.getMessage());
        }
    }

    private void calculateConvertedAmount() {
        convertedAmount = rate * amount;
        String currencySymbol = Currency.getInstance(currencyTo).getSymbol();
        String convertedText = currencySymbol + " " + df.format(convertedAmount);
        textConvertedAmount.setText(convertedText);
        Log.d(TAG, "Converted Amount: " + convertedAmount);
    }

    private boolean validateInputAmount(Boolean suppressMessage) {
        String amountStr = textBaseAmount.getText().toString();
        if (amountStr.isEmpty() || amountStr.equals(".")) {
            if (!suppressMessage) {
                showMessage(getString(R.string.Invalid_Amount));
            }
            Log.d(TAG,getString(R.string.Invalid_Amount));
            return false;
        }
        amount = Double.valueOf(amountStr);
        return true;
    }

    private void getCurrencyCodesFromDropdowns() {
        int selectedPositionFrom = convertFrom.getSelectedItemPosition();
        int selectedPositionTo = convertTo.getSelectedItemPosition();
        String selectedCurrencyFrom = currencyNames.get(selectedPositionFrom);
        String selectedCurrencyTo = currencyNames.get(selectedPositionTo);
        currencyFrom = selectedCurrencyFrom.substring(
                selectedCurrencyFrom.length() - 3,selectedCurrencyFrom.length());
        currencyTo = selectedCurrencyTo.substring(
                selectedCurrencyTo.length() - 3,selectedCurrencyTo.length());
    }

    private void buildURL() {
        url = new HttpUrl.Builder()
                .scheme(HTTPS)
                .host(HOST)
                .addPathSegments(PATH_SEGMENTS)
                .addQueryParameter(BASE, currencyFrom)
                .addQueryParameter(SYMBOLS, currencyTo)
                .build().url();
        Log.d(TAG, url.toString());
    }

    private void getPreferences() {
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        String currencyFrom = sp.getString(CONVERT_FROM,"British Pound GBP");
        String currencyTo= sp.getString(CONVERT_TO,"Euro EUR");
        amount = Double.valueOf(sp.getString(AMOUNT,"1.00d"));
        textBaseAmount.setText(String.valueOf(df2.format(amount)));
        convertFrom.setSelection(currencyNames.indexOf(currencyFrom));
        convertTo.setSelection(currencyNames.indexOf(currencyTo));
        Log.d(TAG,"Load currencyFrom" + currencyFrom);
        Log.d(TAG,"Load currencyTo" + currencyTo);
        Log.d(TAG,"Load Amount" + amount);
    }

    private void setPreferences() {
        int selectedPositionFrom = convertFrom.getSelectedItemPosition();
        int selectedPositionTo = convertTo.getSelectedItemPosition();
        String selectedCurrencyFrom = currencyNames.get(selectedPositionFrom);
        String selectedCurrencyTo = currencyNames.get(selectedPositionTo);
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(CONVERT_FROM, selectedCurrencyFrom);
        edit.putString(CONVERT_TO, selectedCurrencyTo);
        validateInputAmount(true);
        edit.putString(AMOUNT, Double.toString(amount));
        edit.apply();
        Log.d(TAG,"Save currencyFrom" + selectedCurrencyFrom);
        Log.d(TAG,"Save currencyTo" + selectedCurrencyTo);
        Log.d(TAG,"Save Amount" + amount);
    }

    private void showMessage(String item) {
        Toast.makeText(this,item,Toast.LENGTH_LONG).show();
    }

    public void getCurrencyRate(VolleyCallback callback){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                url.toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onErrorResponse(error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
