package com.kylejw.vsms.vsms;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kylejw.vsms.vsms.Database.SmsMessageContentProvider;
import com.kylejw.vsms.vsms.Database.SmsMessageTable;
import com.kylejw.vsms.vsms.VoipMsApi.DataModel.SmsMessage;
import com.kylejw.vsms.vsms.VoipMsApi.Exchanges.SendSms;
import com.kylejw.vsms.vsms.VoipMsApi.VoipMsRequest;

import java.util.Calendar;

public class ConversationActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private SimpleCursorAdapter adapter;

    private String contact = null;
    private String did = null;

    private EditText sendMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        if (savedInstanceState == null) {
            // Read data from saved instance
            Bundle extras = getIntent().getExtras();
            contact = extras.getString(SmsMessageTable.COLUMN_CONTACT);
            did = extras.getString(SmsMessageTable.COLUMN_DID);
        } else {
            // Read incoming data from other activity
            contact = savedInstanceState.getString(SmsMessageTable.COLUMN_CONTACT);
            did = savedInstanceState.getString(SmsMessageTable.COLUMN_DID);
        }

        String contactName = ContactsHelpers.displayNameFromContactNumber(this, contact);
        if (null == contactName || contactName.length() == 0) {
            setTitle(contact);
        } else {
            setTitle(contactName);
        }

        initAdapter();
        initButtons();
    }

    private void initButtons() {

        final Button sendMessageButton;
        sendMessageButton = (Button) findViewById(R.id.send_message_button);
        sendMessageText = (EditText) findViewById(R.id.send_message_text);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendButtonClicked(v);
            }
        });

        sendMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendMessageButton.setEnabled(!sendMessageText.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initAdapter() {
        // Fields from the database (projection)
        // Must include the _id column for the adapter to work

        String[] from = new String[]{SmsMessageTable.COLUMN_CONTACT, SmsMessageTable.COLUMN_MESSAGE, SmsMessageTable.COLUMN_INTERNAL_ID, SmsMessageTable.COLUMN_TYPE};
        // Fields on the UI to which we map
        int[] to = new int[]{R.id.name_entry, R.id.message_entry, R.id.id_entry, R.id.none};

        getLoaderManager().initLoader(0, null, this);
//        adapter = new SimpleCursorAdapter(this, R.layout.list_message_entry, null, from,
//                to, 0);

        adapter = new SimpleCursorAdapter(this, R.layout.list_message_entry, null, from, to, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);

                final int ind = cursor.getColumnIndex(SmsMessageTable.COLUMN_TYPE);
                String type = cursor.getString(ind);
                SmsMessage.MessageType messageType = SmsMessage.MessageType.valueOf(type);

                if (messageType == SmsMessage.MessageType.RECEIVED) {
                    view.setBackgroundColor(0x5FB2FA);
                } else {
                    view.setBackgroundColor(Color.WHITE);
                }
            }
        };


        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendButtonClicked(View v) {

        final Button sendMessageButton = (Button) findViewById(R.id.send_message_button);
        sendMessageButton.setEnabled(false);
        sendMessageText.setEnabled(false);

        final String text = sendMessageText.getText().toString();

        final Context context = this;

        SendSms send = new SendSms(did, contact, text);
        send.executeAsync(new VoipMsRequest.VoipMsCallback<SendSms.SendSmsResponse>() {
            @Override
            public void onComplete(SendSms.SendSmsResponse sendSmsResponse) {
                sendMessageText.setEnabled(true);

                if (!sendSmsResponse.isSuccess()) {
                    Toast.makeText(context, "Failed to send SMS: " + sendSmsResponse.getStatus(), Toast.LENGTH_LONG);
                    sendMessageButton.setEnabled(true);
                    return;
                }

                ContentValues values = new ContentValues();
                values.put(SmsMessageTable.COLUMN_CONTACT, contact);
                values.put(SmsMessageTable.COLUMN_VOIPMS_ID, sendSmsResponse.getId());
                values.put(SmsMessageTable.COLUMN_TYPE, SmsMessage.MessageType.SENT.toString());
                values.put(SmsMessageTable.COLUMN_MESSAGE, text);
                values.put(SmsMessageTable.COLUMN_DID, did);
                values.put(SmsMessageTable.COLUMN_DATE, Calendar.getInstance().getTimeInMillis());
                getContentResolver().insert(SmsMessageContentProvider.CONTENT_URI, values);

                sendMessageText.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(sendMessageText.getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        try {
            String[] projection = {SmsMessageTable.COLUMN_INTERNAL_ID, SmsMessageTable.COLUMN_DID, SmsMessageTable.COLUMN_CONTACT, SmsMessageTable.COLUMN_MESSAGE, SmsMessageTable.COLUMN_DATE, SmsMessageTable.COLUMN_TYPE};
            Uri dbUri = Uri.parse(SmsMessageContentProvider.CONTACT_URI + "/" + contact);

            return new CursorLoader(this, dbUri, projection, null, null, SmsMessageTable.COLUMN_DATE + " ASC");
        } catch(Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG);
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            adapter.swapCursor(data);
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
