package com.example.android.inventory;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ItemContract.ItemEntry;
import com.example.android.inventory.data.ItemDbHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by Eugen on 10-Jan-18.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    Uri mUri;
    private int CURSOR_LOADER_ID = 2;
    private int BARCODE_REQUEST_CODE = 1;
    private boolean mItemHasChanged = false;

    /**
     * EditText field to enter the item's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the item's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the item's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the item's supplier
     */
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the item's supplier's phone number
     */
    private EditText mPhoneEditText;

    /**
     * EditText field to enter the item's supplier's email
     */
    private EditText mEmailEditText;

    /**
     * TextView field to show the item's barcode
     */
    private TextView mBarcodeText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity);



        mUri = getIntent().getData();
        if (mUri != null) {
            getSupportActionBar().setTitle("Edit Item");
            Log.e("Editor Activity", "--------------URI-ul ESTE: " + mUri.toString() + "----------------");
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.initLoader(CURSOR_LOADER_ID, null, this);
        } else {
            getSupportActionBar().setTitle("Add Item");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_item_name);
        mPriceEditText = findViewById(R.id.edit_item_price);
        mQuantityEditText = findViewById(R.id.edit_item_quantity);
        mSupplierEditText = findViewById(R.id.edit_item_supplier);
        mPhoneEditText = findViewById(R.id.edit_item_supplier_phone);
        mEmailEditText = findViewById(R.id.edit_item_supplier_email);
        mBarcodeText = findViewById(R.id.edit_item_barcode);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mBarcodeText.setOnTouchListener(mTouchListener);

        ImageButton buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save item to database
                saveItem();
                // Exit activity
                finish();
            }
        });
        ImageButton buttonDelete = findViewById(R.id.buttonDelete);
        if (mUri != null) {
            buttonDelete.setVisibility(View.VISIBLE);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog();
                }
            });
        }
        else
            buttonDelete.setVisibility(View.GONE);

        //Setting the order button
        Button buttonOrder = findViewById(R.id.buttonCall);
        if (mUri != null) {
            buttonOrder.setVisibility(View.VISIBLE);
            buttonOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String number = mPhoneEditText.getText().toString().trim();
                    showOrderConfirmationDialog(number);
                }

            });
        }
        else
            buttonOrder.setVisibility(View.GONE);
        Button buttonBarcode = findViewById(R.id.edit_item_barcode_button);
        buttonBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditorActivity.this, BarcodeActivity.class);
                startActivityForResult(intent, BARCODE_REQUEST_CODE);
            }
        });
    }

    //On barcode scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == resultCode) {
            //For unsaved changes
            mItemHasChanged = true;
            String result = data.getExtras().getString("CODE");
            TextView barcodeTextView = findViewById(R.id.edit_item_barcode);
            barcodeTextView.setText(result);
        }
    }

    //Checks the call permissions or request them, than call
    public void onCallNumber(String number) {
        int REQUEST_CODE = 1;

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE);
        } else {
            startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel: " + number)));
        }

    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE,
                ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL,
                ItemEntry.COLUMN_ITEM_BARCODE
        };


        return new CursorLoader(this,
                mUri,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL);
            int barcodeColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_BARCODE);
            String name = cursor.getString(nameColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            Double quantity = cursor.getDouble(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(supplierPhoneColumnIndex);
            String email = cursor.getString(supplierEmailColumnIndex);
            String barcode = cursor.getString(barcodeColumnIndex);
            mNameEditText.setText(name);
            mPriceEditText.setText(formatDouble(price));
            mQuantityEditText.setText(formatDouble(quantity));
            mSupplierEditText.setText(supplier);
            mPhoneEditText.setText(phone);
            mEmailEditText.setText(email);
            mBarcodeText.setText(barcode);
        }
    }
    String formatDouble(double num){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(num);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("0");
        mSupplierEditText.setText("");
        mPhoneEditText.setText("");
        mEmailEditText.setText("");
        mBarcodeText.setText("");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                saveItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Delete item
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItem(){
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String barcodeString = mBarcodeText.getText().toString().trim();
        //If no valid data exit method
        if(mUri == null && TextUtils.isEmpty(nameString)&&TextUtils.isEmpty(priceString))
            return;
        double quantity = 0;
        double price = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
            try {
                Number number = format.parse(quantityString);
                quantity = number.doubleValue();
            }
            catch (ParseException e) {
                quantity = Double.parseDouble(quantityString);
            }
        }
        if(!TextUtils.isEmpty(priceString)){
            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
            try {
                Number number = format.parse(priceString);
                price = number.doubleValue();
            }
            catch (ParseException e){
                price = Double.parseDouble(priceString);
            }
        }


        // Create database helper
        ItemDbHelper mDbHelper = new ItemDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, supplierString);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, phoneString);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL, emailString);
        values.put(ItemEntry.COLUMN_ITEM_BARCODE, barcodeString);

        if (mUri == null) {
            // Insert a new row for item in the database, returning the ID of that new row.
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the row ID is -1, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Update item
            int nrRow = getContentResolver().update(mUri,
                    values,
                    null,
                    null);
            if (nrRow==0) {
                // If the row ID is -1, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showOrderConfirmationDialog(final String number){
        if(!TextUtils.isEmpty(number)) {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the positive and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.order_dialog_msg) + " " + number + "?");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Delete" button, so delete the item.
                    onCallNumber(number);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Cancel" button, so dismiss the dialog
                    // and continue editing the item.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please enter a valid phone number !");
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        if(mUri!=null) {
            int nrRow = getContentResolver().delete(mUri, null, null);
            if (nrRow > 0) {
                //Item deleted
                Toast.makeText(this, getString(R.string.editor_delete_item_successful), Toast.LENGTH_SHORT).show();

            } else {
                //Item delete failed
                Toast.makeText(this, getString(R.string.editor_delete_item_failed), Toast.LENGTH_SHORT).show();
            }
        }
        //Exit the activity
        finish();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mPetHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

}
