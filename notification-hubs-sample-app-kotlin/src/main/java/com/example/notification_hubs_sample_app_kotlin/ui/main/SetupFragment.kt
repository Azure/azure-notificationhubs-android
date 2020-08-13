package com.example.notification_hubs_sample_app_kotlin.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notification_hubs_sample_app_kotlin.R
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub

class SetupFragment : androidx.fragment.app.Fragment() {
    private var mViewModel: SetupViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(SetupViewModel::class.java)
        val unknownText: String = resources.getString(R.string.unknown_information)
        mViewModel!!.setUnknownText(unknownText)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root: View = inflater.inflate(R.layout.setup_fragment, container, false)
        val deviceTokenValue: TextView = root.findViewById(R.id.device_token_value)
        mViewModel?.getDeviceToken()?.observe(viewLifecycleOwner, Observer { text: CharSequence? -> deviceTokenValue.text = text })
        val installationIdValue: TextView = root.findViewById(R.id.installation_id_value)
        mViewModel?.getInstallationId()?.observe(viewLifecycleOwner, Observer { text: CharSequence? -> installationIdValue.text = text })
        val isEnabled: Switch = root.findViewById(R.id.enabled_switch)
        mViewModel?.getIsEnabled()?.observe(viewLifecycleOwner, Observer { checked: Boolean? ->
            if (checked != null) {
                isEnabled.isChecked = checked
            }
        })
        isEnabled.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean -> mViewModel?.setIsEnabled(isChecked) }
        val tagToAddField: EditText = root.findViewById(R.id.add_tag_field)
        val tagToAddButton: Button = root.findViewById(R.id.add_tag_button)
        tagToAddButton.setOnClickListener {
            try {
                mViewModel?.addTag(tagToAddField.text.toString())
            } catch (e: java.lang.IllegalArgumentException) {
                val toast: Toast = Toast.makeText(context, R.string.invalid_tag_message, Toast.LENGTH_SHORT)
                toast.show()
            }
            tagToAddField.text.clear()
        }
        val tagDisplayAdapter = mViewModel?.let { TagDisplayAdapter(it) }
        val tagsObserver: Observer<List<String>> = Observer { s: List<String>? ->
            tagDisplayAdapter?.setTags(s)
        }
        mViewModel?.getTags()?.observe(viewLifecycleOwner, tagsObserver)
        val tagList: RecyclerView = root.findViewById(R.id.tag_list)
        tagList.layoutManager = LinearLayoutManager(activity)
        tagList.adapter = tagDisplayAdapter
        NotificationHub.setInstallationSavedListener {
            Toast.makeText(this.context, getString(R.string.installation_saved_message), Toast.LENGTH_SHORT).show()
            mViewModel?.setInstallationId(NotificationHub.getInstallationId())
            mViewModel?.setDeviceToken(NotificationHub.getPushChannel())
        }
        NotificationHub.setInstallationSaveFailureListener { Toast.makeText(this.context, getString(R.string.installation_save_failure_message), Toast.LENGTH_SHORT).show() }
        return root
    }
}