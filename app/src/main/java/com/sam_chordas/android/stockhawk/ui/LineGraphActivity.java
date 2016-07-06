package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.LinearEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.HistoricalDataDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LineGraphActivity extends AppCompatActivity {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private String STOCK_BASE_URI = "https://query.yahooapis.com/v1/public/yql?q=";
    private String STOCK_BASE_QUERY = "select * from yahoo.finance.historicaldata where symbol = ";

    private String[] mLabels;
    private float[] mValues;

    private String startDate;
    private String endDate;


    private LineChartView mStockLineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        Intent displayLineGraphSymbolIntent = getIntent();

        mStockLineChart = (LineChartView) findViewById(R.id.linechart);
        String getSymbolFromIntent = displayLineGraphSymbolIntent.getStringExtra(MyStocksActivity.CLICK_SYMBOL);
        getSupportActionBar().setTitle(getSymbolFromIntent);
        new FetchStockHistoricalData().execute(getSymbolFromIntent);
    }

    public class FetchStockHistoricalData extends AsyncTask<String, Integer, ArrayList<HistoricalDataDetails>> {

        @Override
        protected ArrayList<HistoricalDataDetails> doInBackground(String... quote) {
            return getStockHistoricalDataFromServer(quote[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<HistoricalDataDetails> historicalDataDetails) {

            mLabels =  new String[historicalDataDetails.size()];
            mValues = new float[historicalDataDetails.size()];

            int i = 0;
            for (HistoricalDataDetails historicalDataDetail : historicalDataDetails) {
                mLabels[i]=historicalDataDetail.getClose();
                mValues[i]= Float.parseFloat(historicalDataDetail.getClose());
                i++;
            }

            LineSet stockdataset = new LineSet(mLabels, mValues);
            stockdataset.setColor(getColor(R.color.line_graph_color))
                    .setFill(getColor(R.color.line_graph_fill))
                    .setGradientFill(new int[]{getColor(R.color.line_graph_gradient_fill), getColor(R.color.line_graph_gradient_fill2)}, null);
            mStockLineChart.addData(stockdataset);

            mStockLineChart.setBorderSpacing(1)
                    .setXLabels(AxisController.LabelPosition.NONE)
                    .setYLabels(AxisController.LabelPosition.NONE)
                    .setXAxis(false)
                    .setYAxis(false)
                    .setBorderSpacing(Tools.fromDpToPx(5));

            Animation anim = new Animation()
                    .setEasing(new LinearEase());mStockLineChart.show(anim);

            TextView tvDateDisplay = (TextView) findViewById(R.id.stock_startDate_endDate);
            tvDateDisplay.setText(String.format(getString(R.string.linegraph_date_display),startDate,endDate));

            super.onPostExecute(historicalDataDetails);
        }
    }

    private ArrayList<HistoricalDataDetails> getStockHistoricalDataFromServer(String quote) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String stockJsonStr = null;
        try {
            // Will contain the raw JSON response as a string.
            String stockHistoryURL = getStockHistoryURL(quote);
            URL url = new URL(stockHistoryURL);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                stockJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                stockJsonStr = null;
            }
            stockJsonStr = buffer.toString();

            return getStockHistoricalDataFromJson(stockJsonStr);

        } catch (IOException e) {
            stockJsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getStockHistoryURL(String qoute) {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date currentDate = new Date();
        endDate = sdf.format(currentDate);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);

        startDate = sdf.format(cal.getTime());

        StringBuilder stockHistoryUrlBuilder = new StringBuilder(STOCK_BASE_URI);
        StringBuilder stockHistoryQueryBuilder = new StringBuilder(STOCK_BASE_QUERY);
        stockHistoryQueryBuilder.append("\"").append(qoute).append("\"");
        stockHistoryQueryBuilder.append("and startDate = \"").append(startDate).append("\" and endDate = \"").append(endDate);
        try {
           return stockHistoryUrlBuilder.append(URLEncoder.encode(stockHistoryQueryBuilder.toString(), "UTF-8")).append("\"&format=json&diagnostics=true&env=store://datatables.org/alltableswithkeys").toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<HistoricalDataDetails> getStockHistoricalDataFromJson(String stockHistoryJson) {
        ArrayList<HistoricalDataDetails> stockHistoricalDataList = new ArrayList<>();
        try {
            JSONObject stockHistoryJsonObject = new JSONObject(stockHistoryJson);
            JSONObject queryObject = stockHistoryJsonObject.getJSONObject("query");
            JSONObject resultsObject = queryObject.getJSONObject("results");
            JSONArray quoteArray = resultsObject.getJSONArray("quote");
            for (int i = 0; i < quoteArray.length(); i++) {

                JSONObject stockHistoryData = quoteArray.getJSONObject(i);
                HistoricalDataDetails historicalDataDetails = new HistoricalDataDetails();

                historicalDataDetails.setSymbol(stockHistoryData.getString("Symbol"));
                historicalDataDetails.setDate(stockHistoryData.getString("Date"));
                historicalDataDetails.setOpen(stockHistoryData.getString("Open"));
                historicalDataDetails.setHigh(stockHistoryData.getString("High"));
                historicalDataDetails.setLow(stockHistoryData.getString("Low"));
                historicalDataDetails.setClose(stockHistoryData.getString("Close"));
                historicalDataDetails.setVolume(stockHistoryData.getString("Volume"));
                historicalDataDetails.setAdj_close(stockHistoryData.getString("Adj_Close"));

                stockHistoricalDataList.add(historicalDataDetails);
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return stockHistoricalDataList;
    }
}
