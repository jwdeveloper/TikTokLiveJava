/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.tools.tester;

import io.github.jwdeveloper.tiktok.tools.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.tools.db.tables.TikTokDataTable;
import io.github.jwdeveloper.tiktok.tools.tester.api.DataTester;
import io.github.jwdeveloper.tiktok.tools.tester.api.DataTesterModel;
import io.github.jwdeveloper.tiktok.tools.tester.mockClient.TikTokLiveMock;
import io.github.jwdeveloper.tiktok.tools.tester.mockClient.mocks.LiveClientMock;

import java.util.LinkedList;
import java.util.Queue;

public class TikTokDataTester implements DataTester {
    private DataTesterModel model;

    private LiveClientMock client;

    private Queue<TikTokDataTable> data;

    private TikTokDatabase database;

    public TikTokDataTester(DataTesterModel model) {
        this.model = model;
    }

    @Override
    public void connect() {

        try {
            database = new TikTokDatabase(model.getDatabaseName());
            database.connect();
            var mockBuilder = TikTokLiveMock.create();
            model.getBuilderConsumer().accept(mockBuilder);
            client = mockBuilder.build();
            var respocnes = database.getSessionResponces(model.getSessionTag(), model.getUser());
            data = new LinkedList<>(respocnes);
            client.connect();
            while (!data.isEmpty()) {
                nextResponse();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while running tester", e);
        }
    }

    @Override
    public void nextResponse() {
        try {
            var responce = data.poll();
            client.publishResponse(responce.getContent());
            Thread.sleep(1);
        } catch (Exception e) {
            throw new RuntimeException("Unable to run response!");
        }
    }

    @Override
    public void disconnect() {

        try {
            client.disconnect();
            database.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
