/*
 * Copyright 2015 Chaos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chaos.fx.cnbeta.hotarticles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.chaos.fx.cnbeta.ContentActivity;
import org.chaos.fx.cnbeta.R;
import org.chaos.fx.cnbeta.app.BaseFragment;
import org.chaos.fx.cnbeta.net.CnBetaApi;
import org.chaos.fx.cnbeta.net.CnBetaApiHelper;
import org.chaos.fx.cnbeta.net.model.ArticleSummary;
import org.chaos.fx.cnbeta.widget.BaseAdapter;
import org.chaos.fx.cnbeta.widget.SwipeLinearRecyclerView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Chaos
 *         2015/11/15.
 */
public class Top10Fragment extends BaseFragment implements SwipeLinearRecyclerView.OnRefreshListener {

    @Bind(R.id.swipe_recycler_view)
    SwipeLinearRecyclerView mTop10View;

    private Top10Adapter mTop10Adapter;

    private Call<CnBetaApi.Result<List<ArticleSummary>>> mCall;

    public static Top10Fragment newInstance() {
        return new Top10Fragment();
    }

    private int mPreClickPosition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle(R.string.nav_hot_articles);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_swipe_recycler_view, container, false);
        ButterKnife.bind(this, rootView);

        mTop10Adapter = new Top10Adapter(getActivity(), mTop10View.getRecyclerView());
        mTop10Adapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                mPreClickPosition = position;
                ArticleSummary summary = mTop10Adapter.get(position);
                ContentActivity.start(getActivity(), summary.getSid(), summary.getTopicLogo());
            }
        });
        mTop10View.setAdapter(mTop10Adapter);

        mTop10View.setOnRefreshListener(this);
        mTop10View.post(new Runnable() {
            @Override
            public void run() {
                mTop10View.setRefreshing(true);
                loadTop10Articles();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTop10Adapter.notifyItemChanged(mPreClickPosition);
    }

    @Override
    public void onDestroyView() {
        if (mCall != null) {
            mCall.cancel();
        }
        super.onDestroyView();
    }

    private void loadTop10Articles() {
        mCall = CnBetaApiHelper.top10();
        mCall.enqueue(new Callback<CnBetaApi.Result<List<ArticleSummary>>>() {
            @Override
            public void onResponse(Call<CnBetaApi.Result<List<ArticleSummary>>> call,
                                   Response<CnBetaApi.Result<List<ArticleSummary>>> response) {
                if (response.code() == 200) {
                    List<ArticleSummary> result = response.body().result;
                    if (!result.isEmpty() && !mTop10Adapter.containsAll(result)) {
                        mTop10Adapter.clear();
                        mTop10Adapter.addAll(0, result);
                    } else {
                        showSnackBar(R.string.no_more_articles);
                    }
                } else {
                    showSnackBar(R.string.load_articles_failed);
                }
                mTop10View.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<CnBetaApi.Result<List<ArticleSummary>>> call, Throwable t) {
                if (isVisible()) {
                    showSnackBar(R.string.load_articles_failed);
                }
                mTop10View.setRefreshing(false);
            }
        });
    }

    private void showSnackBar(@StringRes int strId) {
        Snackbar.make(mTop10View, strId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        loadTop10Articles();
    }
}
