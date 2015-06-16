package com.bob.digcsdn.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.JsonObjectRequest;
import com.bob.digcsdn.R;
import com.bob.digcsdn.adapter.CommentAdapter;
import com.bob.digcsdn.bean.Comment;
import com.bob.digcsdn.bean.Page;
import com.bob.digcsdn.util.Constants;
import com.bob.digcsdn.util.JsoupUtil;
import com.bob.digcsdn.util.UrlUtil;
import com.bob.digcsdn.util.VolleyUtil;
import com.bob.digcsdn.view.LoadMoreListView;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by bob on 15-6-16.
 */
public class CommentsActivity extends Activity implements LoadMoreListView.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private LoadMoreListView listView;
    private SwipeRefreshLayout swipeLayout;
    private CommentAdapter adapter;

    private ProgressBar progressBar;
    private ImageView backBtn;
    private TextView tvComment;
    private Button reloadBtn;
    private View reloadView;

    public static String commentCount = "";
    private Page page;
    private String fileName;
    private int pageIndex = 1;
    private int pageSize = 20;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();
        initWidget();
        initEvent();
    }

    private void init() {
        fileName = getIntent().getExtras().getString("fileName");//获取由上一个活动传来的参数
        page = new Page();
        adapter = new CommentAdapter(this);
    }

    private void initWidget() {
        progressBar = (ProgressBar) findViewById(R.id.pro_common_content);
        reloadBtn = (Button) findViewById(R.id.bt_comment_reLoad);
        reloadView = findViewById(R.id.ll_comment_reLoad);
        backBtn = (ImageView) findViewById(R.id.img_comment_back);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeLayout.setOnRefreshListener(this);//下拉组件的事件监听

        // set style for swipeRefreshLayout
        swipeLayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_green_light,
                android.R.color.holo_blue_bright);
        listView = (LoadMoreListView) findViewById(R.id.list_article_view);
        listView.setAdapter(adapter);
    }

    private void initEvent() {
        progressBar.setOnClickListener(this);
        reloadBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_comment_reLoad:
                executeRefresh(Constants.DEF_TASK_TYPE.REFRESH);
                break;
            case R.id.img_article_detail_back:
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_no, R.anim.push_right_out);
    }

    private void executeRefresh(final int taskType) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest(UrlUtil.getCommentListURL(fileName, page.getCurrentPage()), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject json) {
                progressBar.setVisibility(View.INVISIBLE);
                reloadView.setVisibility(View.INVISIBLE);
                List<Comment> comments = JsoupUtil.getBlogCommentList(json, Integer.parseInt(page.getCurrentPage()), pageSize);

                if (comments.size() == 0) {//重复或者空列表，则停止加载
                    listView.setCanLoadMore(false);//停止加载
                }
                if (taskType == Constants.DEF_TASK_TYPE.REFRESH) {
                    adapter.setList(comments);
                    adapter.notifyDataSetChanged();

                    if (adapter.getCount() == 0) {
                        Toast.makeText(CommentsActivity.this, "无评论", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    adapter.addList(comments);
                    adapter.notifyDataSetChanged();
                    listView.onLoadMoreComplete();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.INVISIBLE);
                reloadView.setVisibility(View.VISIBLE);
            }
        });
        VolleyUtil.getQueue().add(jsonRequest);
    }


    @Override
    public void onLoadMore() {
        executeRefresh(Constants.DEF_TASK_TYPE.LOAD);
    }

    @Override
    public void onRefresh() {
        executeRefresh(Constants.DEF_TASK_TYPE.REFRESH);
    }

}
