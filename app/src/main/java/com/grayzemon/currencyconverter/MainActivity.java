package com.grayzemon.currencyconverter;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        List<String> test = new ArrayList<>(Arrays.asList(
                "EUR","USD","GBP"
        ));

        getCurrencyList();

        ArrayAdapter<String> currencies =
                new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,currencyNames);
        convertFrom.setAdapter(currencies);
        convertTo.setAdapter(currencies);
    }

    private void getCurrencyList() {
        final Set<Currency> availableCurrencies = Currency.getAvailableCurrencies();
        Stream<Currency> stream = availableCurrencies.stream();
        Function<Currency,String> map = currency -> currency.getDisplayName();
        currencyNames = stream.map(map).sorted().filter(str -> str.matches(REGEX)).collect(Collectors.toList());
        for (String s : currencyNames) {
            Log.v(TAG, s);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"onClick: Button pressed");
        View view = findViewById(R.id.constraint_main);
        Snackbar.make(view, R.string.snackBarMessage,Snackbar.LENGTH_SHORT).show();

    }
}
