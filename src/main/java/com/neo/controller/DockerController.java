package com.neo.controller;

import com.neo.domain.TestSuite;
import com.neo.service.generator.AETG;
import com.neo.service.handler.MFTVerifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
//@RequestMapping("/generation")
public class DockerController {
	
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    // ACTS 3.0 version
    public TestSuite Generation(HttpServletRequest request) {
        BufferedReader br;
        StringBuilder sb = null;
        String reqBody = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    request.getInputStream()));
            String line = null;
            sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            reqBody = URLDecoder.decode(sb.toString(), "UTF-8");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject(reqBody);
        int parameters = (Integer)jsonObject.get("parameters");
        int strength = (Integer)jsonObject.get("strength");
        JSONArray jsonArray = (JSONArray)jsonObject.get("values");
        List valueList = jsonArray.toList();
        int[] values = new int[valueList.size()];
        for(int i = 0; i < values.length; i++)
            values[i] = (Integer)valueList.get(i);
	  jsonArray = (JSONArray)jsonObject.get("constraint");
	  List constraintList = jsonArray.toList();
	  ArrayList<int[]> constraint = new ArrayList<>();
	  for(int i = 0; i < constraintList.size(); i++){
	    String line = (String)constraintList.get(i);
		//constraint.add((String)constraintList.get(i));
		String[] split = line.trim().split(" ");
		int[] each = new int[split.length / 2];
		for (int j = 0; j < split.length; j += 2) {
		  // specific symbol, note that the representation in the file
		  // starts from 0 but the representation in the solver starts
		  // from 1.
		  int v = Integer.valueOf(split[j + 1]) + 1;
		  each[j / 2] = split[j].equals("-") ? -v : v;
		}
		constraint.add(each);

	  }
        Instant start = Instant.now();
        com.neo.service.combinatorial.CTModel model = new com.neo.service.combinatorial.CTModel(parameters,  values,strength, constraint, new MFTVerifier());
        AETG aetg = new AETG();
        com.neo.service.combinatorial.TestSuite ts = new com.neo.service.combinatorial.TestSuite();
        aetg.generation(model, ts);
        Instant end = Instant.now();
        long time = Duration.between(start, end).toMillis();

        ArrayList<int[]> testsuite = new ArrayList<>();
        for(int i = 0; i < ts.suite.size(); i++)
            testsuite.add(ts.suite.get(i).test);
        TestSuite res = new TestSuite(testsuite, time);
        return res;
    }

    @GetMapping("/check")
    public String healthCheck(){
        return "ok";
    }
}