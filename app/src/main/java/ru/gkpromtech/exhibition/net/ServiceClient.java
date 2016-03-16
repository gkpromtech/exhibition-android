/*
 * Copyright 2016 Promtech. All rights reserved.
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
package ru.gkpromtech.exhibition.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

import ru.gkpromtech.exhibition.utils.Callback;
import ru.gkpromtech.exhibition.utils.SharedData;

public class ServiceClient extends HttpClient {
    private final static ResponseProcessor<JsonNode> mProcessor = new ResponseProcessor<JsonNode>() {
        @Override
        public JsonNode process(InputStream in) throws Exception {
            return new ObjectMapper().readTree(in);
        }
    };

    public static void getJson(String path, Callback<JsonNode> callback) {
        get(SharedData.REST_SERVER_URL + path, callback, mProcessor);
    }

    public static void getJsonByUrl(String url, Callback<JsonNode> callback) {
        get(url, callback, mProcessor);
    }

    public static JsonNode getJson(String path) throws Exception {
        return mProcessor.process(get(SharedData.REST_SERVER_URL + path));
    }
}


