package com.grayzemon.currencyconverter;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import okhttp3.HttpUrl;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String REGEX = ".+[^()0-9-]+";
    private final String TAG = getClass().getSimpleName();
    private Spinner convertFrom;
    private Spinner convertTo;
    private List<String> currencyNames;
    private URL url;
    private String currencyFrom;
    private String currencyTo;
    private double rate;
    private TextView numberConvertedAmount;
    private Button buttonConvert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        convertFrom = (Spinner) findViewById(R.id.spinner_convert_from);
        convertTo = (Spinner) findViewById(R.id.spinner_convert_to);
        numberConvertedAmount = (TextView) findViewById(R.id.number_converted_amount);

        buttonConvert = (Button) findViewById(R.id.button_convert);
        buttonConvert.setOnClickListener(this);

        getCurrencyList();

        ArrayAdapter<String> currencies =
                new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,currencyNames);
        convertFrom.setAdapter(currencies);
        convertTo.setAdapter(currencies);
        convertFrom.setSelection(9);
        convertTo.setSelection(8);
    }

    private void getCurrencyList() {
        final String currencyCodeList =
                "AUD,BGN,BRL,CAD,CHF,CNY,CZK,DKK,GBP,EUR,HKD,HRK,HUF,IDR,ILS," +
                "INR,ISK,JPY,KRW,MXN,MYR,NOK,NZD,PHP,PLN,RON,RUB,SEK,SGD,THB,TRY,USD,ZAR";
        final List<String> currencyCodes = Arrays.asList(currencyCodeList.split(","));

        Function<String,String> mapCode2Desc = currencyCode -> Currency.getInstance(currencyCode).getDisplayName() + " " + currencyCode;
        Consumer<String> desc = d -> Log.v(TAG,"currency after sort & map = " + d);
        Stream<String> stream = currencyCodes.stream();
        currencyNames = stream.sorted().map(mapCode2Desc).peek(desc).collect(Collectors.toList());

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"onClick: Button pressed");
        buttonConvert.setEnabled(false);
        getCurrencyCodesFromDropdowns();
        buildURL();
        //showSnackBar(url.toString());
        getCurrencyRate();
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
                .scheme("https")
                .host("exchangeratesapi.io")
                .addPathSegments("api/latest")
                .addQueryParameter("base", currencyFrom)
                .addQueryParameter("symbols", currencyTo)
                .build().url();
        Log.d(TAG, url.toString());
    }

    private void showSnackBar(String text) {
        View view = findViewById(R.id.constraint_main);
        Snackbar.make(view, text,Snackbar.LENGTH_SHORT).show();
    }

    public void getCurrencyRate(){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                url.toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            buttonConvert.setEnabled(true);
                            rate = response.getJSONObject("rates").getDouble(currencyTo);
                            Log.d(TAG,"Converted=" + rate);
                            numberConvertedAmount.setText(String.valueOf(rate));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showSnackBar(e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                buttonConvert.setEnabled(true);
                Log.d(TAG,error.getMessage());
            }
        });
        queue.add(jsonObjectRequest);
    }
}
