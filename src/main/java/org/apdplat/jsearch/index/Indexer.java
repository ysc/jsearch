/*
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apdplat.jsearch.index;

import org.apdplat.word.segmentation.SegmentationAlgorithm;

/**
 * 索引接口
 * @author 杨尚川
 */
public interface Indexer {
    /**
     * 为目录及其所有子目录下的所有文本建立索引, 默认使用纯英文分词器
     * @param dir 目录
     */
    void indexDir(String dir);
    void indexDir(String dir, SegmentationAlgorithm segmentationAlgorithm);
}
