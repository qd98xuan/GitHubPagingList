package com.wuniutech.githubsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.Text
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.Exception

data class Repo(
    @SerializedName("id")val id: Int,
    @SerializedName("name")val name: String,
    @SerializedName("description")val description: String,
    @SerializedName("stargazers_count")val stargazers_count: Int
)

class RepoResponse(
    @SerializedName("items")val items: List<Repo> = emptyList()
)

interface GithubService {
    @GET("search/repositories?sort=stars&q=Android")
    suspend fun getRepos(@Query("per_page") per_page: Int, @Query("page") page: Int): RepoResponse

    companion object {
        private const val BASE_URL = "https://api.github.com/"
        fun create(): GithubService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GithubService::class.java)
        }
    }
}

class RepoPagingSource(val gitHubService: GithubService):PagingSource<Int,Repo>(){
    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? =null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        return try {
            val page = params.key?:1
            val prePage =params.loadSize
            val repo = gitHubService.getRepos(prePage,page)
            val items = repo.items
            val prevPage = if (page>1)page-1 else null
            val nextPage = if (items.isNotEmpty()) page+1 else null
            LoadResult.Page(items,prevPage,nextPage)
        }catch (e:Exception){
            LoadResult.Error(e)
        }
    }
}

object Repository {
    private const val PAGE_SIZE = 50
    private val gitHubService = GithubService.create()
    fun getPageData(): Flow<PagingData<Repo>> {
        return Pager(
            config = PagingConfig(PAGE_SIZE),
            pagingSourceFactory = { RepoPagingSource(gitHubService) }
        ).flow
    }
}

class GithubPagingAdapter : PagingDataAdapter<Repo, GithubPagingAdapter.ViewHolder>(diffUtil) {
    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Repo>() {
            override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
                return oldItem == newItem
            }

        }
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val name = item.findViewById<TextView>(R.id.name)
        val description = item.findViewById<TextView>(R.id.description)
        val starts = item.findViewById<TextView>(R.id.starts)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.name.text = item.name
            holder.description.text = item.description
            holder.starts.text = item.stargazers_count.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.github_starts_item, parent, false)
        return ViewHolder(view)
    }
}


