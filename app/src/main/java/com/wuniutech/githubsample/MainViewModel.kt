package com.wuniutech.githubsample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class MainViewModel:ViewModel() {
    fun getRepoData():Flow<PagingData<Repo>>{
        return Repository.getPageData().cachedIn(viewModelScope)
    }
}