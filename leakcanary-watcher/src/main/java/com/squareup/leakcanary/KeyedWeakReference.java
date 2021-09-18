/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.leakcanary;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import static com.squareup.leakcanary.Preconditions.checkNotNull;

/**
 * @see {@link HeapDump#referenceKey}.
 */
//弱引用    有单参数构造 双参数构造 (T var1, ReferenceQueue<? super T> var2)
//软引用建议 有单参数构造 双参数构造 (T var1, ReferenceQueue<? super T> var2)
// * <p>Most applications should use an {@code android.util.LruCache} instead of
// * soft references. LruCache has an effective eviction policy and lets the user
// * tune how much memory is allotted.
// *
// * @author   Mark Reinhold
// * @since    1.2
// */
//
//public class SoftReference<T> extends Reference<T> {

//PhantomReference 虚引用 只有双参数构造 (T var1, ReferenceQueue<? super T> var2)

//ReferenceQueue存储这些引用对象，而不是内部的对象。 这是最正确的检测方法，而不是使用finalize来进行判断。
// 当只有弱引用且弱引用的引用对象释放了，则会添加到第二个参数中的队列中去！！！！！！

final class KeyedWeakReference extends WeakReference<Object> { //Object 垃圾回收的是这个object 只有弱引用会直接gc干掉
    //强引用不符合gc条件
    //弱一点的引用 其他三个引用
    public final String key;
    public final String name;

    KeyedWeakReference(Object referent, String key, String name,
                       ReferenceQueue<Object> referenceQueue) {
        super(checkNotNull(referent, "referent"), checkNotNull(referenceQueue, "referenceQueue"));
        this.key = checkNotNull(key, "key");
        this.name = checkNotNull(name, "name");
    }
}