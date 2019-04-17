/* **************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************/
package com.miracl.mpinsdksample.util;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.miracl.mpinsdk.model.Status;

public class ToastUtils {

    public static void showStatus(final @NonNull Activity activity, final @NonNull Status status) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(activity, "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(),
                  Toast.LENGTH_SHORT).show();
            }
        });
    }
}
