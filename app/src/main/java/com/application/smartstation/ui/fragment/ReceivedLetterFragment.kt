package com.application.smartstation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentReceivedLetterBinding
import com.application.smartstation.ui.activity.LetterViewActivity
import com.application.smartstation.ui.activity.NewLetterActivity
import com.application.smartstation.ui.adapter.ReceivedLetterAdapter
import com.application.smartstation.ui.model.ChatResponse
import com.application.smartstation.util.Constants
import com.application.smartstation.util.viewBinding

class ReceivedLetterFragment : BaseFragment(R.layout.fragment_received_letter) {

    private val binding by viewBinding(FragmentReceivedLetterBinding::bind)
    var list:ArrayList<ChatResponse> = ArrayList()
    var receivedLetterAdapter:ReceivedLetterAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        list.add(ChatResponse("Anna Bella","Great! Thank you So much","11.54 PM","Today - 06 .08 .2022",R.drawable.pht,1))
        list.add(ChatResponse("Marc Stegen","hey wassup.. whats going on","10.30 PM","Today - 06 .08 .2022",R.drawable.pht1,0))
        list.add(ChatResponse("The Kop Fans","Stevie: Hai, h r u","8.30 PM","Today - 06 .08 .2022",R.drawable.pht2,1))
        list.add(ChatResponse("Philipe Louis","You: I shall do that!","10.15 AM","Today - 06 .08 .2022",R.drawable.pht3,0))
        list.add(ChatResponse("Zhen Zou","Great! Thank you So much","4.30 PM","Today - 06 .08 .2022",R.drawable.pht4,1))
        list.add(ChatResponse("Ji Sung","hey wassup.. whats going on","7.30 PM","Today - 06 .08 .2022",R.drawable.pht5,1))
        list.add(ChatResponse("Aguilera","Stevie: Hai, h r u","9.30 PM","Today - 06 .08 .2022",R.drawable.pht5,0))


        binding.rvReceivedLetter.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
        receivedLetterAdapter = ReceivedLetterAdapter(requireActivity())
        binding.rvReceivedLetter.adapter = receivedLetterAdapter
        receivedLetterAdapter!!.setReceivedLetter(list)

        receivedLetterAdapter!!.onItemClick = { model ->
//            startActivity(Intent(requireActivity(),LetterViewActivity::class.java).putExtra(
//                Constants.NAME,model.name).putExtra(Constants.PROFILE,model.profile))
        }

    }

    private fun setOnClickListener() {

    }

}