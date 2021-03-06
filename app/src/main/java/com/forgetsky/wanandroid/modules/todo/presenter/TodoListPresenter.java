/*
 *     (C) Copyright 2019, ForgetSky.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.forgetsky.wanandroid.modules.todo.presenter;

import com.forgetsky.wanandroid.R;
import com.forgetsky.wanandroid.app.WanAndroidApp;
import com.forgetsky.wanandroid.base.presenter.BasePresenter;
import com.forgetsky.wanandroid.core.event.TodoStatusEvent;
import com.forgetsky.wanandroid.core.rx.BaseObserver;
import com.forgetsky.wanandroid.modules.todo.bean.TodoListData;
import com.forgetsky.wanandroid.modules.todo.contract.TodoListContract;
import com.forgetsky.wanandroid.utils.RxUtils;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class TodoListPresenter extends BasePresenter<TodoListContract.View>
        implements TodoListContract.Presenter {

    @Inject
    TodoListPresenter() {
    }

    private int currentPage = 1;
    private boolean isRefresh = true;
    private int type;
    private int status;

    @Override
    public void refreshLayout(int type, int status, boolean isShowStatusView) {
        this.type = type;
        this.status = status;
        isRefresh = true;
        currentPage = 1;
        getTodoListData(isShowStatusView);
    }

    @Override
    public void getTodoListData(boolean isShowStatusView) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("status", status);
        map.put("priority", 0); //默认全部
        if (status == 1) {
            map.put("orderby", 2);
        } else {
            map.put("orderby", 4);
        }
        addSubscribe(mDataManager.getTodoListData(currentPage, map)
                .compose(RxUtils.SchedulerTransformer())
                .filter(todoListData -> mView != null)
                .subscribeWith(new BaseObserver<TodoListData>(mView,
                        WanAndroidApp.getContext().getString(R.string.failed_to_obtain_article_list),
                        isShowStatusView) {
                    @Override
                    public void onSuccess(TodoListData todoListData) {
                        if(isShowStatusView && currentPage == 1 &&
                                todoListData.getDatas().size() < 1) {
                            mView.showEmpty();
                        } else {
                            mView.showTodoListData(todoListData, isRefresh);
                        }
                    }
                }));
    }

    @Override
    public void reload() {
        refreshLayout(type, status, true);
    }



    @Override
    public void loadMore() {
        isRefresh = false;
        currentPage++;
        getTodoListData(false);
    }

    @Override
    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    @Subscriber()
    public void TodoStatusEvent(TodoStatusEvent todoEvent) {
        mView.todoStatusChange(todoEvent.getStatus());
    }
}
