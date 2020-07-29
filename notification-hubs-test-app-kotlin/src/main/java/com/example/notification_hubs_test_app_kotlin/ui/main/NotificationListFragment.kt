package com.example.notification_hubs_test_app_kotlin.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notification_hubs_test_app_kotlin.NotificationDetailActivity
import com.example.notification_hubs_test_app_kotlin.R
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationMessage

class NotificationListFragment : Fragment() {
    private var mViewModel: NotificationListViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(NotificationListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.notification_list_fragment, container, false)
        val notificationDisplayAdapter = NotificationDisplayAdapter()
        val notificationsObserver: Observer<List<NotificationMessage>> = Observer { notificationMessages: List<NotificationMessage> ->
            notificationDisplayAdapter.setNotifications(notificationMessages)
            Toast.makeText(this.context, R.string.notification_received_message, Toast.LENGTH_SHORT).show()
        }
        mViewModel?.getNotificationList()?.observe(viewLifecycleOwner, notificationsObserver)
        val notificationList: RecyclerView = root.findViewById(R.id.notificationList)
        notificationList.layoutManager = LinearLayoutManager(activity)
        notificationList.adapter = notificationDisplayAdapter
        notificationDisplayAdapter.setClickListener(object : NotificationDisplayAdapter.NotificationClickListener {
            override fun onNotificationClicked(message: NotificationMessage?) {
                val i = Intent(activity, NotificationDetailActivity::class.java)
                if (message != null) {
                    i.putExtra(NotificationDetailActivity.INTENT_TITLE_KEY, message.title)
                }
                if (message != null) {
                    i.putExtra(NotificationDetailActivity.INTENT_BODY_KEY, message.body)
                }
                if (message != null) {
                    for ((key, value) in message.data.entries) {
                        i.putExtra(key, value)
                    }
                }
                startActivity(i)
            }
        })
        return root
    }

    companion object {
        fun newInstance(): NotificationListFragment {
            return NotificationListFragment()
        }
    }
}