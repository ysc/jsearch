###jsearch是一个高性能的全文检索工具包，基于倒排索引，基于java8，类似于lucene，但更轻量级。

####[捐赠致谢](https://github.com/ysc/QuestionAnsweringSystem/wiki/donation)

####索引文件结构

    1、一个词的索引由=分割的三部分组成：
        第一部分是词
        第二部分是这个词在多少个文档中出现过（上限1000）
        第三部分是倒排表
    2、倒排表由多个倒排表项目组成，倒排表项目之间使用|分割
    3、倒排表项目的组成又分为三部分，用_分割：
        第一部分是文档ID
        第二部分是词频
        第三部分是词的位置
    4、词的位置用:分割
    
    例如:
    shingles=31=47466_1_2|1_1_6|1_1_1|2_1_5|67_1_1|903_1_3|17_1_5|1_3_4:6:11
    表示词 shingles 的索引：
    词：shingles
    有 31 个文档包含 shingles 这个词
    包含这个词的第一篇文档的ID是47466，
    shingles 的词频是1，出现 shingles 的位置是2
    文档内容为：
    A better solution is to use shingles, which are compound tokens created 
    from multiple adjacent tokens.
    对文档内容进行分词并移除停用词之后的结果为：
    [solution, shingles, compound, tokens, created, multiple, adjacent, tokens]
    
    包含这个词的第二篇文档的ID是47466+1=47467，
    shingles 的词频是1，出现 shingles 的位置是6
    文档内容为：
    Lucene has a sandbox module that simplifies adding shingles to your index, 
    described in section 8.3.2
    对文档内容进行分词并移除停用词之后的结果为：
    [lucene, sandbox, module, simplifies, adding, shingles, index, section]
    
    包含这个词的第八篇文档的ID是47466+1+1+2+67+903+17+1=48458，
    shingles 的词频是3，出现 shingles 的位置分别是4、6、11
    文档内容为：
    For example the sentence “please divide this sentence into shingles” 
    might be tokenized into the shingles “please divide”, “divide this”, 
    “this sentence”, “sentence into” and “into shingles”
    对文档内容进行分词并移除停用词之后的结果为：
    [sentence, divide, sentence, shingles, tokenized, shingles, divide, divide, sentence, sentence, shingles]
    
    这里需要注意的是位置不是和原文一一对应的，而是和去除停用词后的位置一一对应的
    分词使用word分词提供的针对纯英文文本的分词器
    
[停用词的定义](https://github.com/ysc/word/blob/master/src/main/resources/stopwords.txt)

[word分词提供的针对纯英文文本的分词器](https://github.com/ysc/word/blob/master/src/main/java/org/apdplat/word/segmentation/impl/PureEnglish.java)

[word分词](https://github.com/ysc/word)
        

