package it.polito.mad.g01_timebanking.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.polito.mad.g01_timebanking.adapters.MessageDetails

class ChatViewModel(a: Application) : AndroidViewModel(a) {
    private val _messageList = listOf<MessageDetails>()

    private val pvtMessageList = MutableLiveData<List<MessageDetails>>().also {
        it.value = _messageList
    }

    val messageList = pvtMessageList
}