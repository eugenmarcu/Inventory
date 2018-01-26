package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ItemContract.ItemEntry;

import java.text.DecimalFormat;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find fields to populate in inflated template
        TextView itemName = (TextView) view.findViewById(R.id.item_name);
        TextView itemPrice = (TextView) view.findViewById(R.id.item_price);
        TextView itemQuantity = (TextView) view.findViewById(R.id.item_quantity);
        // Extract properties from cursor
        // Figure out the index of each column
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        final String name = cursor.getString(nameColumnIndex);
        Double price = cursor.getDouble(priceColumnIndex);
        final Double quantity = cursor.getDouble(quantityColumnIndex);
        final int id = cursor.getInt(idColumnIndex);
        if (price == 0|| price==null)
            price = 0.0;
        // Populate fields with extracted properties
        itemName.setText(name);
        itemPrice.setText(formatDouble(price));
        itemQuantity.setText(formatDouble(quantity));

        //Create content values
        final ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, name);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, price);


        //Setting listener buttons for quantity
        Button btnMinus = view.findViewById(R.id.item_btn_minus);
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 0) {
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity - 1);
                    updateQuantity(context, values, id);
                }


            }
        });
        Button btnPlus = view.findViewById(R.id.item_btn_plus);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity + 1);
                updateQuantity(context, values, id);

            }
        });
    }

    void updateQuantity(Context context, ContentValues values, int id) {
        int nrRow = context.getContentResolver().update(ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id),
                values,
                null,
                null);
    }

    String formatDouble(double num){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(num);
    }


}
