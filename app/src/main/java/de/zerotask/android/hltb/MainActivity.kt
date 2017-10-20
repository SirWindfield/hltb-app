package de.zerotask.android.hltb

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import de.zerotask.android.hltb.api.API
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private val api = API.hltbAPI

    private lateinit var adapter: GameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        adapter = GameAdapter(this)

        val layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        // register listener for input changes
        val searchView: SearchView = menu?.findItem(R.id.action_search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                // make sure we lose focus so the kb closes automatically
                searchView.clearFocus()

                // search the web api
                api.search(query!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .toList()
                        .subscribe { it ->
                            // load games into adapter and notify for changes being made
                            adapter.games = it
                            adapter.notifyDataSetChanged()
                        }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        return true
    }
}
