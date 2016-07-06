package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by PRAGYA on 4/24/2016.
 */
public class WidgetDataProvider implements RemoteViewsFactory {

    private Cursor widgetCursor = null;
    private Context mContext = null;

    public WidgetDataProvider(Context context, Intent intent) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        Log.d("", "");
    }

    public void setWidgetCursor(Cursor widgetCursor) {
        this.widgetCursor = widgetCursor;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (widgetCursor != null) {
            return widgetCursor.getCount();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item_quote);
        if (widgetCursor != null) {
            Cursor mCursor = widgetCursor;
            mCursor.moveToPosition(position);
            mView.setTextViewText(R.id.stock_symbol, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
            mView.setTextViewText(R.id.bid_price, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));

            if (mCursor.getInt(mCursor.getColumnIndex("is_up")) == 1) {
                mView.setViewVisibility(R.id.changeGreen, View.VISIBLE);
                mView.setViewVisibility(R.id.changeRed, View.INVISIBLE);
                if (Utils.showPercent) {
                    mView.setTextViewText(R.id.changeGreen, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                } else {
                    mView.setTextViewText(R.id.changeGreen, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));
                }
            } else {
                mView.setViewVisibility(R.id.changeGreen, View.INVISIBLE);
                mView.setViewVisibility(R.id.changeRed, View.VISIBLE);
                if (Utils.showPercent) {
                    mView.setTextViewText(R.id.changeRed, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                } else {
                    mView.setTextViewText(R.id.changeRed, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));
                }
            }

            String symbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));

            final Intent fillInIntent = new Intent();
            fillInIntent.putExtra(MyStocksActivity.CLICK_SYMBOL,symbol);
            mView.setOnClickFillInIntent(R.id.widgetStockItem, fillInIntent);
        }
        return mView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
