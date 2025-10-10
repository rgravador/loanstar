package com.example.loanstar.ui.loans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoansViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Loans Fragment"
    }
    val text: LiveData<String> = _text
}
