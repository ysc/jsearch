/**
 *
 * APDPlat - Application Product Development Platform Copyright (c) 2013, 杨尚川,
 * yang-shangchuan@qq.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.apdplat.jsearch.search;

import org.apdplat.jsearch.score.ProximityScore;
import org.apdplat.jsearch.score.Score;
import org.apdplat.jsearch.score.WordFrequencyScore;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 文本搜索
 * @author 杨尚川
 */
public class TextSearcher implements Searcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextSearcher.class);
    private String indexText = "data/index_text.txt";
    private String index = "data/index.txt";
    private Map<String, String> indexMap = new ConcurrentHashMap<>();
    private Score score = new WordFrequencyScore();
    private int pageSize = 10;

    public TextSearcher(){
        init();
    }

    public TextSearcher(String index, String indexText){
        this(index, indexText, 10);
    }

    public TextSearcher(String index, String indexText, int pageSize){
        this.index = index;
        this.indexText = indexText;
        this.pageSize = pageSize;
        init();
    }

    private void init(){
        try{
            LOGGER.info("开始初始化索引");
            long start = System.currentTimeMillis();
            Files.readAllLines(Paths.get(index)).parallelStream().forEach(line -> {
                String[] attrs = line.split("=");
                if (attrs != null && attrs.length == 3) {
                    indexMap.put(attrs[0], attrs[2]);
                }
            });
            LOGGER.info("索引初始化完毕，耗时：" + (System.currentTimeMillis()-start) + "毫秒");
        }catch (Exception e){
            LOGGER.error("索引初始化失败", e);
            throw new RuntimeException(e);
        }
    }

    public String getIndexText() {
        return indexText;
    }

    public void setIndexText(String indexText) {
        this.indexText = indexText;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    @Override
    public Hits search(String keyword){
        return search(keyword, SearchMode.INTERSECTION);
    }

    @Override
    public Hits search(String keyword, SearchMode searchMode){
        long start = System.currentTimeMillis();
        //搜索关键词
        List<Doc> docs = hit(keyword, searchMode);
        int hitCount = docs.size();
        int limit = docs.size() > pageSize ? pageSize : docs.size();
        docs = docs.subList(0, limit);
        //获取文档
        docs(docs);
        long cost = System.currentTimeMillis()-start;
        LOGGER.info("搜索耗时："+cost+"毫秒");
        return new Hits(hitCount, docs);
    }

    public List<Doc> hit(String keyword, SearchMode searchMode){
        long start = System.currentTimeMillis();
        LOGGER.info("搜索关键词：" + keyword);
        List<Word> words = WordSegmenter.seg(keyword, SegmentationAlgorithm.PureEnglish);
        LOGGER.info("分词结果："+words);
        //搜索结果文档
        Set<Doc> result = new ConcurrentSkipListSet<>();
        if(words.size()==1){
            //单 词 查询
            result.addAll(term(words.get(0).getText()));
        }else{
            //多 词 查询
            result.addAll(term(words.get(0).getText()));
            for(int i=1; i<words.size(); i++){
                if(searchMode==SearchMode.INTERSECTION) {
                    SearchMode.intersection(result, term(words.get(i).getText()));
                }else {
                    SearchMode.union(result, term(words.get(i).getText()));
                }
            }
        }
        //文档评分排序
        List<Doc> finalResult = result.parallelStream()
                //评分
                .map(doc -> {
                    doc.setScore(score.score(doc, words.stream().map(word -> word.getText()).collect(Collectors.toList())));
                    return doc;
                })
                //排序
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .collect(Collectors.toList());
        long cost = System.currentTimeMillis()-start;
        LOGGER.info("命中数：："+result.size());
        LOGGER.info("查询索引耗时："+cost+"毫秒");
        return finalResult;
    }

    public void docs(List<Doc> docs, int start, int length){
        docs(docs.subList(start, start+length));
    }

    public void docs(List<Doc> docs){
        long startTime = System.currentTimeMillis();
        int lineCount = 0;
        Set<Integer> ids = docs.parallelStream().map(doc -> doc.getId()).collect(Collectors.toSet());
        final Map<Integer, String> data = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(indexText)))){
            String line = null;
            while (data.size()<docs.size() && (line=reader.readLine())!=null){
                lineCount++;
                if(ids.contains(lineCount)){
                    data.put(lineCount, line);
                }
            }
        }catch (Exception e){
            LOGGER.error("读取文件失败", e);
        }
        docs.parallelStream().forEach(doc->doc.setText(data.get(doc.getId())));
        data.clear();
        ids.clear();
        ids = null;
        long cost = System.currentTimeMillis()-startTime;
        LOGGER.info("准备文本耗时："+cost+"毫秒");
    }

    private Set<Doc> term(String word){
        String posting = indexMap.get(word);
        if(posting==null){
            return Collections.emptySet();
        }
        //System.out.println("posting:"+posting);
        String[] postingItems = posting.split("\\|");
        Set<Doc> set = new HashSet<>();
        int lastDocId=0;
        for(String postingItem : postingItems){
            //System.out.println("postingItem:"+postingItem);
            String[] postingItemAttr = postingItem.split("_");
            int delta = Integer.parseInt(postingItemAttr[0]);
            lastDocId+=delta;
            int frequency = Integer.parseInt(postingItemAttr[1]);
            String positions = postingItemAttr[2];
            Doc doc = new Doc();
            doc.setId(lastDocId);
            doc.setFrequency(frequency);
            //System.out.println("docId:"+lastDocId);
            //System.out.println("frequency:"+frequency);
            List<Integer> pos = new ArrayList<>();
            for(String position : positions.split(":")){
                pos.add(Integer.parseInt(position));
                //System.out.println("position:"+position);
            }
            doc.putWordPosition(word, pos);
            set.add(doc);
        }
        return Collections.unmodifiableSet(set);
    }

    public static void main(String[] args) {
        TextSearcher textSearcher = new TextSearcher();

        textSearcher.setScore(new WordFrequencyScore());
        //SearchMode.INTERSECTION
        Hits hits = textSearcher.search("shingles", SearchMode.INTERSECTION);
        LOGGER.info("搜索结果数："+hits.getHitCount());
        AtomicInteger i = new AtomicInteger();
        hits.getDocs().forEach(doc -> LOGGER.info("Result" + i.incrementAndGet() + "、ID：" + doc.getId() + "，Score：" + doc.getScore() + "，Text：" + doc.getText()));

        textSearcher.setScore(new ProximityScore());
        hits = textSearcher.search("distributed algorithm ", SearchMode.INTERSECTION);
        LOGGER.info("搜索结果数："+hits.getHitCount());
        AtomicInteger j = new AtomicInteger();
        hits.getDocs().forEach(doc -> LOGGER.info("Result" + j.incrementAndGet() + "、ID：" + doc.getId() + "，Score：" + doc.getScore() + "，Text：" + doc.getText()));

    }
}
