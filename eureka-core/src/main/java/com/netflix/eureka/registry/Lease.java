/*
 * Copyright 2014 Netflix, Inc.
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

package com.netflix.eureka.registry;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represent a lease over an element E
 * This object should be thread safe
 * TODO: figure out a way to sync time between write servers (or even if necessary)
 * @author David Liu
 */
public class Lease<E> {

    public static final int DEFAULT_LEASE_DURATION_MILLIS = 90 * 1000;  // TODO: get default via config

    private final AtomicReference<E> holderRef;

    private final AtomicLong lastRenewalTimestamp;  // timestamp of last renewal
    private final AtomicLong leaseDurationMillis;  // duration of this lease in millis

    public Lease(E holder) {
        this(holder, DEFAULT_LEASE_DURATION_MILLIS);
    }

    public Lease(E holder, long durationMillis) {
        holderRef = new AtomicReference<E>(holder);

        lastRenewalTimestamp = new AtomicLong(System.currentTimeMillis());
        leaseDurationMillis = new AtomicLong(durationMillis);
    }

    // TODO make this less expensive than a full compare on InstanceInfo
    public boolean compareAndSet(E expected, E update) {
        return holderRef.compareAndSet(expected, update);
    }

    public E getHolder() {
        return holderRef.get();
    }

    public long getLastRenewalTimestamp() {
        return lastRenewalTimestamp.get();
    }

    public long getLeaseDurationMillis() {
        return leaseDurationMillis.get();
    }

    public void renew() {
        lastRenewalTimestamp.set(System.currentTimeMillis());
    }

    public void renew(long durationMillis) {
        leaseDurationMillis.set(durationMillis);
        renew();
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() > (lastRenewalTimestamp.get() + leaseDurationMillis.get());
    }

    public void cancel() {
        // TODO necessary?
    }
}