package com.dongyu.movies;

import com.dongyu.movies.model.parser.PlayParam;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTest {

    @Test
    public void test() {
        // /index.php/vod/play/id/106197/sid/1/nid/1.html
        // /vodplay/75319-2-1.html
        // /paly-9MdSCS-1-1.html
        Pattern pattern = Pattern.compile("([0-9a-zA-Z]+)[-/sid]+(\\d+)[-/nid]+(\\d+)\\.html");
        Matcher matcher = pattern.matcher("/paly-9MdSCS-1-1.html");
        if (matcher.find()) {
            String detailId = matcher.group(1);
            String routeId = matcher.group(2);
            String selectionId = matcher.group(3);
            assert detailId != null : "detailId";
            assert routeId != null : "routeId";
            assert selectionId != null : "selectionId";
            System.out.println(detailId);
        } else {
            System.out.println("未匹配");
        }
    }
}
