package com.randompicker.handler;

import com.randompicker.repository.ResultRepository;
import com.randompicker.service.NamePickerService;
import com.randompicker.util.FormParser;
import com.randompicker.util.HttpResponses;
import com.randompicker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PickHandler implements HttpHandler {

    private final NamePickerService namePickerService;
    private final ResultRepository resultRepository;

    public PickHandler(NamePickerService namePickerService, ResultRepository resultRepository) {
        this.namePickerService = namePickerService;
        this.resultRepository = resultRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePost(exchange);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<String> names = resultRepository.readNames();
            sendPickResult(exchange, names, 1, "、");
            return;
        }

        HttpResponses.json(exchange, 405, false, "只支持 GET 或 POST 请求");
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Map<String, String> form = FormParser.parse(exchange);
        List<String> names = namePickerService.parseNames(
                form.getOrDefault("names", ""),
                form.getOrDefault("splitMode", "auto"),
                form.getOrDefault("customSplit", "")
        );
        int count = namePickerService.parseCount(form.getOrDefault("count", "1"));
        String outputSeparator = form.getOrDefault("outputSeparator", "、");

        sendPickResult(exchange, names, count, outputSeparator);
    }

    private void sendPickResult(HttpExchange exchange, List<String> names, int count, String separator) throws IOException {
        if (names.isEmpty()) {
            HttpResponses.json(exchange, 400, false, "名单为空，请先输入名字。");
            return;
        }

        if (count < 1) {
            HttpResponses.json(exchange, 400, false, "抽取人数至少为 1。");
            return;
        }

        if (count > names.size()) {
            HttpResponses.json(exchange, 400, false, "抽取人数不能超过名单人数，目前共有 " + names.size() + " 人。");
            return;
        }

        List<String> selectedNames = namePickerService.pickRandomNames(names, count);
        String result = String.join(separator, selectedNames);
        resultRepository.saveResult(selectedNames);

        String json = """
                {
                  "success": true,
                  "names": [%s],
                  "result": "%s"
                }
                """.formatted(JsonUtil.toJsonArrayItems(selectedNames), JsonUtil.escape(result));

        HttpResponses.json(exchange, 200, json);
    }
}
