package com.example.notification_hubs_test_app_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NotificationDetailActivity : AppCompatActivity() {
    companion object {
        private const val INTENT_KEY_PREFIX = "com.example.notification_hubs_test_app_kotlin.detail:"
        const val INTENT_TITLE_KEY = INTENT_KEY_PREFIX + "title"
        const val INTENT_BODY_KEY = INTENT_KEY_PREFIX + "body"

        private var RESERVED_KEYS: HashSet<String> = HashSet()

        private fun getDataKeys(i: Intent): Iterable<String> {
            val retval: ArrayList<String> = ArrayList()
            for (key in i.extras!!.keySet()) {
                if (!RESERVED_KEYS.contains(key)) {
                    retval.add(key)
                }
            }
            retval.sort()
            return retval
        }

        init {
            RESERVED_KEYS.add(INTENT_TITLE_KEY)
            RESERVED_KEYS.add(INTENT_BODY_KEY)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info: Intent = intent
        setContentView(R.layout.activity_notification_detail)
        val titleValue: TextView = findViewById(R.id.detailTitleValue)
        titleValue.text = info.getStringExtra(INTENT_TITLE_KEY)
        val bodyValue: TextView = findViewById(R.id.detail_body_view)
        bodyValue.text = info.getStringExtra(INTENT_BODY_KEY)
        val detailContent: LinearLayout = findViewById(R.id.detail_content)
        for (key in getDataKeys(info)) {
            val dataRow: View = layoutInflater.inflate(R.layout.data_item, null)
            val dataKey: TextView = dataRow.findViewById(R.id.data_key)
            dataKey.text = key
            val dataValue: TextView = dataRow.findViewById(R.id.data_value)
            dataValue.text = info.getStringExtra(key)
            detailContent.addView(dataRow)
        }
    }
}
