package com.bilibili.api;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class RESTfulApi {
    private final Map<Integer, Map<String, Object>> dataMap;

    public RESTfulApi() {
        dataMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", i);
            map.put("name", "name" + i);
            map.put("age", 20 + i);
            dataMap.put(i, map);
        }
    }

    @GetMapping("/objects")
    public List<Map<String, Object>> listObjects() {
        return dataMap.values().stream().collect(Collectors.toList());
    }

    @GetMapping("/objects/{id}")
    public Map<String, Object> getObjectById(@PathVariable Integer id) {
        return dataMap.get(id);
    }

    @DeleteMapping("/objects/{id}")
    public Map<String, Object> deleteObjectById(@PathVariable Integer id) {
        return dataMap.remove(id);
    }

    @PostMapping("/objects")
    public Map<String, Object> addObject(@RequestBody Map<String, Object> object) {
        int id = dataMap.size();
        object.put("id", id);
        dataMap.put(id, object);
        return object;
    }

    @PutMapping("/objects/{id}")
    public Map<String, Object> updateObjectById(@PathVariable Integer id, @RequestBody Map<String, Object> object) {
        object.put("id", id);
        dataMap.put(id, object);
        return object;
    }

}
