package com.namelessdev.mpdroid;

import com.namelessdev.mpdroid.tools.Tools;
import com.tsengvn.typekit.TypekitContextWrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class PlayPopupActivity extends Activity {
    private int[] mListPositions = null;

    public PlayPopupActivity() {
        Activity activity = this;
    }

    protected void attachBaseContext(Context context) {
        super.attachBaseContext(TypekitContextWrapper.wrap(context));
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_play_popup);
        Intent intent = getIntent();
        this.mListPositions = intent.getIntArrayExtra("positions");
        ((TextView) findViewById(R.id.popup_album)).setText(Tools.get_EUCKR_string(intent.getStringExtra("mainText")));
        ((TextView) findViewById(R.id.popup_artist)).setText(Tools.get_EUCKR_string(intent.getStringExtra("subText")));
        if (intent.getIntExtra("type", 0) == 1) {
            ((ImageButton) findViewById(R.id.button4)).setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view) {
        int result = 0;
        switch (view.getId()) {
            case R.id.button1 /* Play now */:
                result = 1;
                break;
            case R.id.button4 /* Replace and play now*/:
                result = 4;
                break;
            case R.id.button2 /* Add next */:
                result = 2;
                break;
            case R.id.button3 /* Add to queue */:
                result = 3;
                break;
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("positions", this.mListPositions);
        resultIntent.putExtra("popup_result", result);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
