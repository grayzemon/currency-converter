package com.grayzemon.currencyconverter;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        convertFrom = (Spinner) findViewById(R.id.spinner_convert_from);
        convertTo = (Spinner) findViewById(R.id.spinner_convert_to);

        Button convert = (Button) findViewById(R.id.button_convert) ;
        convert.setOnClickListener(this);

        getCurrencyList();

        ArrayAdapter<String> currencies =
                new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,currencyNames);
        convertFrom.setAdapter(currencies);
        convertTo.setAdapter(currencies);
    }

    private void getCurrencyList() {
        final String currencyCodeList = "AUD,BGN,BRL,CAD,CHF,CNY,CZK,DKK,GBP,HKD,HRK,HUF,IDR,ILS,INR,ISK,JPY,KRW,MXN,MYR,NOK,NZD,PHP,PLN,RON,RUB,SEK,SGD,THB,TRY,USD,ZAR";
        final List<String> currencyCodes = Arrays.asList(currencyCodeList.split(","));

        Function<String,String> mapCode2Desc = currencyCode -> Currency.getInstance(currencyCode).getDisplayName() + " " + currencyCode;
        Consumer<String> desc = d -> Log.v(TAG,"currency after sort & map = " + d);
        Stream<String> stream = currencyCodes.stream();
        currencyNames = stream.sorted().map(mapCode2Desc).peek(desc).collect(Collectors.toList());

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"onClick: Button pressed");

        int selectedPositionFrom = convertFrom.getSelectedItemPosition();
        int selectedPositionTo = convertTo.getSelectedItemPosition();
        String selectedCurrencyFrom = currencyNames.get(selectedPositionFrom);
        String selectedCurrencyTo = currencyNames.get(selectedPositionTo);
        String currencyFrom = selectedCurrencyFrom.substring(
                selectedCurrencyFrom.length() - 3,selectedCurrencyFrom.length());
        String currencyTo = selectedCurrencyTo.substring(
                selectedCurrencyTo.length() - 3,selectedCurrencyTo.length());

        URL url = new HttpUrl.Builder()
                .scheme("https")
                .host("exchangeratesapi.io")
                .addPathSegments("api/latest")
                .addQueryParameter("base", currencyFrom)
                .addQueryParameter("symbols", currencyTo)
                .build().url();
        Log.d(TAG, url.toString());
        View view = findViewById(R.id.constraint_main);
        Snackbar.make(view, "URL=" + url.toString(),Snackbar.LENGTH_SHORT).show();
    }
}
