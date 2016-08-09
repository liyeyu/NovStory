package com.liyeyu.novstory.events;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by Liyeyu on 2016/7/20.
 */
public class RxBus {
    private static final RxBus INSTANCE = new RxBus();

    private final Subject<Object, Object> mBusSubject = new SerializedSubject<>(PublishSubject.create());

    public static RxBus get() {
        return INSTANCE;
    }
    public <T> Subscription register(final Class<T> eventClass, Action1<T> onNext) {
        return mBusSubject
                .filter(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return o.getClass().equals(eventClass);
                    }
                })
                .map(new Func1<Object, T>() {
                    @Override
                    public T call(Object o) {
                        return (T)o;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    public void post(Object event) {
        mBusSubject.onNext(event);
    }
    public void unRegister(Object event) {
        mBusSubject.onNext(event);
    }
}
