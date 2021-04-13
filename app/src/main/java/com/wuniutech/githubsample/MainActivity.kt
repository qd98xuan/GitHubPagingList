package com.wuniutech.githubsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    val viewModel:MainViewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }
    val githubList:RecyclerView by lazy { findViewById(R.id.github_list) }
    val progressCircle:ProgressBar by lazy { findViewById(R.id.progress_circular) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val githubAdapter = GithubPagingAdapter()
        githubList.layoutManager=LinearLayoutManager(this)
        githubList.adapter = githubAdapter
        lifecycleScope.launch {
            viewModel.getRepoData().collect {
                githubAdapter.submitData(it)
            }
        }
        githubAdapter.addLoadStateListener {
            when(it.refresh){
                is LoadState.Loading->{
                    progressCircle.visibility= View.VISIBLE
                    githubList.visibility = View.GONE
                }
                is LoadState.NotLoading->{
                    progressCircle.visibility = View.GONE
                    githubList.visibility = View.VISIBLE
                }
                is LoadState.Error->{
                    progressCircle.visibility = View.GONE
                    val error = it.refresh as LoadState.Error
                    Toast.makeText(this,"${error.error.message}",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}